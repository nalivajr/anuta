package by.nalivajr.anuta.test.models;

import android.provider.BaseColumns;

import java.util.Arrays;
import java.util.List;

import by.nalivajr.anuta.annonatations.database.CascadeType;
import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.ManyToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.test.content.TestContract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = TestContract.AUTHORITY)
public class Technology {

    @Id
    @Column(BaseColumns._ID)
    private Long id;

    @Column
    private String name;

    @RelatedEntity(dependentEntityClass = Technology.class)
    private Department department;

    @ManyToMany(relationColumnName = "name", cascadeType = CascadeType.ALL)
    private List<Tag> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Tag[] getTags() {
        if (this.tags == null) {
            return null;
        }
        return tags.toArray(new Tag[this.tags.size()]);
    }

    public void setTags(Tag[] tags) {
        this.tags = null;
        if (tags != null) {
            this.tags = Arrays.asList(tags);
        }
    }
}
