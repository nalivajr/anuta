package by.nalivajr.alice.components.adapters;

import android.content.Context;
import android.view.View;

import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceSingleViewTypeAdapter<T> extends AliceAbstractAdapter<T> {

    private final Integer layoutId;

    public AliceSingleViewTypeAdapter(Context context, int layoutId) {
        super(context, Alice.adapterTools.buildIdsMap(context, layoutId));
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
