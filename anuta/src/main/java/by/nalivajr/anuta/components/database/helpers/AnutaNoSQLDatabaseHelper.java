package by.nalivajr.anuta.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import by.nalivajr.anuta.components.database.models.Identifiable;
import by.nalivajr.anuta.tools.Anuta;

import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaNoSQLDatabaseHelper extends AnutaDatabaseHelper {

    private static final String TAG = AnutaNoSQLDatabaseHelper.class.getSimpleName();

    public AnutaNoSQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AnutaNoSQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    protected <T extends Identifiable> String getTableCreationScript(List<Class<T>> classes) {
        return Anuta.databaseTools.generateNoSQLTableScript(classes);
    }
}
