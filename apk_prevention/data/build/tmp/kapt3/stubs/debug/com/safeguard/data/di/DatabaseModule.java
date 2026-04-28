package com.safeguard.data.di;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
import com.safeguard.core.domain.repository.ThreatFeedCursorStore;
import com.safeguard.core.domain.repository.ThreatFeedStatus;
import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.data.local.database.SafeGuardDatabase;
import kotlinx.coroutines.flow.Flow;
import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.dao.QuarantineDao;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.safeguard.data.local.database.dao.TrustedAppDao;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import net.sqlcipher.database.SupportFactory;
import java.io.File;
import javax.inject.Named;
import javax.inject.Singleton;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u001c\u0010\u000b\u001a\u00020\n2\b\b\u0001\u0010\f\u001a\u00020\r2\b\b\u0001\u0010\u000e\u001a\u00020\u000fH\u0007J\u0012\u0010\u0010\u001a\u00020\u000f2\b\b\u0001\u0010\f\u001a\u00020\rH\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0007J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0012\u0010\u001b\u001a\u00020\u001c2\b\b\u0001\u0010\f\u001a\u00020\rH\u0007J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u001f\u001a\u00020 2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0012\u0010!\u001a\u00020\u00162\b\b\u0001\u0010\f\u001a\u00020\rH\u0007J\u0010\u0010\"\u001a\u00020#2\u0006\u0010\u0015\u001a\u00020\u0016H\u0007J\u0010\u0010$\u001a\u00020%2\u0006\u0010\u0015\u001a\u00020\u0016H\u0007J\u0010\u0010&\u001a\u00020\'2\u0006\u0010\t\u001a\u00020\nH\u0007J\f\u0010(\u001a\u00020)*\u00020\u0016H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/safeguard/data/di/DatabaseModule;", "", "()V", "MIGRATION_3_4", "Landroidx/room/migration/Migration;", "MIGRATION_4_5", "MIGRATION_5_6", "provideAuditLogDao", "Lcom/safeguard/data/local/database/dao/AuditLogDao;", "db", "Lcom/safeguard/data/local/database/SafeGuardDatabase;", "provideDatabase", "context", "Landroid/content/Context;", "passphrase", "", "provideDbPassphrase", "provideDeletedApkDao", "Lcom/safeguard/data/local/database/dao/DeletedApkDao;", "provideFeedbackPrivacyGate", "Lcom/safeguard/core/domain/feedback/FeedbackPrivacyGate;", "prefs", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "provideMalwareSignatureDao", "Lcom/safeguard/data/local/database/dao/MalwareSignatureDao;", "provideQuarantineDao", "Lcom/safeguard/data/local/database/dao/QuarantineDao;", "provideQuarantineDir", "Ljava/io/File;", "provideScanFeedbackEventDao", "Lcom/safeguard/data/local/database/dao/ScanFeedbackEventDao;", "provideScanHistoryDao", "Lcom/safeguard/data/local/database/dao/ScanHistoryDao;", "provideSecurePreferencesManager", "provideThreatFeedCursorStore", "Lcom/safeguard/core/domain/repository/ThreatFeedCursorStore;", "provideThreatFeedStatusStore", "Lcom/safeguard/core/domain/repository/ThreatFeedStatusStore;", "provideTrustedAppDao", "Lcom/safeguard/data/local/database/dao/TrustedAppDao;", "toThreatFeedStatus", "Lcom/safeguard/core/domain/repository/ThreatFeedStatus;", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DatabaseModule {
    @org.jetbrains.annotations.NotNull
    private static final androidx.room.migration.Migration MIGRATION_3_4 = null;
    @org.jetbrains.annotations.NotNull
    private static final androidx.room.migration.Migration MIGRATION_4_5 = null;
    
    /**
     * Phase 3.2 — adds the privacy-preserving feedback queue. New table only; no existing
     * data is touched. We default the table to empty on an upgrade because every event
     * must be authored by the new opt-in flow (existing scans pre-3.2 weren't gated on
     * user consent, so even if we had old data we couldn't ethically migrate it).
     */
    @org.jetbrains.annotations.NotNull
    private static final androidx.room.migration.Migration MIGRATION_5_6 = null;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.di.DatabaseModule INSTANCE = null;
    
    private DatabaseModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.SafeGuardDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @javax.inject.Named(value = "db_pass")
    @org.jetbrains.annotations.NotNull
    byte[] passphrase) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Named(value = "db_pass")
    @org.jetbrains.annotations.NotNull
    public final byte[] provideDbPassphrase(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.ScanHistoryDao provideScanHistoryDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.QuarantineDao provideQuarantineDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.MalwareSignatureDao provideMalwareSignatureDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.TrustedAppDao provideTrustedAppDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.AuditLogDao provideAuditLogDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.DeletedApkDao provideDeletedApkDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.dao.ScanFeedbackEventDao provideScanFeedbackEventDao(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.SafeGuardDatabase db) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.preferences.SecurePreferencesManager provideSecurePreferencesManager(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    /**
     * Adapter exposing [SecurePreferencesManager]'s three privacy toggles as the small
     * [FeedbackPrivacyGate] interface. Letting the repository depend on the interface
     * (instead of the full prefs class) keeps it free of Android KeyStore imports — and
     * lets JVM unit tests script the gate without `EncryptedSharedPreferences`.
     */
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.feedback.FeedbackPrivacyGate provideFeedbackPrivacyGate(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager prefs) {
        return null;
    }
    
    /**
     * Production binding for the threat-feed cursor: thin adapter over
     * [SecurePreferencesManager.lastThreatFeedSyncMs] so the cursor is persisted in the same
     * `EncryptedSharedPreferences` file as every other privacy-sensitive setting.
     */
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.repository.ThreatFeedCursorStore provideThreatFeedCursorStore(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager prefs) {
        return null;
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
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.repository.ThreatFeedStatusStore provideThreatFeedStatusStore(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager prefs) {
        return null;
    }
    
    /**
     * Read all five threat-feed status fields in one go. Defensive against forward-incompatible
     * outcome strings: if the persisted enum name doesn't decode (someone older binary wrote
     * a value our build doesn't know about), we fall back to `NEVER` rather than crashing —
     * the worst outcome is the user sees "not yet synced" until the next worker run rewrites
     * the field with a recognised value.
     */
    private final com.safeguard.core.domain.repository.ThreatFeedStatus toThreatFeedStatus(com.safeguard.data.local.preferences.SecurePreferencesManager $this$toThreatFeedStatus) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "quarantine_dir")
    @org.jetbrains.annotations.NotNull
    public final java.io.File provideQuarantineDir(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
}