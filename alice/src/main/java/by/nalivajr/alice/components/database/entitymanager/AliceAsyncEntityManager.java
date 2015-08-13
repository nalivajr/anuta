package by.nalivajr.alice.components.database.entitymanager;

import by.nalivajr.alice.callbacks.database.ActionCallback;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.query.AliceQuery;

import java.util.Collection;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AliceAsyncEntityManager extends AliceEntityManager {

    <T> void save(T entity, ActionCallback<T> callback);

    <T> void find(Class<T> entityClass, String id, ActionCallback<T> callback);

    <T> void update(T entity, ActionCallback<T> callback);

    <T> void delete(T entity, ActionCallback<Boolean> callback);

    <T> void delete(Class<T> entityClass, String id, ActionCallback<Boolean> callback);

    <T> void findAll(Class<T> entityClass, ActionCallback<List<T>> callback);

    <T> void findByQuery(AliceQuery<T> query, ActionCallback<List<T>> callback);

    <T> void saveAll(Collection<T> entities, ActionCallback<Collection<T>> callback);

    <T> void updateAll(Collection<T> entities, ActionCallback<Collection<T>> callback);

    <T> void deleteAll(Collection<T> entities, ActionCallback<Boolean> callback);

    <T> void deleteAll(Class<T> entityClass, Collection<String> ids, ActionCallback<Boolean> callback);

    <T> void getEntityCursor(AliceQuery<T> query, ActionCallback<AliceEntityCursor<T>> callback);

    void cancelAll();
}
