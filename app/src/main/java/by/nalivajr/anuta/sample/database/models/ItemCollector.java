package by.nalivajr.anuta.sample.database.models;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collection;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.FetchType;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.OneToMany;
import by.nalivajr.anuta.sample.database.Contract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */

@Entity(authority = Contract.AUTHORITY)
public class ItemCollector extends ArrayList<SampleItem> {

    @Id
    @Column(BaseColumns._ID)
    private Long rowId;

    @Column
    private String collectorId;

    @OneToMany(fetchType = FetchType.EAGER)
    private Collection<SampleItem> items = this;

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public Collection<SampleItem> getItems() {
        return items;
    }

    public void setItems(Collection<SampleItem> items) {
        this.items = items;
    }
}
