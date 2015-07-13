package com.alice.sample.database;

import android.database.sqlite.SQLiteDatabase;

import com.alice.components.database.helpers.AliceRelationalDatabaseHelper;
import com.alice.components.database.providers.AliceRelationalProvider;
import com.alice.sample.database.models.SubSubItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SampleProvider extends AliceRelationalProvider {
    @Override
    protected AliceRelationalDatabaseHelper createDatabaseHelper() {
        return new AliceRelationalDatabaseHelper(getContext(), "AliceSampleDB.db", null, Contract.DATABASE_VERSION) {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                deleteEntitiesTables(db);
                createTables(db);
            }
        };
    }

    @Override
    public <T> List<Class<T>> getEntityClasses() {
        List<Class<T>> classes = new ArrayList<Class<T>>();
        classes.add((Class<T>) SubSubItem.class);
        return classes;
    }
}
