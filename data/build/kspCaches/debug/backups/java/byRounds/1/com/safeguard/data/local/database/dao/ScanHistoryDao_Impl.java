package com.safeguard.data.local.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.safeguard.data.local.database.entity.ScanRecordEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class ScanHistoryDao_Impl implements ScanHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScanRecordEntity> __insertionAdapterOfScanRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public ScanHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScanRecordEntity = new EntityInsertionAdapter<ScanRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scan_history` (`id`,`apkHash`,`apkName`,`apkPath`,`scanTimestamp`,`finalVerdict`,`riskScore`,`layerResultsJson`,`wasBlocked`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanRecordEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getApkHash());
        statement.bindString(3, entity.getApkName());
        statement.bindString(4, entity.getApkPath());
        statement.bindLong(5, entity.getScanTimestamp());
        statement.bindString(6, entity.getFinalVerdict());
        statement.bindLong(7, entity.getRiskScore());
        statement.bindString(8, entity.getLayerResultsJson());
        final int _tmp = entity.getWasBlocked() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_history";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ScanRecordEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScanRecordEntity.insert(entity);
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
  public Object getById(final String id, final Continuation<? super ScanRecordEntity> $completion) {
    final String _sql = "SELECT * FROM scan_history WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScanRecordEntity>() {
      @Override
      @Nullable
      public ScanRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfApkHash = CursorUtil.getColumnIndexOrThrow(_cursor, "apkHash");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfApkPath = CursorUtil.getColumnIndexOrThrow(_cursor, "apkPath");
          final int _cursorIndexOfScanTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "scanTimestamp");
          final int _cursorIndexOfFinalVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "finalVerdict");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfLayerResultsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "layerResultsJson");
          final int _cursorIndexOfWasBlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "wasBlocked");
          final ScanRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpApkHash;
            _tmpApkHash = _cursor.getString(_cursorIndexOfApkHash);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpApkPath;
            _tmpApkPath = _cursor.getString(_cursorIndexOfApkPath);
            final long _tmpScanTimestamp;
            _tmpScanTimestamp = _cursor.getLong(_cursorIndexOfScanTimestamp);
            final String _tmpFinalVerdict;
            _tmpFinalVerdict = _cursor.getString(_cursorIndexOfFinalVerdict);
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final String _tmpLayerResultsJson;
            _tmpLayerResultsJson = _cursor.getString(_cursorIndexOfLayerResultsJson);
            final boolean _tmpWasBlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWasBlocked);
            _tmpWasBlocked = _tmp != 0;
            _result = new ScanRecordEntity(_tmpId,_tmpApkHash,_tmpApkName,_tmpApkPath,_tmpScanTimestamp,_tmpFinalVerdict,_tmpRiskScore,_tmpLayerResultsJson,_tmpWasBlocked);
          } else {
            _result = null;
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
  public Flow<List<ScanRecordEntity>> getAllFlow() {
    final String _sql = "SELECT * FROM scan_history ORDER BY scanTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_history"}, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfApkHash = CursorUtil.getColumnIndexOrThrow(_cursor, "apkHash");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfApkPath = CursorUtil.getColumnIndexOrThrow(_cursor, "apkPath");
          final int _cursorIndexOfScanTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "scanTimestamp");
          final int _cursorIndexOfFinalVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "finalVerdict");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfLayerResultsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "layerResultsJson");
          final int _cursorIndexOfWasBlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "wasBlocked");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpApkHash;
            _tmpApkHash = _cursor.getString(_cursorIndexOfApkHash);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpApkPath;
            _tmpApkPath = _cursor.getString(_cursorIndexOfApkPath);
            final long _tmpScanTimestamp;
            _tmpScanTimestamp = _cursor.getLong(_cursorIndexOfScanTimestamp);
            final String _tmpFinalVerdict;
            _tmpFinalVerdict = _cursor.getString(_cursorIndexOfFinalVerdict);
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final String _tmpLayerResultsJson;
            _tmpLayerResultsJson = _cursor.getString(_cursorIndexOfLayerResultsJson);
            final boolean _tmpWasBlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWasBlocked);
            _tmpWasBlocked = _tmp != 0;
            _item = new ScanRecordEntity(_tmpId,_tmpApkHash,_tmpApkName,_tmpApkPath,_tmpScanTimestamp,_tmpFinalVerdict,_tmpRiskScore,_tmpLayerResultsJson,_tmpWasBlocked);
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

  @Override
  public Object getRecent(final int limit,
      final Continuation<? super List<ScanRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM scan_history ORDER BY scanTimestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfApkHash = CursorUtil.getColumnIndexOrThrow(_cursor, "apkHash");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfApkPath = CursorUtil.getColumnIndexOrThrow(_cursor, "apkPath");
          final int _cursorIndexOfScanTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "scanTimestamp");
          final int _cursorIndexOfFinalVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "finalVerdict");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfLayerResultsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "layerResultsJson");
          final int _cursorIndexOfWasBlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "wasBlocked");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpApkHash;
            _tmpApkHash = _cursor.getString(_cursorIndexOfApkHash);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpApkPath;
            _tmpApkPath = _cursor.getString(_cursorIndexOfApkPath);
            final long _tmpScanTimestamp;
            _tmpScanTimestamp = _cursor.getLong(_cursorIndexOfScanTimestamp);
            final String _tmpFinalVerdict;
            _tmpFinalVerdict = _cursor.getString(_cursorIndexOfFinalVerdict);
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final String _tmpLayerResultsJson;
            _tmpLayerResultsJson = _cursor.getString(_cursorIndexOfLayerResultsJson);
            final boolean _tmpWasBlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWasBlocked);
            _tmpWasBlocked = _tmp != 0;
            _item = new ScanRecordEntity(_tmpId,_tmpApkHash,_tmpApkName,_tmpApkPath,_tmpScanTimestamp,_tmpFinalVerdict,_tmpRiskScore,_tmpLayerResultsJson,_tmpWasBlocked);
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
  public Object getAll(final Continuation<? super List<ScanRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM scan_history ORDER BY scanTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfApkHash = CursorUtil.getColumnIndexOrThrow(_cursor, "apkHash");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfApkPath = CursorUtil.getColumnIndexOrThrow(_cursor, "apkPath");
          final int _cursorIndexOfScanTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "scanTimestamp");
          final int _cursorIndexOfFinalVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "finalVerdict");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfLayerResultsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "layerResultsJson");
          final int _cursorIndexOfWasBlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "wasBlocked");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpApkHash;
            _tmpApkHash = _cursor.getString(_cursorIndexOfApkHash);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpApkPath;
            _tmpApkPath = _cursor.getString(_cursorIndexOfApkPath);
            final long _tmpScanTimestamp;
            _tmpScanTimestamp = _cursor.getLong(_cursorIndexOfScanTimestamp);
            final String _tmpFinalVerdict;
            _tmpFinalVerdict = _cursor.getString(_cursorIndexOfFinalVerdict);
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final String _tmpLayerResultsJson;
            _tmpLayerResultsJson = _cursor.getString(_cursorIndexOfLayerResultsJson);
            final boolean _tmpWasBlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWasBlocked);
            _tmpWasBlocked = _tmp != 0;
            _item = new ScanRecordEntity(_tmpId,_tmpApkHash,_tmpApkName,_tmpApkPath,_tmpScanTimestamp,_tmpFinalVerdict,_tmpRiskScore,_tmpLayerResultsJson,_tmpWasBlocked);
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
  public Object getCountSince(final long since, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM scan_history WHERE scanTimestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getBlockedCountSince(final long since,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM scan_history WHERE scanTimestamp >= ? AND wasBlocked = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
