package by.nalivajr.alice.components.adapters;

import android.content.Context;

import by.nalivajr.alice.components.adapters.data.provider.DataProvider;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceDataProvidedSingleViewAdapter<T> extends AliceSingleViewTypeAdapter<T> {

    private DataProvider<T> dataProvider;

    public AliceDataProvidedSingleViewAdapter(Context context, int layoutId, DataProvider<T> dataProvider) {
        super(context, layoutId);
        this.dataProvider = dataProvider;
    }

    @Override
    public T getItem(int position) {
        return dataProvider == null ? null : dataProvider.getItem(position);
    }

    @Override
    public int getCount() {
        return dataProvider == null ? 0 : dataProvider.count();
    }
}
