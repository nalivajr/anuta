package by.nalivajr.alice.components.database.models;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */

/**
 * Provides ability to get the id of object
 * @param <I> the type which should be used as id representation
 */
public interface Identifiable<I> {

    /**
     * @return the id of object
     */
    public I getId();
}
