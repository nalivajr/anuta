package com.alice.sample.database.models;

import android.provider.BaseColumns;

import com.alice.annonatations.database.Column;
import com.alice.annonatations.database.Entity;
import com.alice.annonatations.database.EntityCollection;
import com.alice.annonatations.database.Id;
import com.alice.components.database.models.Persistable;

import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = "Item", tableName = "Item", authority = "authority")
public class Item implements Persistable<String> {

    @Id
    @Column("_id")
    private Long rowId;

    @Id
    @Column
    private String id;

    @Column
    private String itemData  = "item-data";

    @EntityCollection
    private List<SubSubItem> subSubItems;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Long getRowId() {
        return rowId;
    }

    @Override
    public void setRowId(Long id) {
        this.rowId = id;
    }

    @Override
    public String getIdColumnName() {
        return BaseColumns._ID;
    }
}
