package by.nalivajr.anuta.test.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.List;

import by.nalivajr.anuta.components.database.helpers.AnutaRelationalDatabaseHelper;
import by.nalivajr.anuta.components.database.providers.AnutaRelationalProvider;
import by.nalivajr.anuta.test.content.TestContract;
import by.nalivajr.anuta.test.models.Department;
import by.nalivajr.anuta.test.models.Employee;
import by.nalivajr.anuta.test.models.Tag;
import by.nalivajr.anuta.test.models.Technology;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class TestProvider extends AnutaRelationalProvider {
    @Override
    protected AnutaRelationalDatabaseHelper createDatabaseHelper() {
        return new AnutaRelationalDatabaseHelper(getContext(), "AnutaTestDB.db", null, TestContract.DATABASE_VERSION) {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Class<?>> getEntityClasses() {
        return Arrays.asList(
                Department.class,
                Employee.class,
                Tag.class,
                Technology.class
        );
    }
}
