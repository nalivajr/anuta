package by.nalivajr.anuta.components.database.stub;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.exceptions.NotInitializedException;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class LazyInitializationSet<T> extends AbstractLazyInitializationCollection<T> implements Set<T> {

    public LazyInitializationSet(Class<? extends Collection<T>> type, AnutaQuery<T> query) {
        super(type, query);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        throw new NotInitializedException();
    }

    @Override
    public int size() {
        throw new NotInitializedException();
    }

    @Override
    public boolean add(T object) {
        throw new NotInitializedException();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> collection) {
        throw new NotInitializedException();
    }

    @Override
    public void clear() {
        throw new NotInitializedException();
    }

    @Override
    public boolean contains(Object object) {
        throw new NotInitializedException();
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        throw new NotInitializedException();
    }

    @Override
    public boolean isEmpty() {
        throw new NotInitializedException();
    }

    @Override
    public boolean remove(Object object) {
        throw new NotInitializedException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        throw new NotInitializedException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        throw new NotInitializedException();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        throw new NotInitializedException();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] array) {
        throw new NotInitializedException();
    }
}
