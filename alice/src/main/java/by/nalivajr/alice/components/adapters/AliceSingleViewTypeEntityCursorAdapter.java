package by.nalivajr.alice.components.adapters;

import android.content.Context;

import by.nalivajr.alice.tools.Alice;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceSingleViewTypeEntityCursorAdapter<T> extends AliceEntityCursorAdapter<T> {

    private final int layoutId;

    public AliceSingleViewTypeEntityCursorAdapter(Context context, int layoutId, AliceEntityCursor<T> cursor) {
        super(context, Alice.adapterTools.buildIdsMap(context, layoutId), cursor);
        this.layoutId = layoutId;
    }

    @Override
    protected int getLayoutIdForItem(int viewType) {
        return layoutId;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
