package by.nalivajr.alice.components.database.entitymanager;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import by.nalivajr.alice.callbacks.execution.ActionCallback;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */

/**
 * Provides a simple implementation of {@link AliceAsyncEntityManager}, wrapping an existing instance of
 * {@link AliceEntityManager}. Supports both sync and async modes
 */
public class EntityManagerAsyncWrapper implements AliceAsyncEntityManager {

    public static final int POOL_SIZE = 5;
    private final AliceEntityManager entityManager;
    private ExecutorService executorService;

    public EntityManagerAsyncWrapper(AliceEntityManager entityManager) {
        this.entityManager = entityManager;
        executorService = initExecutorService();
    }

    @NonNull
    protected ExecutorService initExecutorService() {
        return Executors.newFixedThreadPool(POOL_SIZE);
    }

    @Override
    public <T> void save(final T entity, final ActionCallback<T> callback) {
        executorService.submit(createTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return entityManager.save(entity);
            }
        }, callback));
    }

    @Override
    public <T> void find(final Class<T> entityClass, final String id, ActionCallback<T> callback) {
        executorService.submit(createTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return entityManager.find(entityClass, id);
            }
        }, callback));
    }

    @Override
    public <T> void update(final T entity, ActionCallback<T> callback) {
        executorService.submit(createTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return entityManager.update(entity);
            }
        }, callback));
    }

    @Override
    public <T> void delete(final T entity, ActionCallback<Boolean> callback) {
        executorService.submit(createTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return entityManager.delete(entity);
            }
        }, callback));
    }

    @Override
    public <T> void delete(final Class<T> entityClass, final String id, ActionCallback<Boolean> callback) {
        executorService.submit(createTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return entityManager.delete(entityClass, id);
            }
        }, callback));
    }

    @Override
    public <T> void findAll(final Class<T> entityClass, ActionCallback<List<T>> callback) {
        executorService.submit(createTask(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return entityManager.findAll(entityClass);
            }
        }, callback));
    }

    @Override
    public <T> void findByQuery(final AliceQuery<T> query, ActionCallback<List<T>> callback) {
        executorService.submit(createTask(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return entityManager.findByQuery(query);
            }
        }, callback));
    }

    @Override
    public <T> void saveAll(final Collection<T> entities, ActionCallback<Collection<T>> callback) {
        executorService.submit(createTask(new Callable<Collection<T>>() {
            @Override
            public Collection<T> call() throws Exception {
                return entityManager.saveAll(entities);
            }
        }, callback));
    }

    @Override
    public <T> void updateAll(final Collection<T> entities, ActionCallback<Collection<T>> callback) {
        executorService.submit(createTask(new Callable<Collection<T>>() {
            @Override
            public Collection<T> call() throws Exception {
                return entityManager.updateAll(entities);
            }
        }, callback));
    }

    @Override
    public <T> void deleteAll(final Collection<T> entities, ActionCallback<Boolean> callback) {
        executorService.submit(createTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return entityManager.deleteAll(entities);
            }
        }, callback));
    }

    @Override
    public <T> void deleteAll(final Class<T> entityClass, final Collection<String> ids, ActionCallback<Boolean> callback) {
        executorService.submit(createTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return entityManager.deleteAll(entityClass, ids);
            }
        }, callback));
    }

    @Override
    public <T> void getEntityCursor(final AliceQuery<T> query, ActionCallback<AliceEntityCursor<T>> callback) {
        executorService.submit(createTask(new Callable<AliceEntityCursor<T>>() {
            @Override
            public AliceEntityCursor<T> call() throws Exception {
                return entityManager.getEntityCursor(query);
            }
        }, callback));
    }

    @Override
    public <T> void getPlainEntity(final Class<T> entityClass, final String id, ActionCallback<T> callback) {
        executorService.submit(createTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return entityManager.getPlainEntity(entityClass, id);
            }
        }, callback));
    }

    @Override
    public <T> void initialize(final T entity, ActionCallback<T> callback) {
        executorService.submit(createTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return entityManager.initialize(entity);
            }
        }, callback));
    }

    @Override
    public <T> void initialize(final T entity, final int level, ActionCallback<T> callback) {
        executorService.submit(createTask(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return entityManager.initialize(entity, level);
            }
        }, callback));
    }

    @Override
    public <T> T save(T entity) {
        return entityManager.save(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, String id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public <T> T update(T entity) {
        return entityManager.update(entity);
    }

    @Override
    public <T> boolean delete(T entity) {
        return entityManager.delete(entity);
    }

    @Override
    public <T> boolean delete(Class<T> entityClass, String id) {
        return entityManager.delete(entityClass, id);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return entityManager.findAll(entityClass);
    }

    @Override
    public <T> List<T> findByQuery(AliceQuery<T> query) {
        return entityManager.findByQuery(query);
    }

    @Override
    public <T> Collection<T> saveAll(Collection<T> entities) {
        return entityManager.saveAll(entities);
    }

    @Override
    public <T> Collection<T> updateAll(Collection<T> entities) {
        return entityManager.updateAll(entities);
    }

    @Override
    public <T> boolean deleteAll(Collection<T> entities) {
        return entityManager.deleteAll(entities);
    }

    @Override
    public <T> boolean deleteAll(Class<T> entityClass, Collection<String> ids) {
        return entityManager.deleteAll(entityClass, ids);
    }

    @Override
    public <T> AliceQueryBuilder<T> getQueryBuilder(Class<T> cls) {
        return entityManager.getQueryBuilder(cls);
    }

    @Override
    public <T> AliceEntityCursor<T> getEntityCursor(AliceQuery<T> query) {
        return entityManager.getEntityCursor(query);
    }

    @Override
    public <T> T getPlainEntity(Class<T> entityClass, String id) {
        return entityManager.getPlainEntity(entityClass, id);
    }

    @Override
    public <T> T initialize(T entity) {
        return entityManager.initialize(entity);
    }

    @Override
    public <T> T initialize(T entity, int level) {
        return entityManager.initialize(entity, level);
    }

    @Override
    public void cancelAll() {
        executorService.shutdownNow();
        executorService = initExecutorService();
    }

    @NonNull
    protected <R> Runnable createTask(final Callable<R> action, final ActionCallback<R> callback) {
        return new AsyncDatabaseAccessTask<R>(action, callback);
    }

    @Override
    public <T> boolean executeQuery(AliceQuery<T> query) {
        return entityManager.executeQuery(query);
    }

    @Override
    public <T> void executeQuery(final AliceQuery<T> query, final ActionCallback<Boolean> callback) {
        executorService.submit(createTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return entityManager.executeQuery(query);
            }
        }, callback));
    }

    private static class AsyncDatabaseAccessTask<R> implements Runnable {

        private Callable<R> action;
        private ActionCallback<R> callback;

        public AsyncDatabaseAccessTask(Callable<R> action, ActionCallback<R> callback) {
            this.action = action;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                R result = action.call();
                if (callback != null) {
                    callback.onFinishedSuccessfully(result);
                }
            } catch (Throwable e) {
                if (callback != null) {
                    callback.onErrorOccurred(e);
                }
            }
        }
    }
}
