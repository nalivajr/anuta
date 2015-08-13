package com.alice.components.adapters.data.provider;

import android.database.ContentObserver;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alice.callbacks.database.ActionCallback;
import com.alice.components.adapters.AliceDataProvidedAdapter;
import com.alice.components.database.cursor.AliceEntityCursor;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class CursorDataProvider<T> implements DataProvider<T> {
    private AliceEntityCursor<T> mCursor;

    public CursorDataProvider(AliceEntityCursor<T> cursor) {
        this.mCursor = cursor;
        cursor.registerContentObserver(createObserver());
    }

    /**
     * Is invoked when cursor's managed data update
     */
    protected abstract void onDataUpdated();

    @NonNull
    private ContentObserver createObserver() {
        return new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                mCursor.requery(new ActionCallback<Cursor>() {
                    @Override
                    public void onSuccess(Cursor result) {
                        onDataUpdated();
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        Log.w(AliceDataProvidedAdapter.class.getName(), "Could not requery cursor", e);
                    }
                });
            }
        };
    }

    @Override
    public int count() {
        return mCursor.getCount();
    }

    @Override
    public T getItem(int position) {
        if (position < mCursor.getCount()) {
            return mCursor.getAtPosition(position);
        }
        throw new IndexOutOfBoundsException("Index is greater then items count in provider");
    }
}
