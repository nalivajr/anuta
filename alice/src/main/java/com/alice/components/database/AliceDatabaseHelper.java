package com.alice.components.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alice.tools.Alice;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AliceDatabaseHelper extends SQLiteOpenHelper {

    private List<Class> entityClasses = new LinkedList<Class>();

    public AliceDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AliceDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!entityClasses.isEmpty()) {
            createTablesForClasses(db, entityClasses);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    protected void createTablesForClasses(SQLiteDatabase db, List<Class> classes) {
        String sql = Alice.DatabaseTools.generateCreateTableScript(classes);
        db.beginTransaction();
        db.execSQL(sql);
        db.endTransaction();
    }

    public void setEntityClasses(List<Class> classes) {
        entityClasses.clear();
        if (classes != null) {
            entityClasses.addAll(classes);
        }
    }
}
