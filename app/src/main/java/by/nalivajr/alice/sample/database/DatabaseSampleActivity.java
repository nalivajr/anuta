package by.nalivajr.alice.sample.database;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import by.nalivajr.alice.annonatations.ui.AutoActivity;
import by.nalivajr.alice.annonatations.ui.InnerView;
import by.nalivajr.alice.callbacks.execution.AbstractUiOnlyCallback;
import by.nalivajr.alice.components.adapters.AliceAbstractAdapter;
import by.nalivajr.alice.components.adapters.data.binder.DataBinder;
import by.nalivajr.alice.components.database.entitymanager.AliceEntityManager;
import by.nalivajr.alice.components.database.entitymanager.EntityManagerAsyncWrapper;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;
import by.nalivajr.alice.components.database.query.BaseAliceQueryBuilder;
import by.nalivajr.alice.components.execution.SingleThreadActionExecutor;
import by.nalivajr.alice.sample.R;
import by.nalivajr.alice.sample.database.models.SubSubItem;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@AutoActivity(layoutId = R.layout.ac_database_sample)
public class DatabaseSampleActivity extends Activity {


    @InnerView(R.id.btn_save_db)
    private Button saveDbBtn;
    @InnerView(R.id.et_save_amount)
    private EditText saveAmountText;
    @InnerView(R.id.chb_save_one)
    private CheckBox saveOneBox;

    @InnerView(R.id.btn_find_db)
    private Button findDbBtn;
    @InnerView(R.id.et_find_amount)
    private EditText findAmountText;
    @InnerView(R.id.chb_find_all)
    private CheckBox findAllBox;

    @InnerView(R.id.btn_update_db)
    private Button updateDbBtn;
    @InnerView(R.id.chb_update_last)
    private CheckBox updateLastFoundBox;

    @InnerView(R.id.btn_delete_db)
    private Button deleteDbBtn;
    @InnerView(R.id.chb_delete_last_found)
    private CheckBox deleteLastFoundBox;

    @InnerView(R.id.lv_cursor_adapter_sample)
    private ListView listView;

    @InnerView(R.id.rb_toggle_list)
    private CheckBox toggleListBtn;

