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
}
