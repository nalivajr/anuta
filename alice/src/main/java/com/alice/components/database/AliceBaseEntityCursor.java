package com.alice.components.database;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;

import java.util.NoSuchElementException;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceBaseEntityCursor<T> implements AliceEntityCursor<T> {

    private final Cursor cursor;
    private boolean moved = false;

    public AliceBaseEntityCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    /**
     * Converts cursor's data from current position to entity
     * @param cursor source cursor
     * @return converted entity
     */
    protected abstract T convert(Cursor cursor);

    @Override
    public boolean hasNext() {
        return getCount() > getPosition();
    }

    @Override
    public T next() {
        if (isLast() || !moveToNext()) {
            throw new NoSuchElementException("There are no more elements in cursor");
        }
        // as we have already moved, then return current
        return getCurrent();
    }

    @Override
    public T getCurrent() {
        return convert(cursor);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Could not remove entity. Operation is not supported by " + AliceBaseEntityCursor.class.getName());
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public int getPosition() {
        return cursor.getPosition();
    }

    @Override
    public boolean move(int offset) {
        return cursor.move(offset);
    }

    @Override
    public boolean moveToPosition(int position) {
        return cursor.moveToPosition(position);
    }

    @Override
    public boolean moveToFirst() {
        return cursor.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        return cursor.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        return cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        return cursor.moveToPrevious();
    }

    @Override
    public boolean isFirst() {
        return cursor.isFirst();
    }

    @Override
    public boolean isLast() {
        return cursor.isLast();
    }

    @Override
    public boolean isBeforeFirst() {
        return cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor.isAfterLast();
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    public boolean isClosed() {
        return cursor.isClosed();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        cursor.registerContentObserver(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        cursor.unregisterContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        cursor.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        cursor.unregisterDataSetObserver(observer);
    }
}
