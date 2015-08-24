package by.nalivajr.anuta.sample.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import by.nalivajr.anuta.components.database.entitymanager.AnutaRelationalEntityManager;
import by.nalivajr.anuta.sample.database.models.Game;
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
public class SampleEntityManager extends AnutaRelationalEntityManager {
    public SampleEntityManager(Context context) {
        super(context);
    }

    @Override
    protected List<Class<?>> getEntityClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(SubSubItem.class);
        list.add(Item.class);
        list.add(Game.class);
        list.add(User.class);
        list.add(Group.class);
        list.add(SampleItem.class);
        list.add(ItemCollector.class);
        return list;
    }
}
