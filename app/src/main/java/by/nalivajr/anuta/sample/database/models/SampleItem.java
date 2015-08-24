package by.nalivajr.anuta.sample.database.models;

import android.provider.BaseColumns;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.sample.database.Contract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = Contract.AUTHORITY)
public class SampleItem {

    @Id
    @Column(BaseColumns._ID)
    private Long rowId;

    @Column
    private String name;

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
