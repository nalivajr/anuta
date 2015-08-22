package by.nalivajr.anuta.components.database.providers;

import by.nalivajr.anuta.components.database.helpers.AnutaRelationalDatabaseHelper;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaRelationalProvider extends AnutaContentProvider {

    private static final String TAG = AnutaRelationalProvider.class.getSimpleName();

    @Override
    protected abstract AnutaRelationalDatabaseHelper createDatabaseHelper();
}