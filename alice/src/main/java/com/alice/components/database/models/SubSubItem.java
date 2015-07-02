package com.alice.components.database.models;

import android.provider.BaseColumns;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.annonatations.db.Id;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = "SubSubItem", tableName = "SubSubItem", authority = "authority", tableUri = "", inheritColumns = Entity.InheritancePolicy.HIERARCHY_COMPOSITE_ID)
public class SubSubItem extends SubItem {

    @Id
    @Column("_id")
    private Long rowId;

    @Id
    @Column("subSubItemId")
    private String id;

    @Column
    private String subSubItemData;

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

    public String getSubSubItemData() {
        return subSubItemData;
    }

    public void setSubSubItemData(String subSubItemData) {
        this.subSubItemData = subSubItemData;
    }
}
