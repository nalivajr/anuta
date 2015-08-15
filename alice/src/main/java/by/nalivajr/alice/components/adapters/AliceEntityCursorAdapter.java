package by.nalivajr.alice.components.adapters;

import android.content.Context;

import java.util.List;
import java.util.Map;

import by.nalivajr.alice.components.adapters.data.provider.CursorDataProvider;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceEntityCursorAdapter<T> extends AliceDataProvidedAdapter<T> {


    public AliceEntityCursorAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, AliceEntityCursor<T> cursor) {
        super(context, layoutIdToSubViewsIdsMap, new CursorDataProvider<T>(cursor));
    }
}
