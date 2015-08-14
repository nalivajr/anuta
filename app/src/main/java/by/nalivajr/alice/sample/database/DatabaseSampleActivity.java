package by.nalivajr.alice.sample.database;

import android.app.Activity;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import by.nalivajr.alice.annonatations.ui.AutoActivity;
import by.nalivajr.alice.annonatations.ui.InnerView;
import by.nalivajr.alice.callbacks.execution.AbstractUiOnlyCallback;
import by.nalivajr.alice.components.adapters.AliceAbstractAdapter;
import by.nalivajr.alice.components.adapters.data.binder.DataBinder;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.entitymanager.AliceEntityManager;
import by.nalivajr.alice.components.database.entitymanager.EntityManagerAsyncWrapper;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;
import by.nalivajr.alice.components.database.query.BaseAliceQueryBuilder;
import by.nalivajr.alice.components.database.query.Restriction;
import by.nalivajr.alice.sample.R;
import by.nalivajr.alice.sample.database.models.SubSubItem;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@AutoActivity(layoutId = R.layout.ac_database_sample)
public class DatabaseSampleActivity extends Activity{


    @InnerView(R.id.btn_save_db)
    private Button saveDbBtn;

    @InnerView(R.id.btn_find_db)
    private Button findDbBtn;

    @InnerView(R.id.btn_update_db)
    private Button updateDbBtn;

    @InnerView(R.id.btn_delete_db)
    private Button deleteDbBtn;

    @InnerView(R.id.lv_cursor_adapter_sample)
    private ListView listView;

    private AliceEntityManager entityManager;

    private SubSubItem oldestEntity = null;

    private int testCount = 10;
    private List<SubSubItem> subSubItemsList = new ArrayList<SubSubItem>(testCount);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        entityManager = new SampleEntityManager(DatabaseSampleActivity.this);
        initListView();

        initListeners();
    }

    private void initListView() {
        AliceQuery<SubSubItem> query = new BaseAliceQueryBuilder<SubSubItem>(SubSubItem.class).buildFindAllQuery();
        DataBinder<SubSubItem> dataBinder = new DataBinder<SubSubItem>() {
            @Override
            public void bindView(View view, int itemLayoutId, Integer viewId, SubSubItem item) {
                if (viewId == R.id.tv_cursor_adapter_item_id) {
                    ((TextView) view).setText(String.valueOf(item.getRowId()));
                }
                if (viewId == R.id.tv_cursor_adapter_item_data) {
                    ((TextView) view).setText(String.valueOf(item.getSubSubItemData()));
                }
            }
        };
        AliceAbstractAdapter<SubSubItem> adapter = Alice.adapterTools.buildAdapter(this, dataBinder, R.layout.layout_alice_cursor_adapter_item, query);
        listView.setAdapter(adapter);
    }

    private void initListeners() {
        saveDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long time = 0;
                String id = "subSubItemId" + System.currentTimeMillis();
                subSubItemsList.clear();
                for (int i = 0; i < testCount; i++) {
                    SubSubItem subSubItem = new SubSubItem();
                    subSubItem.setId(id + i);
                    subSubItem.setSubSubItemData("Sub-sub-item-data");
                    subSubItemsList.add(subSubItem);
                }
                long start = System.currentTimeMillis();
                entityManager.saveAll(subSubItemsList);
                oldestEntity = subSubItemsList.get(0);
                long end = System.currentTimeMillis();
                time += (end - start);
                Toast.makeText(DatabaseSampleActivity.this, String.format("To save %d items were spent %d millis", subSubItemsList.size(), time), Toast.LENGTH_SHORT).show();
            }
        });

        findDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AliceQueryBuilder<SubSubItem> builder = entityManager.getQueryBuilder(SubSubItem.class);
                Restriction restriction = builder.notEqual(BaseColumns._ID, "51");
                Restriction restriction3 = builder.notIn(BaseColumns._ID, new String[]{"52", "53"});
                AliceQuery<SubSubItem> query = builder
                        .and(restriction)
                        .and(restriction3).build();


                EntityManagerAsyncWrapper asyncEntityManager = new EntityManagerAsyncWrapper(entityManager);

                final long cStart = System.currentTimeMillis();
                AliceEntityCursor<SubSubItem> cursor = entityManager.getEntityCursor(query);
                final long cEnd = System.currentTimeMillis();
                Toast.makeText(DatabaseSampleActivity.this, String.format("To read %d items were spent %d millis", cursor.getCount(), (cEnd - cStart)), Toast.LENGTH_SHORT).show();

                final long start = System.currentTimeMillis();

                asyncEntityManager.findByQuery(query, new AbstractUiOnlyCallback<List<SubSubItem>>() {

                    @Override
                    public void onUiThreadRequested(List<SubSubItem> result, Throwable e) {
                        if (e != null) {
                            Toast.makeText(DatabaseSampleActivity.this, "An error during query: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        subSubItemsList.clear();
                        int size = result.size() > testCount ? testCount : result.size();
                        subSubItemsList.addAll(result.subList(0, size));
                        final long end = System.currentTimeMillis();

                        Toast.makeText(DatabaseSampleActivity.this, String.format("To read %d items were spent %d millis", result.size(), (end - start)), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        updateDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldestEntity.setLongDate(new Date());
                for (SubSubItem subSubItem : subSubItemsList) {
                    subSubItem.setSubSubItemData("Updated at: " + System.currentTimeMillis());
                }
                long start = System.currentTimeMillis();
                entityManager.updateAll(subSubItemsList);
                long end = System.currentTimeMillis();
                Toast.makeText(DatabaseSampleActivity.this, String.format("To update %d items were spent %d millis", subSubItemsList.size(), (end - start)), Toast.LENGTH_SHORT).show();
            }
        });

        deleteDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = subSubItemsList.size();
                long start = System.currentTimeMillis();
                boolean deleted = entityManager.deleteAll(subSubItemsList);
                long end = System.currentTimeMillis();
                if (deleted) {
                    subSubItemsList.clear();
                }
                Toast.makeText(DatabaseSampleActivity.this, String.format("To delete %d items were spent %d millis", size, (end - start)), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
