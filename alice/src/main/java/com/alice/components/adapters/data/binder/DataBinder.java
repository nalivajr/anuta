package com.alice.components.adapters.data.binder;

import android.view.View;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface DataBinder<T> {
    /**
     * Binds data to the given view.
     * @param view the view to be updated
     * @param itemLayoutId the id of layout for the given {@code item}
     * @param viewId the id of view
     * @param item the item from adapter for this view
     */
    public void bindView(View view, int itemLayoutId, Integer viewId, T item);
}
