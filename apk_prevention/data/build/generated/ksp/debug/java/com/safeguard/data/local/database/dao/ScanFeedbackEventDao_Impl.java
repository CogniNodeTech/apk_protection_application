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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class ScanFeedbackEventDao_Impl implements ScanFeedbackEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScanFeedbackEventEntity> __insertionAdapterOfScanFeedbackEventEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public ScanFeedbackEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScanFeedbackEventEntity = new EntityInsertionAdapter<ScanFeedbackEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `scan_feedback_queue` (`id`,`createdAtMs`,`sha256`,`verdict`,`confidence`,`packageName`,`versionCode`,`layerScoresJson`,`triggeredRulesJson`,`androidApiLevel`,`appVersionCode`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanFeedbackEventEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getCreatedAtMs());
        statement.bindString(3, entity.getSha256());
        statement.bindString(4, entity.getVerdict());
        statement.bindDouble(5, entity.getConfidence());
        if (entity.getPackageName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPackageName());
        }
        if (entity.getVersionCode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getVersionCode());
        }
        statement.bindString(8, entity.getLayerScoresJson());
        statement.bindString(9, entity.getTriggeredRulesJson());
        statement.bindLong(10, entity.getAndroidApiLevel());
        statement.bindLong(11, entity.getAppVersionCode());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_feedback_queue";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ScanFeedbackEventEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScanFeedbackEventEntity.insert(entity);
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
  public Object nextBatch(final int limit,
      final Continuation<? super List<ScanFeedbackEventEntity>> $completion) {
    final String _sql = "SELECT * FROM scan_feedback_queue ORDER BY createdAtMs ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScanFeedbackEventEntity>>() {
      @Override
      @NonNull
      public List<ScanFeedbackEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final int _cursorIndexOfVerdict = CursorUtil.getColumnIndexOrThrow(_cursor, "verdict");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfVersionCode = CursorUtil.getColumnIndexOrThrow(_cursor, "versionCode");
          final int _cursorIndexOfLayerScoresJson = CursorUtil.getColumnIndexOrThrow(_cursor, "layerScoresJson");
          final int _cursorIndexOfTriggeredRulesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredRulesJson");
          final int _cursorIndexOfAndroidApiLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "androidApiLevel");
          final int _cursorIndexOfAppVersionCode = CursorUtil.getColumnIndexOrThrow(_cursor, "appVersionCode");
          final List<ScanFeedbackEventEntity> _result = new ArrayList<ScanFeedbackEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanFeedbackEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final String _tmpSha256;
            _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            final String _tmpVerdict;
            _tmpVerdict = _cursor.getString(_cursorIndexOfVerdict);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final Integer _tmpVersionCode;
            if (_cursor.isNull(_cursorIndexOfVersionCode)) {
              _tmpVersionCode = null;
            } else {
              _tmpVersionCode = _cursor.getInt(_cursorIndexOfVersionCode);
            }
            final String _tmpLayerScoresJson;
            _tmpLayerScoresJson = _cursor.getString(_cursorIndexOfLayerScoresJson);
            final String _tmpTriggeredRulesJson;
            _tmpTriggeredRulesJson = _cursor.getString(_cursorIndexOfTriggeredRulesJson);
            final int _tmpAndroidApiLevel;
            _tmpAndroidApiLevel = _cursor.getInt(_cursorIndexOfAndroidApiLevel);
            final int _tmpAppVersionCode;
            _tmpAppVersionCode = _cursor.getInt(_cursorIndexOfAppVersionCode);
            _item = new ScanFeedbackEventEntity(_tmpId,_tmpCreatedAtMs,_tmpSha256,_tmpVerdict,_tmpConfidence,_tmpPackageName,_tmpVersionCode,_tmpLayerScoresJson,_tmpTriggeredRulesJson,_tmpAndroidApiLevel,_tmpAppVersionCode);
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM scan_feedback_queue";
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
  public Object deleteByIds(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM scan_feedback_queue WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