    private AliceEntityManager entityManager;
    private SingleThreadActionExecutor executor = new SingleThreadActionExecutor();

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
        initCheckBoxListeners();
        initButtonsListeners();
    }

    private void initCheckBoxListeners() {
        findAllBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !findAllBox.isChecked();
                findAmountText.setEnabled(enabled);
            }
        });

        saveOneBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !saveOneBox.isChecked();
                saveAmountText.setEnabled(enabled);
            }
        });

        toggleListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = toggleListBtn.isChecked();
                if (enabled) {
                    listView.setEnabled(true);
                    initListView();
                } else {
                    listView.setAdapter(null);
                    System.gc();
                    listView.setEnabled(false);
                }
            }
        });
    }

    private void initButtonsListeners() {
        saveDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        findDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFindClicked();
            }
        });

        updateDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateClicked();
            }
        });

        deleteDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClicked();
            }
        });
    }

    protected void onFindClicked() {
        int count = getNumberCount(findAmountText);

        AliceQueryBuilder<SubSubItem> builder = entityManager.getQueryBuilder(SubSubItem.class);
        if (!findAllBox.isChecked()) {
            builder.limit(0, count);
        }
        AliceQuery<SubSubItem> query = builder.build();

        EntityManagerAsyncWrapper asyncEntityManager = new EntityManagerAsyncWrapper(entityManager);

        final long cStart = System.currentTimeMillis();
        asyncEntityManager.findByQuery(query, new AbstractUiOnlyCallback<List<SubSubItem>>() {
            @Override
            public void onUiThreadRequested(List<SubSubItem> result, Throwable e) {
                long cEnd = System.currentTimeMillis();
                subSubItemsList.clear();
                if (result != null) {
                    subSubItemsList.addAll(result);
                }
                Toast.makeText(DatabaseSampleActivity.this, String.format("To read %s items were spent %s millis", subSubItemsList.size(), (cEnd - cStart)), Toast.LENGTH_SHORT).show();
            }
        });
//        AliceEntityCursor<SubSubItem> cursor = entityManager.getEntityCursor(query);
    }

    private int getNumberCount(EditText input) {
        int count;
        try {
            count = Integer.parseInt(input.getText().toString());
        } catch (NumberFormatException e) {
            count = 0;
        }
        return count;
    }

    protected void onSaveClicked() {

        AbstractUiOnlyCallback<Pair<String, Long>> callback = createCallback("To save %s items were spent %s millis");

        executor.execute(new Callable<Pair<String, Long>>() {
            @Override
            public Pair<String, Long> call() throws Exception {
                long start = 0;
                long end = 0;
                String amount = "";
                int count = 0;
                String id = "subSubItemId" + System.currentTimeMillis();
                if (saveOneBox.isChecked()) {
                    count = 1;
                } else {
                    count = getNumberCount(saveAmountText);
                }
                subSubItemsList.clear();
                for (int i = 0; i < count; i++) {
                    SubSubItem subSubItem = new SubSubItem();
                    subSubItem.setId(id + i);
                    subSubItem.setSubSubItemData("Sub-sub-item-data");
                    subSubItemsList.add(subSubItem);
                }
                start = System.currentTimeMillis();
                entityManager.saveAll(subSubItemsList);
                end = System.currentTimeMillis();
                amount = String.valueOf(subSubItemsList.size());
                return new Pair<String, Long>(amount, end - start);
            }
        }, callback);
    }

    protected void onUpdateClicked() {

        AbstractUiOnlyCallback<Pair<String, Long>> callback = createCallback("To update %s items were spent %s millis");


        executor.execute(new Callable<Pair<String, Long>>() {
            @Override
            public Pair<String, Long> call() throws Exception {
                long start = 0;
                long end = 0;
                String amount = "";
                List<SubSubItem> itemsToUpdate = null;
                if (updateLastFoundBox.isChecked()) {
                    itemsToUpdate = subSubItemsList;
                    for (SubSubItem subSubItem : itemsToUpdate) {
                        subSubItem.setSubSubItemData("Updated at: " + System.currentTimeMillis());
                    }
                    start = System.currentTimeMillis();
                    entityManager.updateAll(itemsToUpdate);
                    amount = String.valueOf(itemsToUpdate.size());
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("subSubItemData", "Updated at " + System.currentTimeMillis());

                    BaseAliceQueryBuilder<SubSubItem> queryBuilder = new BaseAliceQueryBuilder<>(SubSubItem.class);
                    AliceQuery<SubSubItem> query = queryBuilder.buildUpdate(contentValues);
                    start = System.currentTimeMillis();
                    entityManager.executeQuery(query);
                    amount = "all";
                }
                end = System.currentTimeMillis();
                return new Pair<String, Long>(amount, end - start);
            }
        }, callback);
    }

    protected void onDeleteClicked() {

        AbstractUiOnlyCallback<Pair<String, Long>> callback = createCallback("To delete %s items were spent %s millis");

        executor.execute(new Callable<Pair<String, Long>>() {
            @Override
            public Pair<String, Long> call() throws Exception {
                long start = 0;
                long end = 0;
                String amount = "";
                if (deleteLastFoundBox.isChecked()) {
                    start = System.currentTimeMillis();
                    entityManager.deleteAll(subSubItemsList);
                    amount = String.valueOf(subSubItemsList.size());
                    subSubItemsList.clear();
                } else {
                    BaseAliceQueryBuilder<SubSubItem> queryBuilder = new BaseAliceQueryBuilder<>(SubSubItem.class);
                    AliceQuery<SubSubItem> query = queryBuilder.buildDelete();
                    start = System.currentTimeMillis();
                    entityManager.executeQuery(query);
                    amount = "all";
                }
                end = System.currentTimeMillis();
                return new Pair<String, Long>(amount, end - start);
            }
        }, callback);
    }

    @NonNull
    private AbstractUiOnlyCallback<Pair<String, Long>> createCallback(final String format) {
        return new AbstractUiOnlyCallback<Pair<String, Long>>() {
            @Override
            public void onUiThreadRequested(Pair<String, Long> result, Throwable e) {
                Toast.makeText(DatabaseSampleActivity.this, String.format(format, result.first, result.second), Toast.LENGTH_SHORT).show();
            }
        };
    }
}
