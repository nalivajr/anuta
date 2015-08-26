package by.nalivajr.anuta.components.database.stub;

import java.util.Collection;

import by.nalivajr.anuta.components.database.query.AnutaQuery;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface LazyInitializationCollection<T> {

    /**
     * @return the query which can be used to load data for this collection
     */
    public AnutaQuery<T> getQuery();

    /**
     * @return the query which can be used to load data for this collection
     */
    public Class<? extends Collection<T>> getTargetCollectionType();
}
