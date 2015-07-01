package com.alice.components.database.models;

import android.provider.BaseColumns;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.annonatations.db.Id;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = "Item", tableName = "Item", authority = "authority")
public class Item implements Persistable<String> {

    @Id
    @Column
    private Integer _id;

    @Id
    @Column
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Integer getRowId() {
        return _id;
    }

    @Override
    public void setRowId(Integer id) {
        this._id = id;
    }

    @Override
    public String getIdColumnName() {
        return BaseColumns._ID;
    }
}
