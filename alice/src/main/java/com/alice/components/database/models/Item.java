package com.alice.components.database.models;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.annonatations.db.Id;
import com.alice.components.database.Identifiable;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = "Item", tableName = "Item")
public class Item implements Identifiable<String> {

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
}
