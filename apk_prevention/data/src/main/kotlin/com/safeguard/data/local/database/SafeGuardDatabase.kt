package com.safeguard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safeguard.data.local.database.dao.AuditLogDao
import com.safeguard.data.local.database.dao.DeletedApkDao
import com.safeguard.data.local.database.dao.MalwareSignatureDao
import com.safeguard.data.local.database.dao.QuarantineDao
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao
import com.safeguard.data.local.database.dao.ScanHistoryDao
import com.safeguard.data.local.database.dao.TrustedAppDao
import com.safeguard.data.local.database.entity.AuditLogEntity
import com.safeguard.data.local.database.entity.DeletedApkEntity
import com.safeguard.data.local.database.entity.MalwareSignatureEntity
import com.safeguard.data.local.database.entity.QuarantineRecordEntity
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity
import com.safeguard.data.local.database.entity.ScanRecordEntity
import com.safeguard.data.local.database.entity.TrustedAppEntity

@Database(
    entities = [
        ScanRecordEntity::class,
        QuarantineRecordEntity::class,
        MalwareSignatureEntity::class,
        TrustedAppEntity::class,
        AuditLogEntity::class,
        DeletedApkEntity::class,
        ScanFeedbackEventEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SafeGuardDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun quarantineDao(): QuarantineDao
    abstract fun malwareSignatureDao(): MalwareSignatureDao
    abstract fun trustedAppDao(): TrustedAppDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun deletedApkDao(): DeletedApkDao
    abstract fun scanFeedbackEventDao(): ScanFeedbackEventDao
}
