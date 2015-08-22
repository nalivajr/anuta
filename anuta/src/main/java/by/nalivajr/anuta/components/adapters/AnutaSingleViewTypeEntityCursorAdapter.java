package by.nalivajr.anuta.components.adapters;

import android.content.Context;

import by.nalivajr.anuta.tools.Anuta;
import by.nalivajr.anuta.components.database.cursor.AnutaEntityCursor;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaSingleViewTypeEntityCursorAdapter<T> extends AnutaEntityCursorAdapter<T> {

    private final int layoutId;

    public AnutaSingleViewTypeEntityCursorAdapter(Context context, int layoutId, AnutaEntityCursor<T> cursor) {
        super(context, Anuta.adapterTools.buildIdsMap(context, layoutId), cursor);
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
