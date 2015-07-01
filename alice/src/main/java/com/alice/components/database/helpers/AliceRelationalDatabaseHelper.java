package com.alice.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alice.components.database.models.Identifiable;
import com.alice.tools.Alice;

import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceRelationalDatabaseHelper extends AliceDatabaseHelper {

    private static final String TAG = AliceRelationalDatabaseHelper.class.getSimpleName();

    public AliceRelationalDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AliceRelationalDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    protected <T extends Identifiable> void createTablesForClasses(SQLiteDatabase db, List<Class<T>> classes) {
        String sql = Alice.DatabaseTools.generateRelationalTableScript(classes);
        try {
            db.beginTransaction();
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            Log.e(TAG, "Could not create tables", e);
        } finally {
            db.endTransaction();
        }
    }
}
