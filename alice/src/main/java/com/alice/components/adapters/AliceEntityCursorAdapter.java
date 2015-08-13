package com.alice.components.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.alice.components.adapters.data.provider.CursorDataProvider;
import com.alice.components.adapters.data.provider.DataProvider;
import com.alice.components.database.cursor.AliceEntityCursor;

import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceEntityCursorAdapter<T> extends AliceDataProvidedAdapter<T> {

    private Handler uiHandler;

    public AliceEntityCursorAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, AliceEntityCursor<T> cursor) {
        super(context, layoutIdToSubViewsIdsMap, null);
        uiHandler = new Handler(Looper.getMainLooper());
        dataProvider = createDataProvider(cursor);
    }

    private DataProvider<T> createDataProvider(final AliceEntityCursor<T> cursor) {
        return new CursorDataProvider<T>(cursor) {
            @Override
            protected void onDataUpdated() {
                notifyDataUpdated();
            }
        };
    }

    private void notifyDataUpdated() {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } else {
            notifyDataSetChanged();
        }
    }
}
