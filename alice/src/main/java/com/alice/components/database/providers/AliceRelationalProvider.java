package com.alice.components.database.providers;

import com.alice.components.database.helpers.AliceRelationalDatabaseHelper;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceRelationalProvider extends AliceContentProvider {

    private static final String TAG = AliceRelationalProvider.class.getSimpleName();

    @Override
    protected abstract AliceRelationalDatabaseHelper createDatabaseHelper();
}
