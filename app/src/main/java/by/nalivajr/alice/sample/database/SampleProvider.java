package by.nalivajr.alice.sample.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import by.nalivajr.alice.components.database.helpers.AliceRelationalDatabaseHelper;
import by.nalivajr.alice.components.database.providers.AliceRelationalProvider;
import by.nalivajr.alice.sample.database.models.Game;
import by.nalivajr.alice.sample.database.models.Group;
import by.nalivajr.alice.sample.database.models.Item;
import by.nalivajr.alice.sample.database.models.SubSubItem;
import by.nalivajr.alice.sample.database.models.User;

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
        classes.add((Class<T>) Item.class);
        classes.add((Class<T>) User.class);
        classes.add((Class<T>) Game.class);
        classes.add((Class<T>) Group.class);
        return classes;
    }
}
