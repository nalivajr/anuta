package by.nalivajr.alice.components.database.cursor;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.Log;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import by.nalivajr.alice.callbacks.execution.ActionCallback;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceBaseEntityCursor<T> implements AliceEntityCursor<T> {

    private static final String TAG = AliceBaseEntityCursor.class.getSimpleName();

    private final ReentrantLock lock = new ReentrantLock();
    private final Uri notificationUri;
    private Set<ContentObserver> observers;
    private Cursor cursor;

    public AliceBaseEntityCursor(Cursor cursor, Uri notificationUri) {
        this.cursor = cursor;
        this.cursor.moveToFirst();
        this.notificationUri = notificationUri;
        observers = new HashSet<ContentObserver>();
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
    public T getAtPosition(int position) {
        try {
            lock.lock();
            int currPosition = cursor.getPosition();
            if (!cursor.moveToPosition(position)) {
                return null;
            }
            T result = getCurrent();
            cursor.moveToPosition(currPosition);
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void requery() {
        requery(null);
    }

    @Override
    public void requery(final ActionCallback<Cursor> callback) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    swapCursor(getActualCursor());
                    if (cursor != null) {
                        cursor.moveToFirst();
                    }

                    if (callback != null) {
                        callback.onFinishedSuccessfully(cursor);
                    }
                } catch (Throwable e) {
                    if (callback != null) {
                        Log.w(TAG, "Could not update entity cursor");
                        callback.onErrorOccurred(e);
                    }
                } finally {
                    lock.unlock();
                }
            }
        };
        new Thread(action).start();
    }

    protected abstract Cursor getActualCursor();

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Could not remove entity. Operation is not supported by " + AliceBaseEntityCursor.class.getName());
    }

    private Cursor swapCursor(Cursor newCursor) {
        for (ContentObserver observer : observers) {
            if (cursor != null) {
                cursor.unregisterContentObserver(observer);
            }
            if (newCursor != null) {
                newCursor.registerContentObserver(observer);
            }
        }
        if (newCursor != null) {
            newCursor.setNotificationUri(getContentResolver(), notificationUri);
        }
        cursor = newCursor;
        return cursor;
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public int getPosition() {
        return cursor == null ? -1 : cursor.getPosition();
    }

    @Override
    public boolean move(int offset) {
        return cursor != null && cursor.move(offset);
    }

    @Override
    public boolean moveToPosition(int position) {
        return cursor != null && cursor.moveToPosition(position);
    }

    @Override
    public boolean moveToFirst() {
        return cursor != null && cursor.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        return cursor != null && cursor.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        return cursor != null && cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        return cursor != null && cursor.moveToPrevious();
    }

    @Override
    public boolean isFirst() {
        return cursor != null && cursor.isFirst();
    }

    @Override
    public boolean isLast() {
        return cursor != null && cursor.isLast();
    }

    @Override
    public boolean isBeforeFirst() {
        return cursor != null && cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor != null && cursor.isAfterLast();
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public boolean isClosed() {
        return cursor != null && cursor.isClosed();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        if (cursor != null) {
            cursor.registerContentObserver(observer);
            cursor.setNotificationUri(getContentResolver(), notificationUri);
            observers.add(observer);
        }
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        if (cursor != null) {
            cursor.unregisterContentObserver(observer);
            observers.remove(observer);
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (cursor != null) {
            cursor.registerDataSetObserver(observer);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (cursor != null) {
            cursor.unregisterDataSetObserver(observer);
        }
    }
}
