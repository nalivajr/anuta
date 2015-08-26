package by.nalivajr.anuta.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = AnutaDatabaseHelper.class.getSimpleName();

    private List<Class<?>> entityClasses = new LinkedList<Class<?>>();

    public AnutaDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AnutaDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * Generates script for creating tables for registered entities
     * @param classes entities classes
     */
    protected abstract String getTableCreationScript(List<Class<?>> classes);

    /**
     * Creates tables which are required to store data
     */
    protected void createTablesForClasses(SQLiteDatabase db, List<Class<?>> classes) {
        String sql = getTableCreationScript(classes);
        executeScript(db, sql);
    }

    /**
     * Deletes tables for entities
     */
    protected void deleteEntitiesTables(SQLiteDatabase db) {
        String sql = Anuta.databaseTools.generateTableDeletionScript(entityClasses);
        executeScript(db, sql);
    }

    protected void executeScript(SQLiteDatabase db, String sql) {
        try {
            db.beginTransaction();
            String[] commands = sql.split(";");
            for (String command: commands) {
                db.execSQL(command);
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            Log.e(TAG, "Could not execute script", e);
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    protected void createTables(SQLiteDatabase db) {
        if (!entityClasses.isEmpty()) {
            createTablesForClasses(db, entityClasses);
        }
    }

    public void setEntityClasses(List<Class<?>> classes) {
        entityClasses.clear();
        if (classes != null) {
            Log.i(TAG, "Applying entity classes " + classes.toString());
            entityClasses.addAll(classes);
        }
    }
}
