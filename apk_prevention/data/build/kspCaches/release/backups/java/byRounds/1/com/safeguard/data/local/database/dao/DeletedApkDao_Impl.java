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
import com.safeguard.data.local.database.entity.DeletedApkEntity;
import java.lang.Boolean;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DeletedApkDao_Impl implements DeletedApkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DeletedApkEntity> __insertionAdapterOfDeletedApkEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public DeletedApkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeletedApkEntity = new EntityInsertionAdapter<DeletedApkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `deleted_apks` (`id`,`apkName`,`apkSha256`,`originalPath`,`threatName`,`riskScore`,`deletedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeletedApkEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getApkName());
        if (entity.getApkSha256() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getApkSha256());
        }
        statement.bindString(4, entity.getOriginalPath());
        if (entity.getThreatName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getThreatName());
        }
        statement.bindLong(6, entity.getRiskScore());
        statement.bindLong(7, entity.getDeletedAt());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM deleted_apks";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final DeletedApkEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDeletedApkEntity.insert(entity);
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
  public Object isApkBlocked(final String apkName,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT COUNT(*) > 0 FROM deleted_apks WHERE apkName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, apkName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
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
  public Object isApkHashBlocked(final String apkSha256,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT COUNT(*) > 0 FROM deleted_apks WHERE apkSha256 = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, apkSha256);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
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
  public Object getAll(final Continuation<? super List<DeletedApkEntity>> $completion) {
    final String _sql = "SELECT * FROM deleted_apks ORDER BY deletedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DeletedApkEntity>>() {
      @Override
      @NonNull
      public List<DeletedApkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfApkName = CursorUtil.getColumnIndexOrThrow(_cursor, "apkName");
          final int _cursorIndexOfApkSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "apkSha256");
          final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
          final int _cursorIndexOfThreatName = CursorUtil.getColumnIndexOrThrow(_cursor, "threatName");
          final int _cursorIndexOfRiskScore = CursorUtil.getColumnIndexOrThrow(_cursor, "riskScore");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final List<DeletedApkEntity> _result = new ArrayList<DeletedApkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeletedApkEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpApkName;
            _tmpApkName = _cursor.getString(_cursorIndexOfApkName);
            final String _tmpApkSha256;
            if (_cursor.isNull(_cursorIndexOfApkSha256)) {
              _tmpApkSha256 = null;
            } else {
              _tmpApkSha256 = _cursor.getString(_cursorIndexOfApkSha256);
            }
            final String _tmpOriginalPath;
            _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
            final String _tmpThreatName;
            if (_cursor.isNull(_cursorIndexOfThreatName)) {
              _tmpThreatName = null;
            } else {
              _tmpThreatName = _cursor.getString(_cursorIndexOfThreatName);
            }
            final int _tmpRiskScore;
            _tmpRiskScore = _cursor.getInt(_cursorIndexOfRiskScore);
            final long _tmpDeletedAt;
            _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            _item = new DeletedApkEntity(_tmpId,_tmpApkName,_tmpApkSha256,_tmpOriginalPath,_tmpThreatName,_tmpRiskScore,_tmpDeletedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
