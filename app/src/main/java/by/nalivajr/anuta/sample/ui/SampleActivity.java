package by.nalivajr.anuta.sample.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import by.nalivajr.anuta.annonatations.ui.AutoActivity;
import by.nalivajr.anuta.annonatations.ui.InnerView;
import by.nalivajr.anuta.sample.R;

@AutoActivity(layoutId = R.layout.ac_main, recursive = true)
public class SampleActivity extends Activity {

    @InnerView(R.id.tv_hello)
    protected TextView view;

    @InnerView(R.id.cv_test)
    protected CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view.setText("This is text");
    }
}
