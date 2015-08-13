package by.nalivajr.alice.components.adapters;

import android.content.Context;

import by.nalivajr.alice.components.adapters.data.provider.DataProvider;

import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceDataProvidedAdapter<T> extends AliceAbstractAdapter<T> {

    protected DataProvider<T> dataProvider;

    public AliceDataProvidedAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, DataProvider<T> dataProvider) {
        super(context, layoutIdToSubViewsIdsMap);
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
