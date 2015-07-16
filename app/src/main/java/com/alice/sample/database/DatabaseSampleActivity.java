package com.alice.sample.database;

import android.app.Activity;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alice.annonatations.ui.AutoActivity;
import com.alice.annonatations.ui.InnerView;
import com.alice.components.database.AliceEntityManager;
import com.alice.components.database.query.AliceQuery;
import com.alice.components.database.query.AliceQueryBuilder;
import com.alice.components.database.query.Restriction;
import com.alice.sample.R;
import com.alice.sample.database.models.SubSubItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private AliceEntityManager entityManager;

    private SubSubItem oldestEntity = null;

    List<SubSubItem> subSubItemsList = new ArrayList<SubSubItem>(1000);
    private int testCount = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        entityManager = new SampleEntityManager(DatabaseSampleActivity.this);

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


                long start = System.currentTimeMillis();
                List<SubSubItem> itemsList = entityManager.findByQuery(query);
                subSubItemsList.clear();
                int size = itemsList.size() > testCount ? testCount : itemsList.size();
                subSubItemsList.addAll(itemsList.subList(0, size));
                long end = System.currentTimeMillis();
                Toast.makeText(DatabaseSampleActivity.this, String.format("To read %d items were spent %d millis", itemsList.size(), (end - start)), Toast.LENGTH_SHORT).show();
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
