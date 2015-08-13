package by.nalivajr.alice.sample.database;

import android.content.Context;

import by.nalivajr.alice.components.database.entitymanager.AliceRelationalEntityManager;
import by.nalivajr.alice.sample.database.models.SubSubItem;

import java.util.ArrayList;
import java.util.List;

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
        return list;
    }
}
