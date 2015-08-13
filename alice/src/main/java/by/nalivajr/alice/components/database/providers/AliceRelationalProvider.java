package by.nalivajr.alice.components.database.providers;

import by.nalivajr.alice.components.database.helpers.AliceRelationalDatabaseHelper;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceRelationalProvider extends AliceContentProvider {

    private static final String TAG = AliceRelationalProvider.class.getSimpleName();

    @Override
    protected abstract AliceRelationalDatabaseHelper createDatabaseHelper();
}