package by.nalivajr.anuta.sample.database.models;

import android.provider.BaseColumns;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.ManyToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.sample.database.Contract;

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

    @ManyToMany(relationTableName = "GameGroup", joinTableRelationColumnName = "gameId", joinTableRelationReferencedColumnName = "groupId")
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
