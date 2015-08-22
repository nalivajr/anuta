package by.nalivajr.alice.sample.database.models;

import android.provider.BaseColumns;

import java.util.List;
import java.util.Set;

import by.nalivajr.alice.annonatations.database.Column;
import by.nalivajr.alice.annonatations.database.Entity;
import by.nalivajr.alice.annonatations.database.Id;
import by.nalivajr.alice.annonatations.database.ManyToMany;
import by.nalivajr.alice.annonatations.database.OneToMany;
import by.nalivajr.alice.annonatations.database.RelatedEntity;
import by.nalivajr.alice.sample.database.Contract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = Contract.AUTHORITY, tableName = "BaseGroup")
public class Group {

    @Id
    @Column(BaseColumns._ID)
    private Long id;

    @Column
    private String groupCode;

    @Column
    private String gender;

    @RelatedEntity(dependentEntityClass = Group.class)
    private User user;

    @RelatedEntity(dependentEntityClass = Game.class)
    private Game officialGame;

    @OneToMany(relationColumnName = "gender", relationReferencedColumnName = "gender")
    private List<User> genderUsers;

    @ManyToMany
    private Game[] supportGames;

    @ManyToMany
    private Set<User> attendingUsers;

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getOfficialGame() {
        return officialGame;
    }

    public void setOfficialGame(Game officialGame) {
        this.officialGame = officialGame;
    }

    public List<User> getGenderUsers() {
        return genderUsers;
    }

    public void setGenderUsers(List<User> genderUsers) {
        this.genderUsers = genderUsers;
    }

    public Game[] getSupportGames() {
        return supportGames;
    }

    public void setSupportGames(Game[] supportGames) {
        this.supportGames = supportGames;
    }

    public Set<User> getAttendingUsers() {
        return attendingUsers;
    }

    public void setAttendingUsers(Set<User> attendingUsers) {
        this.attendingUsers = attendingUsers;
    }
}
