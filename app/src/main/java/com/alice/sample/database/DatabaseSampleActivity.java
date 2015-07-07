package com.alice.sample.database;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alice.annonatations.ui.AutoActivity;
import com.alice.annonatations.ui.InnerView;
import com.alice.components.database.AliceEntityManager;
import com.alice.sample.R;
import com.alice.sample.database.models.SubSubItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        entityManager = new SampleEntityManager(DatabaseSampleActivity.this);

        saveDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SubSubItem subSubItem = new SubSubItem();
                subSubItem.setId("subSubItemId" + System.currentTimeMillis());
                subSubItem.setSubSubItemData("Sub-sub-item-data");
                oldestEntity = entityManager.save(subSubItem);
            }
        });

        findDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long start = System.currentTimeMillis();
                List<SubSubItem> itemsList = entityManager.findAll(SubSubItem.class);
                long end = System.currentTimeMillis();
                oldestEntity = itemsList.isEmpty() ? null : itemsList.get(0);
                Toast.makeText(DatabaseSampleActivity.this, String.format("To read %d items were spent %d millis", itemsList.size(), (end - start)), Toast.LENGTH_LONG).show();
            }
        });

        updateDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldestEntity.setLongDate(new Date());
                entityManager.update(oldestEntity);
            }
        });

        deleteDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldestEntity == null) {
                    return;
                }
                entityManager.delete(oldestEntity);
                oldestEntity = null;
            }
        });
    }
}
