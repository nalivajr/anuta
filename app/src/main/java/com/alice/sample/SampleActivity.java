package com.alice.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.alice.annonatations.AutoActivity;
import com.alice.annonatations.InnerView;
import com.alice.sample.ui.CustomView;

@AutoActivity(layoutId = R.layout.ac_main, recursive = true)
public class SampleActivity extends Activity {

    @InnerView(R.id.tv_hello)
    protected TextView view;

    @InnerView(R.id.cv_test)
    protected CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view.setText("this is text");
        Log.i("MainActivity", "[CBT] onCreate");
    }
}
