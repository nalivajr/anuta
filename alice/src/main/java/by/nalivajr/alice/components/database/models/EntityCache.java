package by.nalivajr.alice.components.database.models;

import java.util.Collection;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface EntityCache {


    <T> Collection<T> getEntitiesOfClass(Class<T> entityClass);

    /**
     * Retrieves entity from cache by its rowId ({@link android.provider.BaseColumns#_ID})
     * @param entityClass the type of entity
     * @param rowId the rowId ({@link android.provider.BaseColumns#_ID}) of entity
     * @return entity instance if it is in cache and null otherwise
     */
    <T> T getByRowId(Class<T> entityClass, Long rowId);

    /**
     * Puts entity into a cache
     * @param entity the entity to be stored
     * @param rowId the rowId ({@link android.provider.BaseColumns#_ID}) of entity
     */
    <T> void put(T entity, Long rowId);

    /**
     * Clears the cache
     */
    void clear();

    /**
     * Checks whether the entity with the given Id already in cache
     * @param entityClass the class of entity
     * @param rowId the {@link android.provider.BaseColumns#_ID} of entity
     * @return {@code true} if entity is in cache and false otherwise
     */
    boolean containsEntity(Class<?> entityClass, Long rowId);
}
