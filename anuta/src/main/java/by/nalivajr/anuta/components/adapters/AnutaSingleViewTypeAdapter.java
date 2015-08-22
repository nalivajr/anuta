package by.nalivajr.anuta.components.adapters;

import android.content.Context;
import android.view.View;

import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaSingleViewTypeAdapter<T> extends AnutaAbstractAdapter<T> {

    private final Integer layoutId;

    public AnutaSingleViewTypeAdapter(Context context, int layoutId) {
        super(context, Anuta.adapterTools.buildIdsMap(context, layoutId));
        this.layoutId = layoutId;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    /**
     * Binds data to a separate view.
     * @param view the view to be updated
     * @param viewId the id of view
     * @param item the item from adapter for this view
     */
    protected abstract void bindView(View view, Integer viewId, T item);

    @Override
    protected void bindView(View view, int itemLayoutId, Integer viewId, T item) {
        bindView(view, viewId, item);
    }

    @Override
    protected int getLayoutIdForItem(int viewType) {
        return layoutId;
    }
}
