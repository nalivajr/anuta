package by.nalivajr.alice.components.database.cursor;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import by.nalivajr.alice.callbacks.database.CursorUpdatedListener;
import by.nalivajr.alice.callbacks.execution.ActionCallback;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceBaseEntityCursor<T> implements AliceEntityCursor<T> {

    private static final String TAG = AliceBaseEntityCursor.class.getSimpleName();

//    private final ReentrantLock lock = new ReentrantLock();
    private final Uri notificationUri;
    private final ContentObserver selfObserver;
    private Set<CursorUpdatedListener> listeners;
    private HandlerThread contentObserverThread;
    private ExecutorService reloadThread;

    private Cursor cursor;
    /**
     * The udated cursore with reloaded data
     */
    private Cursor actualCursor;

    private Handler handler;
    private Handler uiHandler;

    private AtomicInteger requeryCalls = new AtomicInteger(0);
    private AtomicInteger notifyUiCalls = new AtomicInteger(0);

    private boolean autoRequery = false;

    public AliceBaseEntityCursor(Cursor cursor, final Uri notificationUri) {
        this.cursor = cursor;
        this.cursor.moveToFirst();
        this.notificationUri = notificationUri;
        listeners = new HashSet<CursorUpdatedListener>();
        initThreads();
        handler = new Handler(contentObserverThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());
        selfObserver = createSelfContentObserver();

        cursor.registerContentObserver(selfObserver);
        cursor.setNotificationUri(getContentResolver(), notificationUri);
    }

    /**
     * Converts cursor's data from current position to entity
     * @param cursor source cursor
     * @return converted entity
     */
    protected abstract T convert(Cursor cursor);

    /**
     * Provides cursor with actual data
     */
    protected abstract Cursor getActualCursor();

    @NonNull
    protected ContentObserver createSelfContentObserver() {
        return new ContentObserver(handler) {
            @Override
            public void onChange(final boolean selfChange) {
                if (autoRequery) {
                    requery();
                } else {
                    notifyObservers(false);
                }
            }
        };
    }

    private void notifyObservers(boolean requeried) {
        for (CursorUpdatedListener listener : listeners) {
            if (!requeried) {
                listener.onDataUpdated();
            } else {
                listener.onRequeryFinished();
            }
        }
    }

    private void initThreads() {
        contentObserverThread = new HandlerThread("AliceCursorContentObserverHandlerThread");
        contentObserverThread.start();
        if (reloadThread != null) {
            reloadThread.shutdownNow();
        }
        reloadThread = Executors.newSingleThreadExecutor();
    }

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
        int currPosition = cursor.getPosition();
        if (!cursor.moveToPosition(position)) {
            return null;
        }
        T result = getCurrent();
        cursor.moveToPosition(currPosition);
        return result;
    }

    @Override
    public void requery() {
        requery(null);
    }

    @Override
    public void requery(final ActionCallback<AliceEntityCursor> callback) {
        requeryCalls.incrementAndGet();
        if (!contentObserverThread.isAlive()) {
            initThreads();
            handler = new Handler(contentObserverThread.getLooper());
        }
        Runnable action = new Runnable() {
            @Override
            public void run() {
                reload(callback);
            }
        };
        reloadThread.submit(action);
    }

    protected void reload(ActionCallback<AliceEntityCursor> callback) {
        if (requeryCalls.get() > 1) {
            Log.i(TAG, "Too much requery calls. Skipping until one left");
            requeryCalls.decrementAndGet();
            return;
        }
        try {
            Cursor actualCursor = getActualCursor();
            notifyUiThread(actualCursor, callback);
        } catch (Throwable e) {
            if (callback != null) {
                Log.w(TAG, "Could not update entity cursor");
                callback.onErrorOccurred(e);
            }
        } finally {
            requeryCalls.decrementAndGet();
        }
    }

    private void notifyUiThread(Cursor actualCursor, final ActionCallback<AliceEntityCursor> callback) {
        this.actualCursor = actualCursor;
        int notifiers = notifyUiCalls.getAndIncrement();
        if (notifiers > 0) {
            notifyUiCalls.decrementAndGet();
            Log.i(TAG, "UI notification is skipped as last was not received yet");
            return;
        }
        Runnable action = new Runnable() {
            @Override
            public void run() {
                notifyUiCalls.decrementAndGet();
                swapCursor(AliceBaseEntityCursor.this.actualCursor);
                if (cursor != null) {
                    cursor.moveToFirst();
                }

                if (callback != null) {
                    callback.onFinishedSuccessfully(AliceBaseEntityCursor.this);
                }
                notifyObservers(true);
            }
        };
        uiHandler.post(action);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Could not remove entity. Operation is not supported by " + AliceBaseEntityCursor.class.getName());
    }

    private Cursor swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.unregisterContentObserver(selfObserver);
            cursor.close();
        }
        if (newCursor != null) {
            newCursor.registerContentObserver(selfObserver);
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
            cursor.unregisterContentObserver(selfObserver);
        }
        if (contentObserverThread.isAlive()) {
            contentObserverThread.quit();
        }
        if (reloadThread != null) {
            reloadThread.shutdownNow();
        }
    }

    @Override
    public boolean isClosed() {
        return cursor != null && cursor.isClosed();
    }

    @Override
    public void registerCursorUpdatedListener(CursorUpdatedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterCursorUpdatedListener(CursorUpdatedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setAutoRequery(boolean autoRequery) {
        this.autoRequery = autoRequery;
    }

    @Override
    public boolean isAutoRequery() {
        return autoRequery;
    }
}
