package by.nalivajr.alice.components.database.cursor;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.Iterator;

import by.nalivajr.alice.callbacks.database.CursorUpdatedListener;
import by.nalivajr.alice.callbacks.execution.ActionCallback;

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
     * Registers listener, which will be used to notify about data updates
     * @param listener the instance of {@link CursorUpdatedListener} to register
     */
    void registerCursorUpdatedListener(CursorUpdatedListener listener);

    /**
     * Unegisters listener
     * @param listener the instance of {@link CursorUpdatedListener} to unregister
     */
    void unregisterCursorUpdatedListener(CursorUpdatedListener listener);

    /**
     * Refreshes cursor data in background thread.
     * @see {@link AliceEntityCursor#requery(ActionCallback)}
     */
    void requery();

    /**
     * Refreshes cursor data in background thread. After refreshing new instance of cursor will be passed, but you may continue use this instance
     */
    void requery(ActionCallback<AliceEntityCursor> callback);

    /**
     * Set the requery mode. If true, then cursor will automatically reload data from database. Be aware, it may decrease performance.
     * By default auto-requery is disabled
     */
    void setAutoRequery(boolean autoRequery);

    /**
     * @return true if autorequeryEnabled and false otherwise
     */
    boolean isAutoRequery();

    /**
     * @return {@link ContentResolver} instance
     */
    ContentResolver getContentResolver();
}
