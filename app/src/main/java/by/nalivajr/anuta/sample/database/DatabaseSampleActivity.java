package by.nalivajr.anuta.sample.database;

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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import by.nalivajr.anuta.annonatations.ui.AutoActivity;
import by.nalivajr.anuta.annonatations.ui.InnerView;
import by.nalivajr.anuta.callbacks.execution.AbstractUiOnlyCallback;
import by.nalivajr.anuta.components.adapters.AnutaAbstractAdapter;
import by.nalivajr.anuta.components.adapters.data.binder.DataBinder;
import by.nalivajr.anuta.components.database.entitymanager.AnutaEntityManager;
import by.nalivajr.anuta.components.database.entitymanager.EntityManagerAsyncWrapper;
import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.components.database.query.AnutaQueryBuilder;
import by.nalivajr.anuta.components.database.query.BaseAnutaQueryBuilder;
import by.nalivajr.anuta.components.execution.SingleThreadActionExecutor;
import by.nalivajr.anuta.sample.R;
import by.nalivajr.anuta.sample.database.models.Game;
import by.nalivajr.anuta.sample.database.models.Group;
import by.nalivajr.anuta.sample.database.models.ItemCollector;
import by.nalivajr.anuta.sample.database.models.SampleItem;
import by.nalivajr.anuta.sample.database.models.SubSubItem;
import by.nalivajr.anuta.sample.database.models.User;
import by.nalivajr.anuta.tools.Anuta;

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

    @InnerView(R.id.btn_init_relations_sample)
    private Button initRelationSample;

    @InnerView(R.id.btn_init_alex_test)
    private Button initAlexTest;

    private AnutaEntityManager entityManager;
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
        AnutaQuery<SubSubItem> query = new BaseAnutaQueryBuilder<SubSubItem>(SubSubItem.class).buildFindAllQuery();
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
        AnutaAbstractAdapter<SubSubItem> adapter = Anuta.adapterTools.buildAdapter(this, dataBinder, R.layout.layout_anuta_cursor_adapter_item, query);
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

        initRelationSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunRelationTestClicked();
            }
        });

        initAlexTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //initialization part

                ItemCollector collector = new ItemCollector();
                SampleItem sampleItem1 = new SampleItem();
                sampleItem1.setName("SampleItem1");

                SampleItem sampleItem2 = new SampleItem();
                sampleItem2.setName("SampleItem2");

                collector.getItems().add(sampleItem1);
                collector.getItems().add(sampleItem2);

                Gson gson = new Gson();
                String json = gson.toJson(collector);

                //test part

                collector = gson.fromJson(json, ItemCollector.class);
                entityManager.save(collector);
                String id = String.valueOf(collector.getRowId());

                collector = entityManager.find(ItemCollector.class, id);
            }
        });
    }

    protected void onFindClicked() {
        int count = getNumberCount(findAmountText);

        AnutaQueryBuilder<SubSubItem> builder = entityManager.getQueryBuilder(SubSubItem.class);
        if (!findAllBox.isChecked()) {
            builder.limit(0, count);
        }
        AnutaQuery<SubSubItem> query = builder.build();

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
//        AnutaEntityCursor<SubSubItem> cursor = entityManager.getEntityCursor(query);
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

                    BaseAnutaQueryBuilder<SubSubItem> queryBuilder = new BaseAnutaQueryBuilder<SubSubItem>(SubSubItem.class);
                    AnutaQuery<SubSubItem> query = queryBuilder.buildUpdate(contentValues);
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
                    BaseAnutaQueryBuilder<SubSubItem> queryBuilder = new BaseAnutaQueryBuilder<SubSubItem>(SubSubItem.class);
                    AnutaQuery<SubSubItem> query = queryBuilder.buildDelete();
                    start = System.currentTimeMillis();
                    entityManager.executeQuery(query);
                    amount = "all";
                }
                end = System.currentTimeMillis();
                return new Pair<String, Long>(amount, end - start);
            }
        }, callback);
    }

    protected void onRunRelationTestClicked() {
        User user = createTestUser();

        long saveStart = System.currentTimeMillis();
        user = entityManager.save(user);
        long saveEnd = System.currentTimeMillis();

        long refStart = System.currentTimeMillis();
        user = entityManager.getPlainEntity(User.class, String.valueOf(user.getId()));
        long refEnd = System.currentTimeMillis();

        long initStart = System.currentTimeMillis();
        user = entityManager.initialize(user);
        long initEnd = System.currentTimeMillis();

        Toast.makeText(DatabaseSampleActivity.this, String.format("Saved in: %s millis.\nRef loaded in: %s.\nInitialized in: %s",
                        (saveEnd - saveStart),
                        (refEnd - refStart),
                        (initEnd - initStart)),
                Toast.LENGTH_LONG).show();
    }

    private AbstractUiOnlyCallback<Pair<String, Long>> createCallback(final String format) {
        return new AbstractUiOnlyCallback<Pair<String, Long>>() {
            @Override
            public void onUiThreadRequested(Pair<String, Long> result, Throwable e) {
                Toast.makeText(DatabaseSampleActivity.this, String.format(format, result.first, result.second), Toast.LENGTH_SHORT).show();
            }
        };
    }

    @NonNull
    protected User createTestUser() {
        User user = new User();
        user.setName("Test");
        user.setGender("Male");

        Group groupA = new Group();
        groupA.setGroupCode("Test Group A");

        Group groupB = new Group();
        groupB.setGroupCode("Test Group B");
        user.setCuratingGroup(Arrays.asList(groupA));
        user.setAttendingGroup(new HashSet<Group>(Arrays.asList(groupA, groupB)));

        Game game1 = new Game();
        game1.setName("Test game 1");
        game1.setCreator(user);
        game1.setOfficialGroup(groupA);
        game1.setFanGroups(new Group[]{groupA, groupB});

        Game game2 = new Game();
        game2.setName("Test game 2");
        game2.setCreator(user);
        groupB.setSupportGames(new Game[]{game1, game2});
        return user;
    }
}
