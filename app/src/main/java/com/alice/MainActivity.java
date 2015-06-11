package com.alice;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.alice.annonatations.AutoActivity;
import com.alice.annonatations.AutoView;
import com.alice.tools.Alice;
import com.nalivajr.android.asbs.R;

@AutoActivity(layoutId = R.layout.ac_main)
public class MainActivity extends ActionBarActivity {

    @AutoView(id = R.id.tv_hello)
    protected TextView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Alice.setContentView(this);
        view.setText("this is text");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
