package by.nalivajr.alice.sample.database.models;

import android.provider.BaseColumns;

import by.nalivajr.alice.annonatations.database.Column;
import by.nalivajr.alice.annonatations.database.Entity;
import by.nalivajr.alice.annonatations.database.Id;
import by.nalivajr.alice.annonatations.database.ManyToMany;
import by.nalivajr.alice.annonatations.database.RelatedEntity;
import by.nalivajr.alice.sample.database.Contract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = Contract.AUTHORITY)
public class Game {

    @Id
    @Column(BaseColumns._ID)
    private Long id;

    @Column
    private String name;

    @RelatedEntity(dependentEntityClass = Game.class)
    private User creator;

    @RelatedEntity(dependentEntityClass = Game.class)
    private Group officialGroup;

    @ManyToMany
    private Group[] fanGroups;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Group getOfficialGroup() {
        return officialGroup;
    }

    public void setOfficialGroup(Group officialGroup) {
        this.officialGroup = officialGroup;
    }

    public Group[] getFanGroups() {
        return fanGroups;
    }

    public void setFanGroups(Group[] fanGroups) {
        this.fanGroups = fanGroups;
    }
}
