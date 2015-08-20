package by.nalivajr.alice.sample.database.models;

import android.provider.BaseColumns;

import java.util.Collection;
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
@Entity(authority = Contract.AUTHORITY)
public class User {

    @Id
    @Column (BaseColumns._ID)
    private Long id;

    @Column
    private String name;

    @Column
    private String gender;

    @RelatedEntity(relationColumnName = "gender", relationReferencedColumnName = "gender", dependentEntityClass = Group.class)
    private Group genderGroup;

    @OneToMany
    private Collection<Group> curatingGroup;

    @ManyToMany
    private Set<Group> attendingGroup;

    @ManyToMany
    private Set<Game> favouriteGames;
}
