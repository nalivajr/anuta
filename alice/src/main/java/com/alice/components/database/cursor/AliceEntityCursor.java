package com.alice.components.database.cursor;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;

import com.alice.callbacks.database.ActionCallback;

import java.util.Iterator;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AliceEntityCursor<T> extends Iterator<T> {


    boolean hasNext();
    /**
     * @return an entity at position next to current
     * @throws java.util.NoSuchElementException if there are no any entities avalable
     */
    T next();

    /**
     * @return an entity an current position
     */
    T getCurrent();

    /**
     * @return an entity at the given position
     */
    T getAtPosition(int position);

    /**
     * @see Cursor#getCount()
     */
    int getCount();

    /**
     * @see Cursor#getPosition()
     */
    int getPosition();

    /**
     * @see Cursor#move(int)
     */
    boolean move(int offset);

    /**
     * @see Cursor#moveToPosition(int)
     */
    boolean moveToPosition(int position);

    /**
     * @see Cursor#moveToFirst()
     */
    boolean moveToFirst();

    /**
     * @see Cursor#moveToLast()
     */
    boolean moveToLast();

    /**
     * @see Cursor#moveToNext()
     */
    boolean moveToNext();

    /**
     * @see Cursor#moveToPrevious()
     */
    boolean moveToPrevious();

    /**
     * @see Cursor#isFirst()
     */
    boolean isFirst();

    /**
     * @see Cursor#isLast()
     */
    boolean isLast();

    /**
     * @see Cursor#isBeforeFirst()
     */
    boolean isBeforeFirst();

    /**
     * @see Cursor#isAfterLast()
     */
    boolean isAfterLast();

    /**
     * @see Cursor#close()
     */
    void close();

    /**
     * @see Cursor#isClosed()
     */
    boolean isClosed();

    /**
     * @see Cursor#registerContentObserver(ContentObserver)
     */
    void registerContentObserver(ContentObserver observer);

    /**
     * @see Cursor#unregisterContentObserver(ContentObserver)
     */
    void unregisterContentObserver(ContentObserver observer);

    /**
     * @see Cursor#registerDataSetObserver(DataSetObserver)
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * @see Cursor#unregisterDataSetObserver(DataSetObserver)
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * Refreshes cursor data in background thread.
     * @see {@link AliceEntityCursor#requery(ActionCallback)}
     */
    void requery();

    /**
     * Refreshes cursor data in background thread. After refreshing new instance of cursor will be passed, but you may continue use this instance
     */
    void requery(ActionCallback<Cursor> callback);

    /**
     * @return {@link ContentResolver} instance
     */
    ContentResolver getContentResolver();
}
