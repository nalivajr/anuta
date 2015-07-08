package com.alice.components.database;

import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AliceEntityManager {

    /**
     * Loads the entity from data source using entity's id
     * @param entityClass the target entity class
     * @param id the id of entity lo load
     * @param <T> the type if target entity
     * @return Java object, representing the entity if object was found and null otherwise
     */
    public <T> T find(Class<T> entityClass, String id);

    /**
     * Updates entity in database
     * @param <T> the type if target entity
     * @return updated entity
     */
    public <T> T update(T entity);

    /**
     * Finds all entities of the given type
     * @param <T> the type if target entity
     * @return list of found entities or empty list if nothing was found
     */
    public <T> List<T> findAll(Class<T> entityClass);

    /**
     * Saves entity in database
     * @param entity the entity to be saved
     * @param <T> the type if target entity
     * @return saved entity
     */
    public <T> T save(T entity);

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
}
