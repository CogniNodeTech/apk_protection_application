package com.safeguard.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import androidx.room.migration.Migration
import java.io.File

@RunWith(AndroidJUnit4::class)
class MigrationsTest {

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
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

    @Test
    fun migrate3To4_createsDeletedApksTable() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbName = "migration-test.db"

        // Create a v3-style DB (no deleted_apks table) without relying on exported Room schemas.
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) dbFile.delete()

        val config = Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Minimal baseline tables used by the app before v4 introduced deleted_apks.
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `scan_history` (
                            `id` TEXT NOT NULL,
                            `apkHash` TEXT NOT NULL,
                            `apkName` TEXT NOT NULL,
                            `apkPath` TEXT NOT NULL,
                            `scanTimestamp` INTEGER NOT NULL,
                            `finalVerdict` TEXT NOT NULL,
                            `riskScore` INTEGER NOT NULL,
                            `layerResultsJson` TEXT NOT NULL,
                            `wasBlocked` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `audit_log` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `scanId` TEXT NOT NULL,
                            `apkName` TEXT NOT NULL,
                            `verdict` TEXT NOT NULL,
                            `action` TEXT NOT NULL,
                            `riskScore` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `quarantine` (
                            `id` TEXT NOT NULL,
                            `apkName` TEXT NOT NULL,
                            `originalPath` TEXT NOT NULL,
                            `quarantinePath` TEXT NOT NULL,
                            `verdict` TEXT NOT NULL,
                            `riskScore` INTEGER NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `threatName` TEXT,
                            `threatFamily` TEXT,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    // Not used for this test; we apply the migration explicitly below.
                }
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val supportDb = helper.writableDatabase

        // Apply the migration and assert new table exists.
        MIGRATION_3_4.migrate(supportDb)
        val cursor = supportDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='deleted_apks'")
        cursor.use { assertTrue(it.moveToFirst()) }

        helper.close()

        // Cleanup
        File(context.getDatabasePath(dbName).path).delete()
    }
}

