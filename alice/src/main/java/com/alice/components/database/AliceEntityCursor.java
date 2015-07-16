package com.alice.components.database;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AliceEntityCursor<T> {

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
}
