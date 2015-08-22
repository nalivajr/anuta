package by.nalivajr.anuta.sample.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.annonatations.ui.AutoActivity;
import by.nalivajr.anuta.annonatations.ui.InnerView;
import by.nalivajr.anuta.sample.R;
import by.nalivajr.anuta.sample.adapters.SampleAnutaAdapter;
import by.nalivajr.anuta.sample.models.Person;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@AutoActivity(layoutId = R.layout.ac_adatpter)
public class AdapterSampleActivity extends Activity {

    @InnerView(R.id.lv_adapter_sample)
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Person> personList = createTestDataList();

        Map<Integer, List<Integer>> subViewIds = Anuta.adapterTools.buildIdsMap(this, R.layout.anuta_adapter_item_left, R.layout.anuta_adapter_item_right);
        listView.setAdapter(new SampleAnutaAdapter(this, personList, subViewIds));
    }

    @NonNull
    private List<Person> createTestDataList() {
        List<Person> personList = new ArrayList();
        personList.add(new Person("user", "one", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "two", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "three", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "four", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "five", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "six", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "seven", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "eight", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "nine", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "ten", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "eleven", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "twelve", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "thirteen", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "fourteen", android.R.drawable.ic_menu_zoom));
        personList.add(new Person("user", "fifteen", android.R.drawable.ic_menu_zoom));
        return personList;
    }
}
