package com.safeguard.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.dao.QuarantineDao;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.safeguard.data.local.database.dao.TrustedAppDao;
import com.safeguard.data.local.database.entity.AuditLogEntity;
import com.safeguard.data.local.database.entity.DeletedApkEntity;
import com.safeguard.data.local.database.entity.MalwareSignatureEntity;
import com.safeguard.data.local.database.entity.QuarantineRecordEntity;
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity;
import com.safeguard.data.local.database.entity.ScanRecordEntity;
import com.safeguard.data.local.database.entity.TrustedAppEntity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&J\b\u0010\r\u001a\u00020\u000eH&J\b\u0010\u000f\u001a\u00020\u0010H&\u00a8\u0006\u0011"}, d2 = {"Lcom/safeguard/data/local/database/SafeGuardDatabase;", "Landroidx/room/RoomDatabase;", "()V", "auditLogDao", "Lcom/safeguard/data/local/database/dao/AuditLogDao;", "deletedApkDao", "Lcom/safeguard/data/local/database/dao/DeletedApkDao;", "malwareSignatureDao", "Lcom/safeguard/data/local/database/dao/MalwareSignatureDao;", "quarantineDao", "Lcom/safeguard/data/local/database/dao/QuarantineDao;", "scanFeedbackEventDao", "Lcom/safeguard/data/local/database/dao/ScanFeedbackEventDao;", "scanHistoryDao", "Lcom/safeguard/data/local/database/dao/ScanHistoryDao;", "trustedAppDao", "Lcom/safeguard/data/local/database/dao/TrustedAppDao;", "data_debug"})
@androidx.room.Database(entities = {com.safeguard.data.local.database.entity.ScanRecordEntity.class, com.safeguard.data.local.database.entity.QuarantineRecordEntity.class, com.safeguard.data.local.database.entity.MalwareSignatureEntity.class, com.safeguard.data.local.database.entity.TrustedAppEntity.class, com.safeguard.data.local.database.entity.AuditLogEntity.class, com.safeguard.data.local.database.entity.DeletedApkEntity.class, com.safeguard.data.local.database.entity.ScanFeedbackEventEntity.class}, version = 6, exportSchema = false)
public abstract class SafeGuardDatabase extends androidx.room.RoomDatabase {
    
    public SafeGuardDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.ScanHistoryDao scanHistoryDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.QuarantineDao quarantineDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.MalwareSignatureDao malwareSignatureDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.TrustedAppDao trustedAppDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.AuditLogDao auditLogDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.DeletedApkDao deletedApkDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.local.database.dao.ScanFeedbackEventDao scanFeedbackEventDao();
}