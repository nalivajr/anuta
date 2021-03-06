package by.nalivajr.anuta.sample.database.models;

import android.provider.BaseColumns;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.components.database.models.Persistable;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = "SubItem", tableName = "SubItem", authority = "authority",
        inheritColumns = Entity.InheritancePolicy.SEQUENTIAL_NO_ID)
public class SubItem implements Persistable<String> {

    @Id
    @Column("_id")
    private Long rowId;

    @Id
    @Column("subItemId")
    private String id;

    @Column
    private String subItemData = "sub_item_data";

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

    public String getSubItemData() {
        return subItemData;
    }

    public void setSubItemData(String subItemData) {
        this.subItemData = subItemData;
    }
}
