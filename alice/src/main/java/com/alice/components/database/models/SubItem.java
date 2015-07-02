package com.alice.components.database.models;

import android.provider.BaseColumns;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.annonatations.db.Id;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = "SubItem", tableName = "SubItem", authority = "authority", tableUri = "", inheritColumns = Entity.InheritancePolicy.SEQUENTIAL_NO_ID)
public class SubItem extends Item implements Persistable<String> {

    @Id
    @Column("_id")
    private Long rowId;

    @Id
    @Column("subItemId")
    private String id;

    @Column
    private String subItemData;

    @Override
    public String getId() {
        return id;
    }

    @Override
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

    public String getSubItemData() {
        return subItemData;
    }

    public void setSubItemData(String subItemData) {
        this.subItemData = subItemData;
    }
}
