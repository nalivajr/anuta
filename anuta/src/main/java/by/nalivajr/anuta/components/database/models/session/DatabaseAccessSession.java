package by.nalivajr.anuta.components.database.models.session;

import by.nalivajr.anuta.components.database.models.cache.EntityCache;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface DatabaseAccessSession {

    /**
     * Loads only entity without related entities
     */
    public static final int LEVEL_ENTITY_ONLY = 0;

    /**
     * Loads entities according annotation configuration
     */
    public static final int LEVEL_ANNOTATION_BASED = -1;

    /**
     * Loads entities ignoring annotations configuration (EAGER-like)
     */
    public static final int LEVEL_ALL = -2;

    /**
     * @return the instance of {@link EntityCache} which is used during db access operation
     */
    public EntityCache getCache();

    /**
     * Specifies the level of loading for session
     */
    public int getLoadLevel();

    /**
     * Specifies the level of entities loading for session
     */
    public void setLoadLevel(int level);

}
