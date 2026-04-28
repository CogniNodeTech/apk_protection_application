package com.safeguard.data.local.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.safeguard.data.local.database.entity.AuditLogEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AuditLogDao_Impl implements AuditLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AuditLogEntity> __insertionAdapterOfAuditLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public AuditLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAuditLogEntity = new EntityInsertionAdapter<AuditLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `audit_log` (`id`,`timestamp`,`scanId`,`apkName`,`verdict`,`action`,`riskScore`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AuditLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getScanId());
        statement.bindString(4, entity.getApkName());
        statement.bindString(5, entity.getVerdict());
        statement.bindString(6, entity.getAction());
        statement.bindLong(7, entity.getRiskScore());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM audit_log";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AuditLogEntity entity, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAuditLogEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecent(final int limit,
      final Continuation<? super List<AuditLogEntity>> $completion) {
    final String _sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AuditLogEntity>>() {
      @Override
      @NonNull
      public List<AuditLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfScanId = CursorUtil.getColumnIndexOrThrow(_cursor, "scanId");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "verdict");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final List<AuditLogEntity> _result = new ArrayList<AuditLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpScanId;
            _tmpScanId = _cursor.getString(_cursorIndexOfScanId);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpVerdict;
            _tmpVerdict = _cursor.getString(_cursorIndexOfVerdict);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            _item = new AuditLogEntity(_tmpId,_tmpTimestamp,_tmpScanId,_tmpApkName,_tmpVerdict,_tmpAction,_tmpRiskScore);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AuditLogEntity>> getAllFlow() {
    final String _sql = "SELECT * FROM audit_log ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_log"}, new Callable<List<AuditLogEntity>>() {
      @Override
      @NonNull
      public List<AuditLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfScanId = CursorUtil.getColumnIndexOrThrow(_cursor, "scanId");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "verdict");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final List<AuditLogEntity> _result = new ArrayList<AuditLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpScanId;
            _tmpScanId = _cursor.getString(_cursorIndexOfScanId);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpVerdict;
            _tmpVerdict = _cursor.getString(_cursorIndexOfVerdict);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            _item = new AuditLogEntity(_tmpId,_tmpTimestamp,_tmpScanId,_tmpApkName,_tmpVerdict,_tmpAction,_tmpRiskScore);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
