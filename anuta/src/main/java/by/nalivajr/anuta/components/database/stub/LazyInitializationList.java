package by.nalivajr.anuta.components.database.stub;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.exceptions.NotInitializedException;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class LazyInitializationList<T> extends AbstractLazyInitializationCollection<T> implements List<T> {

    public LazyInitializationList(Class<? extends Collection<T>> type, AnutaQuery<T> query) {
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
    public void add(int location, T object) {
        throw new NotInitializedException();
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends T> collection) {
        throw new NotInitializedException();
    }

    @Override
    public T get(int location) {
        throw new NotInitializedException();
    }

    @Override
    public int indexOf(Object object) {
        throw new NotInitializedException();
    }

    @Override
    public int lastIndexOf(Object object) {
        throw new NotInitializedException();
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new NotInitializedException();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        throw new NotInitializedException();
    }

    @Override
    public T remove(int location) {
        throw new NotInitializedException();
    }

    @Override
    public T set(int location, T object) {
        throw new NotInitializedException();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
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
