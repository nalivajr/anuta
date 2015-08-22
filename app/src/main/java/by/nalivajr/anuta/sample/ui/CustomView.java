package by.nalivajr.anuta.sample.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import by.nalivajr.anuta.annonatations.ui.AutoView;
import by.nalivajr.anuta.annonatations.ui.InnerView;
import by.nalivajr.anuta.sample.R;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@AutoView(layoutId = by.nalivajr.anuta.sample.R.layout.custom_view)
public class CustomView extends FrameLayout {

    @InnerView(R.id.tv_custom_view_cell_one)
    private TextView view1;

    @InnerView(by.nalivajr.anuta.sample.R.id.tv_custom_view_cell_two)
    private TextView view2;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
