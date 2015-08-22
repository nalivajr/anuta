package by.nalivajr.anuta.components.adapters;

import android.content.Context;
import android.database.DataSetObserver;

import by.nalivajr.anuta.components.adapters.data.provider.DataProvider;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaDataProvidedSingleViewAdapter<T> extends AnutaSingleViewTypeAdapter<T> {

    private DataProvider<T> dataProvider;

    public AnutaDataProvidedSingleViewAdapter(Context context, int layoutId, DataProvider<T> dataProvider) {
        super(context, layoutId);
        this.dataProvider = dataProvider;
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
