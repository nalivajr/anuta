package com.alice.sample.database;

import android.database.sqlite.SQLiteDatabase;

import com.alice.components.database.helpers.AliceNoSQLDatabaseHelper;
import com.alice.components.database.providers.AliceNoSQLProvider;
import com.alice.sample.database.models.SubSubItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SampleProvider extends AliceNoSQLProvider {
    @Override
    protected AliceNoSQLDatabaseHelper createDatabaseHelper() {
        return new AliceNoSQLDatabaseHelper(getContext(), "AliceSampleDB.db", null, Contract.DATABASE_VERSION) {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                deleteEntitiesTables(db);
                createTables(db);
            }
        };
    }

    @Override
    public <T> List<Class<T>> getEntityClasses() {
        List<Class<T>> classes = new ArrayList<>();
        classes.add((Class<T>) SubSubItem.class);
        return classes;
    }
}
