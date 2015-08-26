package by.nalivajr.anuta.sample.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import by.nalivajr.anuta.components.database.helpers.AnutaRelationalDatabaseHelper;
import by.nalivajr.anuta.components.database.providers.AnutaRelationalProvider;
import by.nalivajr.anuta.sample.database.models.Game;
import by.nalivajr.anuta.sample.database.models.GameGroup;
import by.nalivajr.anuta.sample.database.models.Group;
import by.nalivajr.anuta.sample.database.models.Item;
import by.nalivajr.anuta.sample.database.models.ItemCollector;
import by.nalivajr.anuta.sample.database.models.SampleItem;
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
    public List<Class<?>> getEntityClasses() {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(SubSubItem.class);
        classes.add(Item.class);
        classes.add(User.class);
        classes.add(Game.class);
        classes.add(Group.class);
        classes.add(ItemCollector.class);
        classes.add(SampleItem.class);
        classes.add(GameGroup.class);
        return classes;
    }
}
