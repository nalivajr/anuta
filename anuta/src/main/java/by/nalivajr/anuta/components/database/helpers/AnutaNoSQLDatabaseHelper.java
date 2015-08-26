package by.nalivajr.anuta.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com

 * @deprecated Not implemented totally, can be used only without relations
 */
@Deprecated
public abstract class AnutaNoSQLDatabaseHelper extends AnutaDatabaseHelper {

    private static final String TAG = AnutaNoSQLDatabaseHelper.class.getSimpleName();

    public AnutaNoSQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AnutaNoSQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    protected String getTableCreationScript(List<Class<?>> classes) {
        return Anuta.databaseTools.generateNoSQLTableScript(classes);
    }
}
