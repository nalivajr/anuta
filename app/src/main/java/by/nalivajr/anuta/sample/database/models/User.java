package by.nalivajr.anuta.sample.database.models;

import android.provider.BaseColumns;

import java.util.Collection;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.CascadeType;
import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.ManyToMany;
import by.nalivajr.anuta.annonatations.database.OneToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.sample.database.Contract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = Contract.AUTHORITY)
public class User {

    @Id
    @Column (BaseColumns._ID)
    private Long id;

    @Column
    private String name;

    @Column
    private String gender;

    @RelatedEntity(relationColumnName = "gender", relationReferencedColumnName = "gender", dependentEntityClass = User.class)
    private Group genderGroup;

    @OneToMany(cascadeType = CascadeType.ALL)
    private Collection<Group> curatingGroup;

    @ManyToMany
    private Set<Group> attendingGroup;

    @ManyToMany
    private Set<Game> favouriteGames;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Group getGenderGroup() {
        return genderGroup;
    }

    public void setGenderGroup(Group genderGroup) {
        this.genderGroup = genderGroup;
    }

    public Collection<Group> getCuratingGroup() {
        return curatingGroup;
    }

    public void setCuratingGroup(Collection<Group> curatingGroup) {
        this.curatingGroup = curatingGroup;
    }

    public Set<Group> getAttendingGroup() {
        return attendingGroup;
    }

    public void setAttendingGroup(Set<Group> attendingGroup) {
        this.attendingGroup = attendingGroup;
    }

    public Set<Game> getFavouriteGames() {
        return favouriteGames;
    }

    public void setFavouriteGames(Set<Game> favouriteGames) {
        this.favouriteGames = favouriteGames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
