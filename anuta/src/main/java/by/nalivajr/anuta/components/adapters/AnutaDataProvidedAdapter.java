package by.nalivajr.anuta.components.adapters;

import android.content.Context;
import android.database.DataSetObserver;

import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.components.adapters.data.provider.DataProvider;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaDataProvidedAdapter<T> extends AnutaAbstractAdapter<T> {

    protected DataProvider<T> dataProvider;

    public AnutaDataProvidedAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, DataProvider<T> dataProvider) {
        super(context, layoutIdToSubViewsIdsMap);
        this.dataProvider = dataProvider;
        if (dataProvider != null) {
            return;
        }
        this.dataProvider.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        });
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
