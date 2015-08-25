package by.nalivajr.anuta.test.database;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import by.nalivajr.anuta.components.database.entitymanager.AnutaRelationalEntityManager;
import by.nalivajr.anuta.test.models.Department;
import by.nalivajr.anuta.test.models.Employee;
import by.nalivajr.anuta.test.models.Tag;
import by.nalivajr.anuta.test.models.Technology;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class TestEntityManager extends AnutaRelationalEntityManager {

    public TestEntityManager(Context context) {
        super(context);
    }

    @Override
    protected List<Class<?>> getEntityClasses() {
        return Arrays.asList(
                Department.class,
                Employee.class,
                Tag.class,
                Technology.class
        );
    }
}
