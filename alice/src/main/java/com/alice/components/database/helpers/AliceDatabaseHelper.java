package com.alice.components.database.helpers;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.alice.components.database.models.Identifiable;
import com.alice.tools.Alice;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = AliceDatabaseHelper.class.getSimpleName();

    private List<Class<Identifiable>> entityClasses = new LinkedList<>();

    public AliceDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AliceDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * Generates script for creating tables for registered entities
     * @param classes entities classes
     */
    protected abstract  <T extends Identifiable> String getTableCreationScript(List<Class<T>> classes);

    /**
     * Creates tables which are required to store data
     */
    protected <T extends Identifiable> void createTablesForClasses(SQLiteDatabase db, List<Class<T>> classes) {
        String sql = getTableCreationScript(classes);
        executeScript(db, sql);
    }

    /**
     * Deletes tables for entities
     */
    protected <T extends Identifiable> void deleteEntitiesTables(SQLiteDatabase db) {
        String sql = Alice.databaseTools.generateTableDeletionScript(entityClasses);
        executeScript(db, sql);
    }

    protected void executeScript(SQLiteDatabase db, String sql) {
        try {
            db.beginTransaction();
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            Log.e(TAG, "Could not execute script", e);
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

    public void setEntityClasses(List<Class<Identifiable>> classes) {
        entityClasses.clear();
        if (classes != null) {
            Log.i(TAG, "Applying entity classes " + classes.toString());
            entityClasses.addAll(classes);
        }
    }
}
