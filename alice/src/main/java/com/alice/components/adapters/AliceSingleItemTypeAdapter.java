package com.alice.components.adapters;

import android.content.Context;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceSingleItemTypeAdapter<T> extends AliceAbstractBinderAdapter<T> implements DataBinder<T> {

    private final Integer layoutId;

    public AliceSingleItemTypeAdapter(Context context, int layoutId, List<T> items, List<Integer> subViewsIds) {
        super(context, items, createMap(layoutId, subViewsIds), null);
        this.layoutId = layoutId;
        setDataBinder(this);
    }


    public AliceSingleItemTypeAdapter(Context context, int layoutId, List<Integer> subViewsIds, T... items) {
        super(context, createMap(layoutId, subViewsIds), null, items);
        this.layoutId = layoutId;
        setDataBinder(this);
    }

    public AliceSingleItemTypeAdapter(Context context, List<T> items, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap) {
        super(context, items, layoutIdToSubViewsIdsMap, null);
        setDataBinder(this);
        if (layoutIdToSubViewsIdsMap.isEmpty()) {
            this.layoutId = -1;
        } else {
            this.layoutId = layoutIdToSubViewsIdsMap.keySet().iterator().next();
        }
    }

    public AliceSingleItemTypeAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap, T... items) {
        super(context, layoutIdToSubViewsIdsMap, null, items);
        setDataBinder(this);
        if (layoutIdToSubViewsIdsMap.isEmpty()) {
            this.layoutId = -1;
        } else {
            this.layoutId = layoutIdToSubViewsIdsMap.keySet().iterator().next();
        }
    }

    /**
     * Binds data to a separate view.
     * @param view the view to be updated
     * @param viewId the id of view
     * @param item the item from adapter for this view
     */
    protected abstract void bindView(View view, Integer viewId, T item);

    @Override
    public void bindView(View view, int itemLayoutId, Integer viewId, T item) {
        bindView(view, viewId, item);
    }

    @Override
    protected int getLayoutIdForItem(int viewType) {
        return layoutId;
    }

    private static Map<Integer, List<Integer>> createMap(Integer layoutId, List<Integer> ids) {
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
        map.put(layoutId, ids);
        return map;
    }
}
