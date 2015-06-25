package com.alice.components.adapters;

import android.content.Context;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceAbstractBinderAdapter<T> extends AliceAbstractAdapter<T> {

    private DataBinder<T> dataBinder;

    public AliceAbstractBinderAdapter(Context context, List<T> items, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, DataBinder<T> dataBinder) {
        super(context, items, layoutIdToSubViewsIdsMap);
        this.dataBinder = dataBinder;
    }

    public AliceAbstractBinderAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, DataBinder<T> dataBinder, T... items) {
        super(context, layoutIdToSubViewsIdsMap, items);
        this.dataBinder = dataBinder;
    }

    public DataBinder<T> getDataBinder() {
        return dataBinder;
    }

    public void setDataBinder(DataBinder<T> dataBinder) {
        this.dataBinder = dataBinder;
    }

    @Override
    protected void bindView(View view, int itemLayoutId, Integer viewId, T item) {
        if (dataBinder != null) {
            dataBinder.bindView(view, itemLayoutId, viewId, item);
        }
    }
}
