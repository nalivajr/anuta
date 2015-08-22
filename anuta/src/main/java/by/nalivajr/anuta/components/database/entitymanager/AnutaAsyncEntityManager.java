package by.nalivajr.anuta.components.database.entitymanager;

import java.util.Collection;
import java.util.List;

import by.nalivajr.anuta.callbacks.execution.ActionCallback;
import by.nalivajr.anuta.components.database.cursor.AnutaEntityCursor;
import by.nalivajr.anuta.components.database.models.DatabaseAccessSession;
import by.nalivajr.anuta.components.database.query.AnutaQuery;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AnutaAsyncEntityManager extends AnutaEntityManager {

    <T> void save(T entity, ActionCallback<T> callback);

    <T> void find(Class<T> entityClass, String id, ActionCallback<T> callback);

    <T> void update(T entity, ActionCallback<T> callback);

    <T> void delete(T entity, ActionCallback<Boolean> callback);

    <T> void delete(Class<T> entityClass, String id, ActionCallback<Boolean> callback);

    <T> void findAll(Class<T> entityClass, ActionCallback<List<T>> callback);

    <T> void findByQuery(AnutaQuery<T> query, ActionCallback<List<T>> callback);

    <T> void saveAll(Collection<T> entities, ActionCallback<Collection<T>> callback);

    <T> void updateAll(Collection<T> entities, ActionCallback<Collection<T>> callback);

    <T> void deleteAll(Collection<T> entities, ActionCallback<Boolean> callback);

    <T> void deleteAll(Class<T> entityClass, Collection<String> ids, ActionCallback<Boolean> callback);

    <T> void getEntityCursor(AnutaQuery<T> query, ActionCallback<AnutaEntityCursor<T>> callback);

    <T> void executeQuery(AnutaQuery<T> query, ActionCallback<Boolean> callback);

    /**
     * Loads entity without loading all inner collections
     * @param id the id of the entity
     * @param callback the callback to pass loaded entity, if exists and null otherwise
     */
    public <T> void getPlainEntity(Class<T> entityClass, String id, ActionCallback<T> callback);


    /**
     * Initializes all related collections and entities
     * @param entity the entity to be initialized
     * @param callback the callback to pass initialized entity as result. WARNING! The reference of returned object will differ from passed at params
     */
    public <T> void initialize(T entity, ActionCallback<T> callback);

    /**
     * Initializes all related collections and entities up to the given level
     * @param entity the entity to be initialized
     * @param level the level of initialization (greater then 0 or one of the following)
     *              <ul>
     *              <li>{@link DatabaseAccessSession#LEVEL_ENTITY_ONLY} - to load plain entity</li>
     *              <li>{@link DatabaseAccessSession#LEVEL_ALL} - to load entity and all dependent collections</li>
     *              <li>{@link DatabaseAccessSession#LEVEL_ANNOTATION_BASED} - to load entity and collections will be loaded accorging to annotation config</li>
     *              </ul>
     * @param callback the callback to pass initialized entity as result. WARNING! The reference of returned object will differ from passed at params
     */
    public <T> void initialize(T entity, int level, ActionCallback<T> callback);

    void cancelAll();
}
