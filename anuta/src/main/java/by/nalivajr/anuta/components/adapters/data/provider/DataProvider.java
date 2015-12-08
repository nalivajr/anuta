package by.nalivajr.anuta.components.adapters.data.provider;

import android.database.DataSetObserver;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface DataProvider<T> {

    /**
     * Provides amount of items owned by provider
     * @return actual count of items
     */
    public int count();

    /**
     * Provides the item by it's position
     * @param position the position of the item
     * @return item on the given position
     * @throws IndexOutOfBoundsException if index greater or equal to {@link DataProvider#count()}
     */
    public T getItem(int position);

    /**
     * Notifies observers, that data changed
     */
    public void notifyDataSetChanged();

    /**
     * Registers observer, which will be used to notify about data updates
     * @param observer the instance of {@link DataSetObserver} to register
     */
    public void registerDataSetObserver(DataSetObserver observer);

    /**
     * Unregisters observer
     * @param observer the instance of {@link DataSetObserver} to unregister
     */
    public void unregisterDataSetObserver(DataSetObserver observer);

}
