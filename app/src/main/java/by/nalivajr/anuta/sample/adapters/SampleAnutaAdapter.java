package by.nalivajr.anuta.sample.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.components.adapters.AnutaAbstractAdapter;
import by.nalivajr.anuta.sample.R;
import by.nalivajr.anuta.sample.models.Person;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SampleAnutaAdapter extends AnutaAbstractAdapter<Person> {

    private Context context;
    private List<Person> items;

    public SampleAnutaAdapter(Context context, List<Person> items, Map<Integer, List<Integer>> layoutIdToSubViewsIdsMap) {
        super(context, layoutIdToSubViewsIdsMap);
        this.context = context;
        this.items = new ArrayList<Person>(items);
    }

    @Override
    protected int getLayoutIdForItem(int viewType) {
        if (viewType == 0) {
            return R.layout.anuta_adapter_item_left;
        } else {
            return R.layout.anuta_adapter_item_right;
        }
    }

    @Override
    public Person getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
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
            case R.layout.anuta_adapter_item_right:
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
            case R.layout.anuta_adapter_item_left:
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
