package by.nalivajr.anuta.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaRelationalDatabaseHelper extends AnutaDatabaseHelper {

    private static final String TAG = AnutaRelationalDatabaseHelper.class.getSimpleName();

    public AnutaRelationalDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AnutaRelationalDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    protected String getTableCreationScript(List<Class<?>> classes) {
        return Anuta.databaseTools.generateRelationalTableScript(classes);
    }
}
