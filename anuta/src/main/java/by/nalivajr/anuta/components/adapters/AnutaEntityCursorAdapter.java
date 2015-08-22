package by.nalivajr.anuta.components.adapters;

import android.content.Context;

import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.components.adapters.data.provider.CursorDataProvider;
import by.nalivajr.anuta.components.database.cursor.AnutaEntityCursor;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaEntityCursorAdapter<T> extends AnutaDataProvidedAdapter<T> {


    public AnutaEntityCursorAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, AnutaEntityCursor<T> cursor) {
        super(context, layoutIdToSubViewsIdsMap, new CursorDataProvider<T>(cursor));
    }
}
