package com.alice.components.database;

import com.alice.components.database.models.Persistable;

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
     * @param <I> the type of entity identifier. Be aware that id is converted to JSON string if {@code I}
     *           neither instance of {@link Number} nor {@link java.lang.String}
     * @param <T> the type if target entity
     * @return Java object, representing the entity if object was found and null otherwise
     */
    public <I, T extends Persistable<I>> T find(Class<T> entityClass, I id);

    /**
     * Updates entity in database
     * @param <T> the type if target entity
     * @param <I> the type of entity identifier. Be aware that id is converted to JSON string if {@code I}
     *           neither instance of {@link Number} nor {@link java.lang.String}
     * @return updated entity
     */
    public <I, T extends Persistable<I>> T update(T entity);

    /**
     * Finds all entities of the given type
     * @param <T> the type if target entity
     * @return list of found entities or empty list if nothing was found
     */
    public <I, T extends Persistable<I>> List<T> findAll(Class<T> entityClass);

    /**
     * Saves entity in database
     * @param entity the entity to be saved
     * @param <T> the type if target entity
     * @param <I> the type of entity identifier. Be aware that id is converted to JSON string if {@code I}
     *           neither instance of {@link Number} nor {@link java.lang.String}
     * @return saved entity
     */
    public <I, T extends Persistable<I>> T save(T entity);

    /**
     * Deletes entity from database
     * @param entity the entity to be deleted
     * @param <T> the type if target entity
     * @param <I> the type of entity identifier. Be aware that id is converted to JSON string if {@code I}
     *           neither instance of {@link Number} nor {@link java.lang.String}
     * @return true if was removed and false otherwise
     */
    public <I, T extends Persistable<I>> boolean delete(T entity);

    /**
     * Deletes entity from database
     * @param entityClass the class of entity to be deleted
     * @param id the id of entity to be removed
     * @param <T> the type if target entity
     * @param <I> the type of entity identifier. Be aware that id is converted to JSON string if {@code I}
     *           neither instance of {@link Number} nor {@link java.lang.String}
     * @return true if was removed and false otherwise
     */
    public <I, T extends Persistable<I>> boolean delete(Class<T> entityClass, I id);
}
