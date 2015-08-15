package by.nalivajr.alice.components.adapters.data.provider;

import android.support.annotation.NonNull;

import by.nalivajr.alice.callbacks.database.CursorUpdatedListener;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class CursorDataProvider<T> extends AbstractDataProvider<T> {
    private AliceEntityCursor<T> mCursor;

    public CursorDataProvider(AliceEntityCursor<T> cursor) {
        this.mCursor = cursor;
        cursor.registerCursorUpdatedListener(createListener());
    }

    /**
     * Is called in Background thread when cursor's managed data update and before cursor will be requeried
     */
    protected void onContentChanged() {
        // Do nothing
    }
    
    /**
     * Is called in Main Thread when cursor was requeried and before {@link DataProvider#notifyDataSetChanged()} will be called
     */
    protected void onCursorRequeried() {
        // Do nothing
    }

    @NonNull
    private CursorUpdatedListener createListener() {
        return new CursorUpdatedListener() {
            @Override
            public void onDataUpdated() {
                onContentChanged();
                mCursor.requery();
            }

            @Override
            public void onRequeryFinished() {
                onCursorRequeried();
                notifyDataSetChanged();
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
