package by.nalivajr.alice.components.database.entitymanager;

import java.util.Collection;
import java.util.List;

import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.models.DatabaseAccessSession;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AliceEntityManager {

    /**
     * Saves entity in database
     * @param entity the entity to be saved
     * @param <T> the type if target entity
     * @return saved entity
     */
    public <T> T save(T entity);

    /**
     * Loads the entity from data source using entity's id
     * @param entityClass the target entity class
     * @param id the id of entity lo load
     * @param <T> the type if target entity
     * @return Java object, representing the entity if object was found and null otherwise
     */
    public <T> T find(Class<T> entityClass, String id);


    /**
     * Loads entity without loading all inner collections
     * @param id the id of the entity
     * @return loaded entity, if exists and null otherwise
     */
    public <T> T getPlainEntity(Class<T> entityClass, String id);

    /**
     * Updates entity in database
     * @param <T> the type if target entity
     * @return updated entity
     */
    public <T> T update(T entity);

    /**
     * Deletes entity from database
     * @param entity the entity to be deleted
     * @param <T> the type if target entity
     * @return true if was removed and false otherwise
     */
    public <T> boolean delete(T entity);

    /**
     * Deletes entity from database
     * @param entityClass the class of entity to be deleted
     * @param id the id of entity to be removed
     * @param <T> the type if target entity
     * @return true if was removed and false otherwise
     */
    public <T> boolean delete(Class<T> entityClass, String id);

    /**
     * Finds all entities of the given type
     * @param <T> the type if target entity
     * @return list of found entities or empty list if nothing was found
     */
    public <T> List<T> findAll(Class<T> entityClass);

    /**
     * Finds all entities of the given type
     * @param <T> the type if target entity
     * @return list of found entities or empty list if nothing was found
     */
    public <T> List<T> findByQuery(AliceQuery<T> query);

    /**
     * Saves all entities into database
     * @param <T> the type if target entity
     * @return collection of saved entities
     */
    public <T> Collection<T> saveAll(Collection<T> entities);

    /**
     * Updates all entities in database
     * @param <T> the type if target entity
     * @return collection of updated entities
     */
    public <T> Collection<T> updateAll(Collection<T> entities);

    /**
     * Deletes all entities in database
     * @param <T> the type if target entity
     * @return collection of updated entities
     */
    public <T> boolean deleteAll(Collection<T> entities);

    /**
     * Deletes all entities in database
     * @param ids collection of ids for entities to be deleted
     * @param <T> the type if target entity
     * @return collection of updated entities
     */
    public <T> boolean deleteAll(Class<T> entityClass, Collection<String> ids);

    /**
     * @return the instance of {@link AliceQueryBuilder} which can be used to build query
     */
    public <T> AliceQueryBuilder<T> getQueryBuilder(Class<T> cls);

    /**
     * Queries data from DB and coverts data from cursor to Java entity.
     * @param query the query to select objects
     * @param <T> type of target entity
     * @return an instance of {@link AliceEntityCursor} which can be used to iterate objects from database
     */
    public <T> AliceEntityCursor<T> getEntityCursor(AliceQuery<T> query);

    /**
     * Executes query and returns true if query was executed successfully and false otherwise
     * @param query the query to be executed
     * @return {@code true} if query was executed successfully and {@code false} otherwise
     */
    public <T> boolean executeQuery(AliceQuery<T> query);

    /**
     * Initializes all related collections and entities
     * @param entity the entity to be initialized
     * @return initialized entity. WARNING! The reference of returned object will differ from passed at params
     */
    public <T> T initialize(T entity);

    /**
     * Initializes all related collections and entities up to the given level
     * @param entity the entity to be initialized
     * @param level the level of initialization (greater then 0 or one of the following)
     *              <ul>
     *              <li>{@link DatabaseAccessSession#LEVEL_ENTITY_ONLY} - to load plain entity</li>
     *              <li>{@link DatabaseAccessSession#LEVEL_ALL} - to load entity and all dependent collections</li>
     *              <li>{@link DatabaseAccessSession#LEVEL_ANNOTATION_BASED} - to load entity and collections will be loaded accorging to annotation config</li>
     *              </ul>
     * @return initialized entity. WARNING! The reference of returned object will differ from passed at params
     */
    public <T> T initialize(T entity, int level);
}
