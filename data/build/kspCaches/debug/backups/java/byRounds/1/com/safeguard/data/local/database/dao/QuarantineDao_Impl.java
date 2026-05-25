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
import com.safeguard.data.local.database.entity.QuarantineRecordEntity;
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
public final class QuarantineDao_Impl implements QuarantineDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<QuarantineRecordEntity> __insertionAdapterOfQuarantineRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByApkName;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public QuarantineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfQuarantineRecordEntity = new EntityInsertionAdapter<QuarantineRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `quarantine` (`id`,`originalPath`,`quarantinePath`,`apkHash`,`threatName`,`apkName`,`riskScore`,`quarantineTimestamp`,`autoDeleteAt`,`sizeBytes`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QuarantineRecordEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getOriginalPath());
        statement.bindString(3, entity.getQuarantinePath());
        statement.bindString(4, entity.getApkHash());
        if (entity.getThreatName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getThreatName());
        }
        if (entity.getApkName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getApkName());
        }
        statement.bindLong(7, entity.getRiskScore());
        statement.bindLong(8, entity.getQuarantineTimestamp());
        statement.bindLong(9, entity.getAutoDeleteAt());
        statement.bindLong(10, entity.getSizeBytes());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM quarantine WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByApkName = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM quarantine WHERE apkName = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM quarantine";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final QuarantineRecordEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQuarantineRecordEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByApkName(final String apkName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByApkName.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, apkName);
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
          __preparedStmtOfDeleteByApkName.release(_stmt);
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
  public Flow<List<QuarantineRecordEntity>> getAllFlow() {
    final String _sql = "SELECT * FROM quarantine ORDER BY quarantineTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"quarantine"}, new Callable<List<QuarantineRecordEntity>>() {
      @Override
      @NonNull
      public List<QuarantineRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
          final int _cursorIndexOfQuarantinePath = CursorUtil.getColumnIndexOrThrow(_cursor, "quarantinePath");
          final int _cursorIndexOfApkHash = CursorUtil.getColumnIndexOrThrow(_cursor, "apkHash");
          final int _cursorIndexOfThreatName = CursorUtil.getColumnIndexOrThrow(_cursor, "threatName");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfQuarantineTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "quarantineTimestamp");
          final int _cursorIndexOfAutoDeleteAt = CursorUtil.getColumnIndexOrThrow(_cursor, "autoDeleteAt");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final List<QuarantineRecordEntity> _result = new ArrayList<QuarantineRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QuarantineRecordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOriginalPath;
            _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
            final String _tmpQuarantinePath;
            _tmpQuarantinePath = _cursor.getString(_cursorIndexOfQuarantinePath);
            final String _tmpApkHash;
            _tmpApkHash = _cursor.getString(_cursorIndexOfApkHash);
            final String _tmpThreatName;
            if (_cursor.isNull(_cursorIndexOfThreatName)) {
              _tmpThreatName = null;
            } else {
              _tmpThreatName = _cursor.getString(_cursorIndexOfThreatName);
            }
            final String _tmpApkName;
            if (_cursor.isNull(_cursorIndexOfApkName)) {
              _tmpApkName = null;
            } else {
              _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            }
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final long _tmpQuarantineTimestamp;
            _tmpQuarantineTimestamp = _cursor.getLong(_cursorIndexOfQuarantineTimestamp);
            final long _tmpAutoDeleteAt;
            _tmpAutoDeleteAt = _cursor.getLong(_cursorIndexOfAutoDeleteAt);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            _item = new QuarantineRecordEntity(_tmpId,_tmpOriginalPath,_tmpQuarantinePath,_tmpApkHash,_tmpThreatName,_tmpApkName,_tmpRiskScore,_tmpQuarantineTimestamp,_tmpAutoDeleteAt,_tmpSizeBytes);
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
  public Object getById(final String id,
      final Continuation<? super QuarantineRecordEntity> $completion) {
    final String _sql = "SELECT * FROM quarantine WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<QuarantineRecordEntity>() {
      @Override
      @Nullable
      public QuarantineRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
          final int _cursorIndexOfQuarantinePath = CursorUtil.getColumnIndexOrThrow(_cursor, "quarantinePath");
          final int _cursorIndexOfApkHash = CursorUtil.getColumnIndexOrThrow(_cursor, "apkHash");
          final int _cursorIndexOfThreatName = CursorUtil.getColumnIndexOrThrow(_cursor, "threatName");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfQuarantineTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "quarantineTimestamp");
          final int _cursorIndexOfAutoDeleteAt = CursorUtil.getColumnIndexOrThrow(_cursor, "autoDeleteAt");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final QuarantineRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOriginalPath;
            _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
            final String _tmpQuarantinePath;
            _tmpQuarantinePath = _cursor.getString(_cursorIndexOfQuarantinePath);
            final String _tmpApkHash;
            _tmpApkHash = _cursor.getString(_cursorIndexOfApkHash);
            final String _tmpThreatName;
            if (_cursor.isNull(_cursorIndexOfThreatName)) {
              _tmpThreatName = null;
            } else {
              _tmpThreatName = _cursor.getString(_cursorIndexOfThreatName);
            }
            final String _tmpApkName;
            if (_cursor.isNull(_cursorIndexOfApkName)) {
              _tmpApkName = null;
            } else {
              _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            }
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final long _tmpQuarantineTimestamp;
            _tmpQuarantineTimestamp = _cursor.getLong(_cursorIndexOfQuarantineTimestamp);
            final long _tmpAutoDeleteAt;
            _tmpAutoDeleteAt = _cursor.getLong(_cursorIndexOfAutoDeleteAt);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            _result = new QuarantineRecordEntity(_tmpId,_tmpOriginalPath,_tmpQuarantinePath,_tmpApkHash,_tmpThreatName,_tmpApkName,_tmpRiskScore,_tmpQuarantineTimestamp,_tmpAutoDeleteAt,_tmpSizeBytes);
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
  public Object getCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM quarantine";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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
  public Object getAutoDeleteCount(final long before,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM quarantine WHERE autoDeleteAt <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, before);
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
