package by.nalivajr.alice.sample.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import by.nalivajr.alice.components.database.entitymanager.AliceRelationalEntityManager;
import by.nalivajr.alice.sample.database.models.Game;
import by.nalivajr.alice.sample.database.models.Group;
import by.nalivajr.alice.sample.database.models.Item;
import by.nalivajr.alice.sample.database.models.SubSubItem;
import by.nalivajr.alice.sample.database.models.User;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SampleEntityManager extends AliceRelationalEntityManager {
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
        return list;
    }
}
