package by.nalivajr.anuta.components.adapters.data.provider;

import android.database.DataSetObserver;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractDataProvider<T> implements DataProvider<T> {

    private Set<DataSetObserver> observers = new HashSet<DataSetObserver>();

    @Override
    public void notifyDataSetChanged() {
        for (DataSetObserver observer : observers) {
            observer.onChanged();
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        observers.remove(observer);
    }
}
