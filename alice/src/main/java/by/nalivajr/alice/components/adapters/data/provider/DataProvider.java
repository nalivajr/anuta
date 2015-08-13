package by.nalivajr.alice.components.adapters.data.provider;

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
     * @throws IndexOutOfBoundsException if index >= {@link DataProvider#count()}
     */
    public T getItem(int position);
}
