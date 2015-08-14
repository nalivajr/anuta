package by.nalivajr.alice.sample.database.models;

import android.provider.BaseColumns;

import by.nalivajr.alice.annonatations.database.Column;
import by.nalivajr.alice.annonatations.database.Entity;
import by.nalivajr.alice.annonatations.database.Id;
import by.nalivajr.alice.components.database.models.Persistable;

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