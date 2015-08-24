package by.nalivajr.anuta.sample.database.models;

import android.provider.BaseColumns;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.sample.database.Contract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = Contract.AUTHORITY)
public class GameGroup {

    @Id
    @Column(BaseColumns._ID)
    private Long rowId;

    @RelatedEntity(relationColumnName = "groupId", dependentEntityClass = GameGroup.class)
    private Group group;

    @RelatedEntity(relationColumnName = "gameId", dependentEntityClass = GameGroup.class)
    private Game game;
}
