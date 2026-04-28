package com.safeguard.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.AuditLogDao_Impl;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.DeletedApkDao_Impl;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.dao.MalwareSignatureDao_Impl;
import com.safeguard.data.local.database.dao.QuarantineDao;
import com.safeguard.data.local.database.dao.QuarantineDao_Impl;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao_Impl;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao_Impl;
import com.safeguard.data.local.database.dao.TrustedAppDao;
import com.safeguard.data.local.database.dao.TrustedAppDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SafeGuardDatabase_Impl extends SafeGuardDatabase {
  private volatile ScanHistoryDao _scanHistoryDao;

  private volatile QuarantineDao _quarantineDao;

  private volatile MalwareSignatureDao _malwareSignatureDao;

  private volatile TrustedAppDao _trustedAppDao;

  private volatile AuditLogDao _auditLogDao;

  private volatile DeletedApkDao _deletedApkDao;

  private volatile ScanFeedbackEventDao _scanFeedbackEventDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(6) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `scan_history` (`id` TEXT NOT NULL, `apkHash` TEXT NOT NULL, `apkName` TEXT NOT NULL, `apkPath` TEXT NOT NULL, `scanTimestamp` INTEGER NOT NULL, `finalVerdict` TEXT NOT NULL, `riskScore` INTEGER NOT NULL, `layerResultsJson` TEXT NOT NULL, `wasBlocked` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `quarantine` (`id` TEXT NOT NULL, `originalPath` TEXT NOT NULL, `quarantinePath` TEXT NOT NULL, `apkHash` TEXT NOT NULL, `threatName` TEXT, `apkName` TEXT, `riskScore` INTEGER NOT NULL, `quarantineTimestamp` INTEGER NOT NULL, `autoDeleteAt` INTEGER NOT NULL, `sizeBytes` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `malware_signatures` (`sha256` TEXT NOT NULL, `sha512` TEXT, `fuzzyHash` TEXT, `threatName` TEXT NOT NULL, `threatFamily` TEXT, `severity` INTEGER NOT NULL, `firstSeen` INTEGER, `source` TEXT, PRIMARY KEY(`sha256`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_malware_signatures_fuzzyHash` ON `malware_signatures` (`fuzzyHash`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `trusted_apps` (`id` TEXT NOT NULL, `sha256` TEXT NOT NULL, `packageName` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, `expiresAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trusted_apps_sha256` ON `trusted_apps` (`sha256`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `audit_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `scanId` TEXT NOT NULL, `apkName` TEXT NOT NULL, `verdict` TEXT NOT NULL, `action` TEXT NOT NULL, `riskScore` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `deleted_apks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `apkName` TEXT NOT NULL, `apkSha256` TEXT, `originalPath` TEXT NOT NULL, `threatName` TEXT, `riskScore` INTEGER NOT NULL, `deletedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `scan_feedback_queue` (`id` TEXT NOT NULL, `createdAtMs` INTEGER NOT NULL, `sha256` TEXT NOT NULL, `verdict` TEXT NOT NULL, `confidence` REAL NOT NULL, `packageName` TEXT, `versionCode` INTEGER, `layerScoresJson` TEXT NOT NULL, `triggeredRulesJson` TEXT NOT NULL, `androidApiLevel` INTEGER NOT NULL, `appVersionCode` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scan_feedback_queue_createdAtMs` ON `scan_feedback_queue` (`createdAtMs`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4d42a92c1ff302ff407e0f6b763c6f64')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `scan_history`");
        db.execSQL("DROP TABLE IF EXISTS `quarantine`");
        db.execSQL("DROP TABLE IF EXISTS `malware_signatures`");
        db.execSQL("DROP TABLE IF EXISTS `trusted_apps`");
        db.execSQL("DROP TABLE IF EXISTS `audit_log`");
        db.execSQL("DROP TABLE IF EXISTS `deleted_apks`");
        db.execSQL("DROP TABLE IF EXISTS `scan_feedback_queue`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsScanHistory = new HashMap<String, TableInfo.Column>(9);
        _columnsScanHistory.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("apkHash", new TableInfo.Column("apkHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("apkName", new TableInfo.Column("apkName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("apkPath", new TableInfo.Column("apkPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("scanTimestamp", new TableInfo.Column("scanTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("finalVerdict", new TableInfo.Column("finalVerdict", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("riskScore", new TableInfo.Column("riskScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("layerResultsJson", new TableInfo.Column("layerResultsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("wasBlocked", new TableInfo.Column("wasBlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScanHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScanHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScanHistory = new TableInfo("scan_history", _columnsScanHistory, _foreignKeysScanHistory, _indicesScanHistory);
        final TableInfo _existingScanHistory = TableInfo.read(db, "scan_history");
        if (!_infoScanHistory.equals(_existingScanHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "scan_history(com.safeguard.data.local.database.entity.ScanRecordEntity).\n"
                  + " Expected:\n" + _infoScanHistory + "\n"
                  + " Found:\n" + _existingScanHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsQuarantine = new HashMap<String, TableInfo.Column>(10);
        _columnsQuarantine.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("originalPath", new TableInfo.Column("originalPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("quarantinePath", new TableInfo.Column("quarantinePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("apkHash", new TableInfo.Column("apkHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("threatName", new TableInfo.Column("threatName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("apkName", new TableInfo.Column("apkName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("riskScore", new TableInfo.Column("riskScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("quarantineTimestamp", new TableInfo.Column("quarantineTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("autoDeleteAt", new TableInfo.Column("autoDeleteAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuarantine.put("sizeBytes", new TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuarantine = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQuarantine = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQuarantine = new TableInfo("quarantine", _columnsQuarantine, _foreignKeysQuarantine, _indicesQuarantine);
        final TableInfo _existingQuarantine = TableInfo.read(db, "quarantine");
        if (!_infoQuarantine.equals(_existingQuarantine)) {
          return new RoomOpenHelper.ValidationResult(false, "quarantine(com.safeguard.data.local.database.entity.QuarantineRecordEntity).\n"
                  + " Expected:\n" + _infoQuarantine + "\n"
                  + " Found:\n" + _existingQuarantine);
        }
        final HashMap<String, TableInfo.Column> _columnsMalwareSignatures = new HashMap<String, TableInfo.Column>(8);
        _columnsMalwareSignatures.put("sha256", new TableInfo.Column("sha256", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("sha512", new TableInfo.Column("sha512", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("fuzzyHash", new TableInfo.Column("fuzzyHash", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("threatName", new TableInfo.Column("threatName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("threatFamily", new TableInfo.Column("threatFamily", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("severity", new TableInfo.Column("severity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("firstSeen", new TableInfo.Column("firstSeen", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMalwareSignatures.put("source", new TableInfo.Column("source", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMalwareSignatures = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMalwareSignatures = new HashSet<TableInfo.Index>(1);
        _indicesMalwareSignatures.add(new TableInfo.Index("index_malware_signatures_fuzzyHash", false, Arrays.asList("fuzzyHash"), Arrays.asList("ASC")));
        final TableInfo _infoMalwareSignatures = new TableInfo("malware_signatures", _columnsMalwareSignatures, _foreignKeysMalwareSignatures, _indicesMalwareSignatures);
        final TableInfo _existingMalwareSignatures = TableInfo.read(db, "malware_signatures");
        if (!_infoMalwareSignatures.equals(_existingMalwareSignatures)) {
          return new RoomOpenHelper.ValidationResult(false, "malware_signatures(com.safeguard.data.local.database.entity.MalwareSignatureEntity).\n"
                  + " Expected:\n" + _infoMalwareSignatures + "\n"
                  + " Found:\n" + _existingMalwareSignatures);
        }
        final HashMap<String, TableInfo.Column> _columnsTrustedApps = new HashMap<String, TableInfo.Column>(5);
        _columnsTrustedApps.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrustedApps.put("sha256", new TableInfo.Column("sha256", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrustedApps.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrustedApps.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrustedApps.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrustedApps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTrustedApps = new HashSet<TableInfo.Index>(1);
        _indicesTrustedApps.add(new TableInfo.Index("index_trusted_apps_sha256", false, Arrays.asList("sha256"), Arrays.asList("ASC")));
        final TableInfo _infoTrustedApps = new TableInfo("trusted_apps", _columnsTrustedApps, _foreignKeysTrustedApps, _indicesTrustedApps);
        final TableInfo _existingTrustedApps = TableInfo.read(db, "trusted_apps");
        if (!_infoTrustedApps.equals(_existingTrustedApps)) {
          return new RoomOpenHelper.ValidationResult(false, "trusted_apps(com.safeguard.data.local.database.entity.TrustedAppEntity).\n"
                  + " Expected:\n" + _infoTrustedApps + "\n"
                  + " Found:\n" + _existingTrustedApps);
        }
        final HashMap<String, TableInfo.Column> _columnsAuditLog = new HashMap<String, TableInfo.Column>(7);
        _columnsAuditLog.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLog.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLog.put("scanId", new TableInfo.Column("scanId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLog.put("apkName", new TableInfo.Column("apkName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLog.put("verdict", new TableInfo.Column("verdict", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLog.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLog.put("riskScore", new TableInfo.Column("riskScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAuditLog = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAuditLog = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAuditLog = new TableInfo("audit_log", _columnsAuditLog, _foreignKeysAuditLog, _indicesAuditLog);
        final TableInfo _existingAuditLog = TableInfo.read(db, "audit_log");
        if (!_infoAuditLog.equals(_existingAuditLog)) {
          return new RoomOpenHelper.ValidationResult(false, "audit_log(com.safeguard.data.local.database.entity.AuditLogEntity).\n"
                  + " Expected:\n" + _infoAuditLog + "\n"
                  + " Found:\n" + _existingAuditLog);
        }
        final HashMap<String, TableInfo.Column> _columnsDeletedApks = new HashMap<String, TableInfo.Column>(7);
        _columnsDeletedApks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletedApks.put("apkName", new TableInfo.Column("apkName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletedApks.put("apkSha256", new TableInfo.Column("apkSha256", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletedApks.put("originalPath", new TableInfo.Column("originalPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletedApks.put("threatName", new TableInfo.Column("threatName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletedApks.put("riskScore", new TableInfo.Column("riskScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletedApks.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeletedApks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeletedApks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeletedApks = new TableInfo("deleted_apks", _columnsDeletedApks, _foreignKeysDeletedApks, _indicesDeletedApks);
        final TableInfo _existingDeletedApks = TableInfo.read(db, "deleted_apks");
        if (!_infoDeletedApks.equals(_existingDeletedApks)) {
          return new RoomOpenHelper.ValidationResult(false, "deleted_apks(com.safeguard.data.local.database.entity.DeletedApkEntity).\n"
                  + " Expected:\n" + _infoDeletedApks + "\n"
                  + " Found:\n" + _existingDeletedApks);
        }
        final HashMap<String, TableInfo.Column> _columnsScanFeedbackQueue = new HashMap<String, TableInfo.Column>(11);
        _columnsScanFeedbackQueue.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("createdAtMs", new TableInfo.Column("createdAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("sha256", new TableInfo.Column("sha256", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("verdict", new TableInfo.Column("verdict", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("packageName", new TableInfo.Column("packageName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("versionCode", new TableInfo.Column("versionCode", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("layerScoresJson", new TableInfo.Column("layerScoresJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("triggeredRulesJson", new TableInfo.Column("triggeredRulesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("androidApiLevel", new TableInfo.Column("androidApiLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanFeedbackQueue.put("appVersionCode", new TableInfo.Column("appVersionCode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScanFeedbackQueue = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScanFeedbackQueue = new HashSet<TableInfo.Index>(1);
        _indicesScanFeedbackQueue.add(new TableInfo.Index("index_scan_feedback_queue_createdAtMs", false, Arrays.asList("createdAtMs"), Arrays.asList("ASC")));
        final TableInfo _infoScanFeedbackQueue = new TableInfo("scan_feedback_queue", _columnsScanFeedbackQueue, _foreignKeysScanFeedbackQueue, _indicesScanFeedbackQueue);
        final TableInfo _existingScanFeedbackQueue = TableInfo.read(db, "scan_feedback_queue");
        if (!_infoScanFeedbackQueue.equals(_existingScanFeedbackQueue)) {
          return new RoomOpenHelper.ValidationResult(false, "scan_feedback_queue(com.safeguard.data.local.database.entity.ScanFeedbackEventEntity).\n"
                  + " Expected:\n" + _infoScanFeedbackQueue + "\n"
                  + " Found:\n" + _existingScanFeedbackQueue);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4d42a92c1ff302ff407e0f6b763c6f64", "0343403af06be28aad149af420825d7a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "scan_history","quarantine","malware_signatures","trusted_apps","audit_log","deleted_apks","scan_feedback_queue");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `scan_history`");
      _db.execSQL("DELETE FROM `quarantine`");
      _db.execSQL("DELETE FROM `malware_signatures`");
      _db.execSQL("DELETE FROM `trusted_apps`");
      _db.execSQL("DELETE FROM `audit_log`");
      _db.execSQL("DELETE FROM `deleted_apks`");
      _db.execSQL("DELETE FROM `scan_feedback_queue`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ScanHistoryDao.class, ScanHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QuarantineDao.class, QuarantineDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MalwareSignatureDao.class, MalwareSignatureDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrustedAppDao.class, TrustedAppDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AuditLogDao.class, AuditLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DeletedApkDao.class, DeletedApkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScanFeedbackEventDao.class, ScanFeedbackEventDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ScanHistoryDao scanHistoryDao() {
    if (_scanHistoryDao != null) {
      return _scanHistoryDao;
    } else {
      synchronized(this) {
        if(_scanHistoryDao == null) {
          _scanHistoryDao = new ScanHistoryDao_Impl(this);
        }
        return _scanHistoryDao;
      }
    }
  }

  @Override
  public QuarantineDao quarantineDao() {
    if (_quarantineDao != null) {
      return _quarantineDao;
    } else {
      synchronized(this) {
        if(_quarantineDao == null) {
          _quarantineDao = new QuarantineDao_Impl(this);
        }
        return _quarantineDao;
      }
    }
  }

  @Override
  public MalwareSignatureDao malwareSignatureDao() {
    if (_malwareSignatureDao != null) {
      return _malwareSignatureDao;
    } else {
      synchronized(this) {
        if(_malwareSignatureDao == null) {
          _malwareSignatureDao = new MalwareSignatureDao_Impl(this);
        }
        return _malwareSignatureDao;
      }
    }
  }

  @Override
  public TrustedAppDao trustedAppDao() {
    if (_trustedAppDao != null) {
      return _trustedAppDao;
    } else {
      synchronized(this) {
        if(_trustedAppDao == null) {
          _trustedAppDao = new TrustedAppDao_Impl(this);
        }
        return _trustedAppDao;
      }
    }
  }

  @Override
  public AuditLogDao auditLogDao() {
    if (_auditLogDao != null) {
      return _auditLogDao;
    } else {
      synchronized(this) {
        if(_auditLogDao == null) {
          _auditLogDao = new AuditLogDao_Impl(this);
        }
        return _auditLogDao;
      }
    }
  }

  @Override
  public DeletedApkDao deletedApkDao() {
    if (_deletedApkDao != null) {
      return _deletedApkDao;
    } else {
      synchronized(this) {
        if(_deletedApkDao == null) {
          _deletedApkDao = new DeletedApkDao_Impl(this);
        }
        return _deletedApkDao;
      }
    }
  }

  @Override
  public ScanFeedbackEventDao scanFeedbackEventDao() {
    if (_scanFeedbackEventDao != null) {
      return _scanFeedbackEventDao;
    } else {
      synchronized(this) {
        if(_scanFeedbackEventDao == null) {
          _scanFeedbackEventDao = new ScanFeedbackEventDao_Impl(this);
        }
        return _scanFeedbackEventDao;
      }
    }
  }
}
