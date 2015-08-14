package by.nalivajr.alice.components.database.entitymanager;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import by.nalivajr.alice.exceptions.OperationExecutionException;
import by.nalivajr.alice.tools.Alice;
import by.nalivajr.alice.components.database.cursor.AliceBaseEntityCursor;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.models.EntityDescriptor;
import by.nalivajr.alice.components.database.models.Persistable;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;
import by.nalivajr.alice.components.database.query.BaseAliceQueryBuilder;
import by.nalivajr.alice.exceptions.DifferentEntityClassesException;
import by.nalivajr.alice.exceptions.NotRegisteredEntityClassUsedException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractEntityManager implements AliceEntityManager {

    public static final String TAG = AbstractEntityManager.class.getSimpleName();

    private Context context;
    private HashSet<Class<?>> entitiesSet;
    protected Map<Class<?>, EntityDescriptor> entityToDescriptor;

    public AbstractEntityManager(Context context) {
        this.context = context;
        List<Class<?>> entityClasses = getEntityClasses();
        final List<EntityDescriptor> entityDescriptors = Alice.databaseTools.generateDescriptorsFor(entityClasses);
        entityToDescriptor = new HashMap<Class<?>, EntityDescriptor>();
        for (EntityDescriptor descriptor : entityDescriptors) {
            entityToDescriptor.put(descriptor.getEntityClass(), descriptor);
        }
        entitiesSet = new HashSet<Class<?>>(entityClasses);
    }

    /**
     * Converts cursor data to entities.
     * @param entityClass target entity class
     * @param cursor cursor with data
     * @param count amount of entities to read from cursor. -1 means all possible entities (all form cursor)
     * @param closeAfter true if cursor should be closed after conversion
     * @return list of converted entities. If no entities was read or any error occurred should return empty list
     */
    protected abstract <T> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count, boolean closeAfter);

    /**
     * Converts single entity from the current position of cursor
     * @param entityClass target entity class
     * @param cursor cursor with data
     * @return converted entity
     */
    protected abstract <T> T cursorToEntity(Class<T> entityClass, Cursor cursor);

    /**
     * Returns the projection for the given entity class
     * @param entityClass target entity class
     * @return an array of Strings with column names or null if all columns are requested
     */
    protected abstract <T> String[] getProjection(Class<T> entityClass);

    /**
     * Converts entity to content values object
     * @param entity source entity to be converted
     * @return {@link ContentValues} object, representing source entity
     */
    protected abstract <T> ContentValues convertToContentValues(T entity);

    /**
     * @return list of entity classes managed by this entity manager
     */
    protected abstract List<Class<?>> getEntityClasses();


    @Override
    public <T> T find(Class<T> entityClass, String id) {
        checkClassRegistered(entityClass);

        Uri uri = getUri(entityClass);

        String idColumn = getIdColumnName(entityClass);

        Cursor cursor = getContext().getContentResolver().query(uri, getProjectionWithRowId(entityClass), idColumn + "=?", new String[]{id}, null);
        T result = cursorToEntity(entityClass, cursor);
        cursor.close();
        return result;
    }

    @Override
    public <T> List<T> findByQuery(AliceQuery<T> query) {
        Class<T> entityClass = query.getTargetClass();

        Cursor cursor = getCursorByQuery(query);
        List<T> entities = convertCursorToEntities(entityClass, cursor, -1, true);
        if (entities == null) {
            return new ArrayList<T>();
        }
        return entities;
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return findByQuery(getQueryBuilder(entityClass).buildFindAllQuery());
    }

    @Override
    public <T> T save(T entity) {
        saveAll(Collections.singletonList(entity));
        return entity;
    }

    /**
     * Generates operations, which are required to save entity
     *
     * @param uri table uri
     * @param entity entity to be saved
     * @return {@link ArrayList} of operations
     */
    protected <T> ArrayList<ContentProviderOperation> generateOperationsToSave(Uri uri, T entity) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newInsert(uri);
        operationBuilder.withValues(convertToContentValues(entity));
        operations.add(operationBuilder.build());
        return operations;
    }

    @Override
    public <T> T update(T entity) {
        updateAll(Collections.singletonList(entity));
        return entity;
    }

    /**
     * Generates operations, which are required to update entity
     *
     * @param uri table uri
     * @param entity entity to be saved
     * @return {@link ArrayList} of operations
     */
    protected <T> ArrayList<ContentProviderOperation> generateOperationsToUpdate(Uri uri, T entity) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newUpdate(uri);
        operationBuilder.withValues(convertToContentValues(entity));
        String selection = getIdColumnName(entity.getClass()) + "=?";
        String[] args = new String[]{getEntityId(entity)};
        operationBuilder.withSelection(selection, args);
        operations.add(operationBuilder.build());
        return operations;
    }

    @Override
    public <T> boolean delete(T entity) {
        if (entity == null) {
            Log.w(TAG, "Attempt to delete null entity");
            return false;
        }
        String strId = getEntityId(entity);
        return delete(entity.getClass(), strId);
    }

    @Override
    public <T> boolean delete(Class<T> entityClass, String id) {
        checkClassRegistered(entityClass);
        if (id == null) {
            Log.w(TAG, "Attempt to delete entity with null id");
            return false;
        }

        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        ArrayList<ContentProviderOperation> operations = generateOperationsToDelete(descriptor.getTableUri(), descriptor.getIdColumnName(), id);
        return applyOperations(operations, descriptor.getAuthority()).length != 0;
    }

    /**
     * Generates operations, which are required to delete entity
     *
     * @param uri table uri
     * @param idColumnName the name of entity's id column
     *@param id entity's id  @return {@link ArrayList} of operations
     */
    protected ArrayList<ContentProviderOperation> generateOperationsToDelete(Uri uri, String idColumnName, String id) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newDelete(uri);
        String selection = idColumnName + "=?";
        String[] args = new String[]{id};
        operationBuilder.withSelection(selection, args);
        operations.add(operationBuilder.build());
        return operations;
    }

    @Override
    public <T> Collection<T> saveAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to save. Collection is null or empty");
            return entities;
        }
        Class<?> entityClass = checkAllEntitiesSameClass(entities);
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(entities.size());
        Uri tableUri = descriptor.getTableUri();

        Object[] entitiesArray = entities.toArray();
        for (Object entity : entitiesArray) {
            operations.addAll(generateOperationsToSave(tableUri, entity));
        }

        String authority = descriptor.getAuthority();
        ContentProviderResult[] results;
        results = applyOperations(operations, authority);

        for (int i = 0; i < results.length; i++) {
            T entity = ((T) entitiesArray[i]);
            ContentProviderResult result = results[i];
            long id = ContentUris.parseId(result.uri);
            setEntityRowId(entity, id);
        }
        return entities;
    }

    protected ContentProviderResult[] applyOperations(ArrayList<ContentProviderOperation> operations, String authority) {
        ContentProviderResult[] result;
        try {
            result = context.getContentResolver().applyBatch(authority, operations);
        } catch (RemoteException e) {
            throw new OperationExecutionException(e);
        } catch (OperationApplicationException e) {
            throw new OperationExecutionException(e);
        }
        return result;
    }

    @Override
    public <T> Collection<T> updateAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to update. Collection is null or empty");
            return entities;
        }
        Class<?> entityClass = checkAllEntitiesSameClass(entities);
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(entities.size());
        Uri tableUri = descriptor.getTableUri();

        Object[] entitiesArray = entities.toArray();
        for (Object entity : entitiesArray) {
            operations.addAll(generateOperationsToUpdate(tableUri, entity));
        }

        String authority = descriptor.getAuthority();
        applyOperations(operations, authority);
        return entities;
    }

    @Override
    public <T> boolean deleteAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to delete. Collection is null or empty");
            return false;
        }
        Class<?> entityClass = checkAllEntitiesSameClass(entities);
        List<String> ids = new ArrayList<String>(entities.size());
        for (T entity : entities) {
            ids.add(getEntityId(entity));
        }
        return deleteAll(entityClass, ids);
    }

    @Override
    public <T> boolean deleteAll(Class<T> entityClass, Collection<String> ids) {
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(ids.size());
        Uri tableUri = descriptor.getTableUri();

        //TODO: possibly correct variant of selection like 'WHERE id IN (?,?...?)' could be used, but leave this way as in future cascade deletion will be integrated
        for (String id : ids) {
            operations.addAll(generateOperationsToDelete(tableUri, descriptor.getIdColumnName(), id));
        }

        String authority = descriptor.getAuthority();
        applyOperations(operations, authority);
        return true;
    }

    @Override
    public <T> AliceEntityCursor<T> getEntityCursor(final AliceQuery<T> query) {
        Cursor cursor = getCursorByQuery(query);

        return new AliceBaseEntityCursor<T>(cursor, getUri(query.getTargetClass())) {
            @Override
            protected T convert(Cursor cursor) {
                return cursorToEntity(query.getTargetClass(), cursor);
            }

            @Override
            protected Cursor getActualCursor() {
                return getCursorByQuery(query);
            }

            @Override
            public ContentResolver getContentResolver() {
                return context.getContentResolver();
            }
        };
    }

    protected <T> Cursor getCursorByQuery(AliceQuery<T> query) {
        final Class<T> entityClass = query.getTargetClass();
        checkClassRegistered(entityClass);

        Uri uri = getUri(entityClass);
        String selection = query.getSelection();
        String[] args = query.getSelectionArgs();

        return getContext().getContentResolver().query(uri, getProjectionWithRowId(entityClass), selection, args, null);
    }

    @Nullable
    private <T> String getEntityId(T entity) {
        Object id = null;
        Field idField = entityToDescriptor.get(entity.getClass()).getIdField();
        id = Alice.reflectionTools.getValue(idField, entity);
        String strId = null;
        if (id != null) {
            strId = id.toString();
        }
        return strId;
    }

    protected void checkClassRegistered(Class<?> entityClass) {
        if (!entitiesSet.contains(entityClass)) {
            throw new NotRegisteredEntityClassUsedException(entityClass, this.getClass());
        }
    }

    protected <T> Class<?> checkAllEntitiesSameClass(Collection<T> entities) {
        Class<?> cls = null;
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to check. Collection is null or empty");
            return null;
        }
        Iterator<T> iterator = entities.iterator();
        cls = iterator.next().getClass();
        while (iterator.hasNext()) {
            if (iterator.next().getClass() != cls) {
                throw new DifferentEntityClassesException();
            }
        }
        return cls;
    }

    protected <T> void setEntityRowId(T entity, long rowId) {
        if (entity instanceof Persistable) {
            ((Persistable) entity).setRowId(rowId);
            return;
        }
        Field rowIdField = entityToDescriptor.get(entity.getClass()).getRowIdField();
        if (rowIdField != null) {
            Alice.reflectionTools.setValue(rowIdField, entity, rowId);
        }
    }

    protected <T> Uri getUri(Class<T> entityClass) {
        checkClassRegistered(entityClass);
        return entityToDescriptor.get(entityClass).getTableUri();
    }

    private <T> String getIdColumnName(Class<T> entityClass) {
        return entityToDescriptor.get(entityClass).getIdColumnName();
    }

    private <T> String[] getProjectionWithRowId(Class<T> entityClass) {
        String[] projection = getProjection(entityClass);
        for (String column : projection) {
            if (column.equals(BaseColumns._ID)) {
                return projection;
            }
        }
        String[] projectionExt = new String[projection.length + 1];
        projectionExt[0] = BaseColumns._ID;
        System.arraycopy(projection, 0, projectionExt, 1, projection.length);
        return projectionExt;
    }

    protected  <T> T createEntity(Class<T> entityClass) {
        return Alice.reflectionTools.createEntity(entityClass);
    }

    protected Context getContext() {
        return context;
    }

    public <T> AliceQueryBuilder<T> getQueryBuilder(Class<T> cls) {
        return new BaseAliceQueryBuilder<T>(cls);
    }
}