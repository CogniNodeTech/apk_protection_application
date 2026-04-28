package com.safeguard.data.di

import android.content.Context
import android.util.Log
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate
import com.safeguard.core.domain.repository.ThreatFeedCursorStore
import com.safeguard.core.domain.repository.ThreatFeedStatus
import com.safeguard.core.domain.repository.ThreatFeedStatusStore
import com.safeguard.data.local.database.SafeGuardDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.safeguard.data.local.database.dao.AuditLogDao
import com.safeguard.data.local.database.dao.DeletedApkDao
import com.safeguard.data.local.database.dao.MalwareSignatureDao
import com.safeguard.data.local.database.dao.QuarantineDao
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao
import com.safeguard.data.local.database.dao.ScanHistoryDao
import com.safeguard.data.local.database.dao.TrustedAppDao
import com.safeguard.data.local.preferences.SecurePreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `deleted_apks` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `apkName` TEXT NOT NULL,
                    `originalPath` TEXT NOT NULL,
                    `threatName` TEXT,
                    `riskScore` INTEGER NOT NULL,
                    `deletedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE deleted_apks ADD COLUMN apkSha256 TEXT")
        }
    }

    /**
     * Phase 3.2 — adds the privacy-preserving feedback queue. New table only; no existing
     * data is touched. We default the table to empty on an upgrade because every event
     * must be authored by the new opt-in flow (existing scans pre-3.2 weren't gated on
     * user consent, so even if we had old data we couldn't ethically migrate it).
     */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `scan_feedback_queue` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `createdAtMs` INTEGER NOT NULL,
                    `sha256` TEXT NOT NULL,
                    `verdict` TEXT NOT NULL,
                    `confidence` REAL NOT NULL,
                    `packageName` TEXT,
                    `versionCode` INTEGER,
                    `layerScoresJson` TEXT NOT NULL,
                    `triggeredRulesJson` TEXT NOT NULL,
                    `androidApiLevel` INTEGER NOT NULL,
                    `appVersionCode` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_scan_feedback_queue_createdAtMs` ON `scan_feedback_queue` (`createdAtMs`)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @Named("db_pass") passphrase: ByteArray
    ): SafeGuardDatabase {
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context, SafeGuardDatabase::class.java, "safeguard.db")
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()
    }

    @Provides
    @Named("db_pass")
    fun provideDbPassphrase(@ApplicationContext context: Context): ByteArray {
        val prefs = try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(
                context, "safeguard_db_key", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Encrypted prefs unavailable for DB key; refusing insecure fallback.", e)
            throw IllegalStateException("Secure DB key storage initialization failed", e)
        }
        var key = prefs.getString("db_key", null)
        if (key == null) {
            key = java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString()
            prefs.edit().putString("db_key", key).apply()
        }
        return key.toByteArray(Charsets.UTF_8).copyOf(32)
    }

    @Provides
    fun provideScanHistoryDao(db: SafeGuardDatabase): ScanHistoryDao = db.scanHistoryDao()
    @Provides
    fun provideQuarantineDao(db: SafeGuardDatabase): QuarantineDao = db.quarantineDao()
    @Provides
    fun provideMalwareSignatureDao(db: SafeGuardDatabase): MalwareSignatureDao = db.malwareSignatureDao()
    @Provides
    fun provideTrustedAppDao(db: SafeGuardDatabase): TrustedAppDao = db.trustedAppDao()
    @Provides
    fun provideAuditLogDao(db: SafeGuardDatabase): AuditLogDao = db.auditLogDao()
    @Provides
    fun provideDeletedApkDao(db: SafeGuardDatabase): DeletedApkDao = db.deletedApkDao()
    @Provides
    fun provideScanFeedbackEventDao(db: SafeGuardDatabase): ScanFeedbackEventDao = db.scanFeedbackEventDao()

    @Provides
    @Singleton
    fun provideSecurePreferencesManager(@ApplicationContext context: Context): SecurePreferencesManager =
        SecurePreferencesManager(context)

    /**
     * Adapter exposing [SecurePreferencesManager]'s three privacy toggles as the small
     * [FeedbackPrivacyGate] interface. Letting the repository depend on the interface
     * (instead of the full prefs class) keeps it free of Android KeyStore imports — and
     * lets JVM unit tests script the gate without `EncryptedSharedPreferences`.
     */
    @Provides
    @Singleton
    fun provideFeedbackPrivacyGate(prefs: SecurePreferencesManager): FeedbackPrivacyGate =
        object : FeedbackPrivacyGate {
            override val isFeedbackOptInEnabled: Boolean get() = prefs.scanFeedbackEnabled
            override val isTelemetryMasterEnabled: Boolean get() = prefs.scanTelemetryEnabled
            override val isPrivacySharingOptedOut: Boolean get() = prefs.privacySharingOptOut
        }

    /**
     * Production binding for the threat-feed cursor: thin adapter over
     * [SecurePreferencesManager.lastThreatFeedSyncMs] so the cursor is persisted in the same
     * `EncryptedSharedPreferences` file as every other privacy-sensitive setting.
     */
    @Provides
    @Singleton
    fun provideThreatFeedCursorStore(prefs: SecurePreferencesManager): ThreatFeedCursorStore =
        object : ThreatFeedCursorStore {
            override var cursorMs: Long
                get() = prefs.lastThreatFeedSyncMs
                set(value) { prefs.lastThreatFeedSyncMs = value }
        }

    /**
     * Production binding for the threat-feed status feed. Marshals between the typed
     * domain [ThreatFeedStatus] and the primitive fields stored in [SecurePreferencesManager],
     * so neither layer has to know about the other's vocabulary.
     *
     * `observe()` turns the prefs change-marker flow into a [ThreatFeedStatus] flow by
     * re-reading the snapshot on every epoch bump — `distinctUntilChanged` is intentionally
     * omitted because [SecurePreferencesManager.writeThreatFeedStatus] only fires when
     * something actually changed (it's a single atomic write per worker run).
     */
    @Provides
    @Singleton
    fun provideThreatFeedStatusStore(prefs: SecurePreferencesManager): ThreatFeedStatusStore =
        object : ThreatFeedStatusStore {
            override fun snapshot(): ThreatFeedStatus = prefs.toThreatFeedStatus()

            override fun update(status: ThreatFeedStatus) {
                prefs.writeThreatFeedStatus(
                    successMs = status.lastSuccessMs,
                    attemptMs = status.lastAttemptMs,
                    outcomeRaw = status.lastOutcome.name,
                    failureReason = status.lastFailureReason,
                    insertedCount = status.lastInsertedCount
                )
            }

            override fun observe(): Flow<ThreatFeedStatus> =
                prefs.lastThreatFeedStatusEpochFlow.map { prefs.toThreatFeedStatus() }
        }

    /**
     * Read all five threat-feed status fields in one go. Defensive against forward-incompatible
     * outcome strings: if the persisted enum name doesn't decode (someone older binary wrote
     * a value our build doesn't know about), we fall back to `NEVER` rather than crashing —
     * the worst outcome is the user sees "not yet synced" until the next worker run rewrites
     * the field with a recognised value.
     */
    private fun SecurePreferencesManager.toThreatFeedStatus(): ThreatFeedStatus {
        val outcome = runCatching {
            ThreatFeedStatus.Outcome.valueOf(lastThreatFeedOutcomeRaw)
        }.getOrDefault(ThreatFeedStatus.Outcome.NEVER)
        return ThreatFeedStatus(
            lastSuccessMs = lastThreatFeedSuccessMs,
            lastAttemptMs = lastThreatFeedAttemptMs,
            lastOutcome = outcome,
            lastFailureReason = lastThreatFeedFailureReason?.takeIf { it.isNotBlank() },
            lastInsertedCount = lastThreatFeedInsertedCount
        )
    }

    @Provides
    @Singleton
    @Named("quarantine_dir")
    fun provideQuarantineDir(@ApplicationContext context: Context): File {
        val dir = File(context.applicationContext.filesDir, "quarantine")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
