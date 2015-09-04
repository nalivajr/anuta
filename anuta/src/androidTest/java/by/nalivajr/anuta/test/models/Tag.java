package by.nalivajr.anuta.test.models;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.test.content.TestContract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = TestContract.AUTHORITY)
public class Tag {

    @Column
    private Long _id;

    @Id
    @Column
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
