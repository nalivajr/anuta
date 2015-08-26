package by.nalivajr.anuta.components.database.stub;

import java.util.Collection;

import by.nalivajr.anuta.components.database.query.AnutaQuery;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractLazyInitializationCollection<T> implements LazyInitializationCollection<T>, Collection<T> {

    protected final AnutaQuery<T> query;
    protected final Class<? extends Collection<T>> targetType;

    public AbstractLazyInitializationCollection(Class<? extends Collection<T>> type, AnutaQuery<T> query) {
        this.targetType = type;
        this.query = query;
    }

    @Override
    public AnutaQuery<T> getQuery() {
        return query;
    }

    @Override
    public Class<? extends Collection<T>> getTargetCollectionType() {
        return targetType;
    }
}
