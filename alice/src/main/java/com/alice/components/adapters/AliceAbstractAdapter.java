package com.alice.components.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceAbstractAdapter<T> extends BaseAdapter {

    private LayoutInflater inflater;
    private Map<Integer, List<Integer>> typeToIdsMap = new HashMap<Integer, List<Integer>>();

    /**
     * Creates instance of {@link AliceAbstractAdapter}
     * @param context the context
     * @param layoutIdToSubViewsIdsMap the map, which contains the resource id of layout for item
     *                                 and list of ids of sub views which are used to present particular property
     *                                 of model object
     */
    public AliceAbstractAdapter(Context context, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap) {
        if (layoutIdToSubViewsIdsMap != null) {
            this.typeToIdsMap.putAll(layoutIdToSubViewsIdsMap);
        }
        inflater = LayoutInflater.from(context);
    }


    /**
     * Returns the layout resource id, which should be used to present item
     * @param viewType the type of view for the item
     * @return the layout resource id
     */
    protected abstract int getLayoutIdForItem(int viewType);

    /**
     * Binds data to a separate view.
     * @param view the view to be updated
     * @param itemLayoutId the id of layout for the given {@code item}
     * @param viewId the id of view
     * @param item the item from adapter for this view
     */
    protected abstract void bindView(View view, int itemLayoutId, Integer viewId, T item);

    @Override
    public abstract int getItemViewType(int position);

    @Override
    public abstract int getViewTypeCount();

    @Override
    public abstract T getItem(int position);

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = initNewView(position);
        }
        bindItemView(position, convertView);
        return convertView;
    }

    private View initNewView(int position) {
        int layoutId = getLayoutIdForItem(getItemViewType(position));
        View view = inflater.inflate(layoutId, null);
        List<Integer> ids = typeToIdsMap.get(layoutId);
        if (ids != null) {
            Map<Integer, View> views = new HashMap<Integer, View>();
            for (Integer id : ids) {
                View subView = view.findViewById(id);
                views.put(id, subView);
            }
            view.setTag(views);
        }
        return view;
    }

    private void bindItemView(int position, View convertView) {
        int layoutId = getLayoutIdForItem(getItemViewType(position));
        T item = getItem(position);
        Map<Integer, View> params = (Map<Integer, View>) convertView.getTag();
        if (params == null) {
            return;
        }
        for (Integer key : params.keySet()) {
            View view = params.get(key);
            if (view != null) {
                bindView(view, layoutId, key, item);
            }
        }
    }
}
