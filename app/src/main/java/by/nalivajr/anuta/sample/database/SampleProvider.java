package by.nalivajr.anuta.sample.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import by.nalivajr.anuta.components.database.helpers.AnutaRelationalDatabaseHelper;
import by.nalivajr.anuta.components.database.providers.AnutaRelationalProvider;
import by.nalivajr.anuta.sample.database.models.Game;
import by.nalivajr.anuta.sample.database.models.Group;
import by.nalivajr.anuta.sample.database.models.Item;
import by.nalivajr.anuta.sample.database.models.SubSubItem;
import by.nalivajr.anuta.sample.database.models.User;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SampleProvider extends AnutaRelationalProvider {
    @Override
    protected AnutaRelationalDatabaseHelper createDatabaseHelper() {
        return new AnutaRelationalDatabaseHelper(getContext(), "AnutaSampleDB.db", null, Contract.DATABASE_VERSION) {
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
