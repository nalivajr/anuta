package by.nalivajr.anuta.components.database.providers;

import by.nalivajr.anuta.components.database.helpers.AnutaNoSQLDatabaseHelper;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com

 * @deprecated Not implemented totally, can be used only without relations
 */
@Deprecated
public abstract class AnutaNoSQLProvider extends AnutaContentProvider {

    private static final String TAG = AnutaNoSQLProvider.class.getSimpleName();

    @Override
    protected abstract AnutaNoSQLDatabaseHelper createDatabaseHelper();
}