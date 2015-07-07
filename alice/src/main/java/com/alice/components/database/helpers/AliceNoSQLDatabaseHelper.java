package com.alice.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import com.alice.components.database.models.Identifiable;
import com.alice.tools.Alice;

import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceNoSQLDatabaseHelper extends AliceDatabaseHelper {

    private static final String TAG = AliceNoSQLDatabaseHelper.class.getSimpleName();

    public AliceNoSQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AliceNoSQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    protected <T extends Identifiable> String getTableCreationScript(List<Class<T>> classes) {
        return Alice.databaseTools.generateNoSQLTableScript(classes);
    }
}
