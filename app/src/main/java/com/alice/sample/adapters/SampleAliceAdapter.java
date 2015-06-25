package com.alice.sample.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alice.components.adapters.AliceAbstractAdapter;
import com.alice.sample.R;
import com.alice.sample.models.Person;

import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SampleAliceAdapter extends AliceAbstractAdapter<Person> {

    private Context context;

    public SampleAliceAdapter(Context context, List<Person> items, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap) {
        super(context, items, layoutIdToSubViewsIdsMap);
        this.context = context;
    }

    @Override
    protected int getLayoutIdForItem(int viewType) {
        if (viewType == 0) {
            return R.layout.alice_adapter_item_left;
        } else {
            return R.layout.alice_adapter_item_right;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected void bindView(View view, int itemLayoutId, Integer viewId, Person item) {
        switch (itemLayoutId) {
            case R.layout.alice_adapter_item_right:
                switch (viewId) {
                    case R.id.iv_photo_img:
                        ((ImageView)view).setImageDrawable(context.getResources().getDrawable(item.getPhotoId()));
                        break;
                    case R.id.tv_user_name:
                        ((TextView)view).setText(item.getName());
                        break;
                    case R.id.tv_user_lastName:
                        ((TextView)view).setText(item.getLastName());
                        break;
                }
                break;
            case R.layout.alice_adapter_item_left:
                switch (viewId) {
                    case R.id.iv_photo_img:
                        ((ImageView)view).setImageDrawable(context.getResources().getDrawable(item.getPhotoId()));
                        break;
                    case R.id.tv_user_name:
                        ((TextView)view).setText(item.getName());
                        break;
                    case R.id.tv_user_lastName:
                        ((TextView)view).setText(item.getLastName());
                        break;
                }
        }
    }
}
