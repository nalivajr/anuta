package by.nalivajr.anuta.components.database.entitymanager;

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.components.database.cursor.AnutaBaseEntityCursor;
import by.nalivajr.anuta.components.database.cursor.AnutaEntityCursor;
import by.nalivajr.anuta.components.database.helpers.relations.EntityManagerRelationsHelper;
import by.nalivajr.anuta.components.database.models.Persistable;
import by.nalivajr.anuta.components.database.models.cache.EntityCache;
import by.nalivajr.anuta.components.database.models.descriptors.EntityDescriptor;
import by.nalivajr.anuta.components.database.models.session.DatabaseAccessSession;
import by.nalivajr.anuta.components.database.models.session.SimpleDatabaseAccessSession;
import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.components.database.query.AnutaQueryBuilder;
import by.nalivajr.anuta.components.database.query.BaseAnutaQueryBuilder;
import by.nalivajr.anuta.exceptions.DifferentEntityClassesException;
import by.nalivajr.anuta.exceptions.InvalidQueryTypeException;
import by.nalivajr.anuta.exceptions.NotRegisteredEntityClassUsedException;
import by.nalivajr.anuta.exceptions.OperationExecutionException;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractEntityManager implements AnutaEntityManager {

    public static final String TAG = AbstractEntityManager.class.getSimpleName();
    public static final String ROW_ID_SELECTION = BaseColumns._ID + " = ?";

    private Context context;
    private EntityManagerRelationsHelper relationsHelper;
    protected Map<Class<?>, EntityDescriptor> entityToDescriptor;
    protected ThreadLocal<DatabaseAccessSession> session;

    public AbstractEntityManager(Context context) {
        this.context = context;
        List<Class<?>> entityClasses = getEntityClasses();
        final List<EntityDescriptor> entityDescriptors = Anuta.databaseTools.generateDescriptorsFor(entityClasses);
        entityToDescriptor = new HashMap<Class<?>, EntityDescriptor>();
        for (EntityDescriptor descriptor : entityDescriptors) {
            entityToDescriptor.put(descriptor.getEntityClass(), descriptor);
        }
        this.relationsHelper = new EntityManagerRelationsHelper(entityToDescriptor);
        session = new ThreadLocal<DatabaseAccessSession>();
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
        if (!cursor.moveToFirst()) {
            return null;
        }
        T result = cursorToEntity(entityClass, cursor);
        cursor.close();
        return result;
    }

    @Override
    public <T> List<T> findByQuery(AnutaQuery<T> query) {
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

        Class<?> entityClass = entity.getClass();
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);

        return saveEntity(entity, descriptor);
    }

    @Override
    public <T> T update(T entity) {
        Class<?> entityClass = entity.getClass();
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);

        updateEntity(entity, descriptor);

        return entity;
    }

    @Override
    public <T> boolean delete(T entity) {
        if (entity == null) {
            Log.w(TAG, "Attempt to delete null entity");
            return false;
        }

        EntityDescriptor descriptor = entityToDescriptor.get(entity.getClass());
        ArrayList<ContentProviderOperation> operations = generateOperationsToDelete(entity, descriptor);
        return applyOperations(operations, descriptor.getAuthority()).length != 0;
    }

    @Override
    public <T> boolean delete(Class<T> entityClass, String id) {
        checkClassRegistered(entityClass);
        if (id == null) {
            Log.w(TAG, "Attempt to delete entity with null id");
            return false;
        }

        return delete(getPlainEntity(entityClass, id));
    }

    @Override
    public <T> Collection<T> saveAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to save. Collection is null or empty");
            return entities;
        }
        Class<?> entityClass = validateAllEntitiesSameClass(entities);
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        for (T entity: entities) {
            saveEntity(entity, descriptor);
        }
        return entities;
    }

    @Override
    public <T> Collection<T> updateAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to update. Collection is null or empty");
            return entities;
        }
        Class<?> entityClass = validateAllEntitiesSameClass(entities);
        checkClassRegistered(entityClass);
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);

        for (T entity : entities) {
            updateEntity(entity, descriptor);
        }

        return entities;
    }

    @Override
    public <T> boolean deleteAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Nothing to delete. Collection is null or empty");
            return false;
        }
        Class<?> entityClass = validateAllEntitiesSameClass(entities);

        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (T entity : entities) {
            operations.addAll(generateOperationsToDelete(entity, descriptor));
        }
        return applyOperations(operations, descriptor.getAuthority()).length != 0;
    }

    @Override
    public <T> boolean deleteAll(Class<T> entityClass, Collection<String> ids) {
        checkClassRegistered(entityClass);
        for (String id: ids) {
            delete(entityClass, id);
        }
        return true;
    }

    @Override
    public <T> T getPlainEntity(Class<T> entityClass, String id) {
        boolean sessionCreator = openSession();

        DatabaseAccessSession accessSession = session.get();
        accessSession.setLoadLevel(DatabaseAccessSession.LEVEL_ENTITY_ONLY);

        T entity = find(entityClass, id);

        closeSession(sessionCreator);
        return entity;
    }

    @Override
    public <T> T initialize(T entity) {
        return initialize(entity, DatabaseAccessSession.LEVEL_ALL);
    }

    @Override
    public <T> T initialize(T entity, int level) {
        if(entity == null) {
            return null;
        }
        boolean sessionCreator = openSession();
        DatabaseAccessSession accessSession = session.get();
        accessSession.setLoadLevel(level);

        String id = getEntityId(entity);
        entity = (T) find(entity.getClass(), id);

        closeSession(sessionCreator);
        return entity;
    }

    @Override
    public <T> Collection<T> initialize(Collection<T> entities) {
        return initialize(entities, DatabaseAccessSession.LEVEL_ALL);
    }

    @Override
    public <T> Collection<T> initialize(Collection<T> entities, int level) {
        if (entities == null || entities.isEmpty()) {
            return entities;
        }
        boolean sessionCreator = openSession();
        List<T> initializedEntities = new LinkedList<T>();
        for (T entity : entities) {
            initializedEntities.add(initialize(entity, level));
        }
        entities.clear();
        entities.addAll(initializedEntities);
        closeSession(sessionCreator);
        return entities;
    }

    @Override
    public <T> AnutaEntityCursor<T> getEntityCursor(final AnutaQuery<T> query) {
        Cursor cursor = getCursorByQuery(query);

        return new AnutaBaseEntityCursor<T>(cursor, getUri(query.getTargetClass())) {
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

    @Override
    public <T> boolean executeQuery(AnutaQuery<T> query) {
        checkClassRegistered(query.getTargetClass());
        Uri uri = getUri(query.getTargetClass());

        executeQuery(query, uri);
        return false;
    }

    @Override
    public <T> AnutaQueryBuilder<T> getQueryBuilder(Class<T> cls) {
        return new BaseAnutaQueryBuilder<T>(cls);
    }

    protected boolean openSession() {
        DatabaseAccessSession accessSession = session.get();
        if (accessSession == null) {
            accessSession = new SimpleDatabaseAccessSession();
            session.set(accessSession);
            return true;
        }
        return false;
    }

    protected void closeSession(boolean sessionCreator) {
        DatabaseAccessSession accessSession = session.get();
        if (sessionCreator && accessSession != null) {
            accessSession.getCache().clear();
            session.remove();
        }
    }

    /**
     * Saves entity and all related entities to database recursively.
     * @param entity the entity to be saved
     * @param descriptor the descriptor for the entity
     * @return saved entity instance
     */
    protected <T> T saveEntity(T entity, EntityDescriptor descriptor) {

        Long rowId = Anuta.databaseTools.getRowId(entity);
        if (rowId != null && rowId != 0) {
            updateEntity(entity, descriptor);
            return entity;
        }

        boolean sessionCreator = openSession();
        DatabaseAccessSession accessSession = session.get();
        EntityCache cache = accessSession.getCache();
        if (isInCache(entity, cache)) {
            closeSession(sessionCreator);
            return entity;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        Uri tableUri = descriptor.getTableUri();

        operations.addAll(generateOperationsToSave(tableUri, entity));      //right now there is one operation, but in future we may extend it

        String authority = descriptor.getAuthority();
        ContentProviderResult[] results;
        results = applyOperations(operations, authority);

        long id = ContentUris.parseId(results[0].uri);
        setEntityRowId(entity, id);
        cache.put(entity, id);

        mergeRelatedEntities(entity, descriptor);

        operations.clear();
        buildAddRelationsOperations(entity, operations);
        applyOperations(operations, authority);

        closeSession(sessionCreator);
        return entity;
    }

    /**
     * Updates (if entity exist in BD which means {@link BaseColumns#_ID} not null and not zero) or save related
     * entities for the given entity
     * @param entity source entity
     * @param descriptor the descriptor of the entity
     */
    private <T> void mergeRelatedEntities(T entity, EntityDescriptor descriptor) {
        List<Field> relationFields = descriptor.getEntityRelatedFields();
        for (Field field : relationFields) {
            Object related = Anuta.reflectionTools.getValue(field, entity);
            if (related == null) {
                continue;
            }
            update(related);
        }
        Collection<Field> relatedCollectionsFields = new ArrayList<Field>(descriptor.getOneToManyFields());
        relatedCollectionsFields.addAll(descriptor.getManyToManyFields());
        for (Field field : relatedCollectionsFields) {
            Collection relatedCollection = getRelatedEntitiesAsCollection(entity, field);
            if (relatedCollection == null) {
                continue;
            }
            for (Object relatedEntity : relatedCollection) {
                update(relatedEntity);
            }
        }
    }

    /**
     * Checks whether the given entity presents in the cache
     * @param entity the entity to be checked
     * @param cache the target cache to check
     * @return true if entity is found in cache and false otherwise
     */
    protected <T> boolean isInCache(T entity, EntityCache cache) {
        Long rowId = Anuta.databaseTools.getRowId(entity);
        return cache.containsEntity(entity.getClass(), rowId);
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

    private <T> void buildAddRelationsOperations(T entity, ArrayList<ContentProviderOperation> operations) {
        Class<?> entityClass = entity.getClass();
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);

        putAddRelatedEntityOperation(entity, operations, entityClass, descriptor);

        putAddOneToManyOperation(entity, operations, entityClass, descriptor);

        putAddManyToManyOperation(entity, operations, entityClass, descriptor);
    }

    /**
     * Updated entity in database and recursively updated related entities
     * @param entity the entity to be updated
     * @param descriptor the descriptor of the entity
     */
    protected <T> void updateEntity(T entity, EntityDescriptor descriptor) {
        Long rowId = Anuta.databaseTools.getRowId(entity);
        if (rowId == null || rowId == 0) {
            saveEntity(entity, descriptor);
            return;
        }

        boolean sessionCreator = openSession();
        DatabaseAccessSession accessSession = session.get();
        EntityCache cache = accessSession.getCache();
        if (isInCache(entity, cache)) {
            closeSession(sessionCreator);
            return;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        Uri tableUri = descriptor.getTableUri();

        operations.addAll(generateOperationsToUpdate(tableUri, entity));

        String authority = descriptor.getAuthority();
        applyOperations(operations, authority);
        cache.put(entity, rowId);

        mergeRelatedEntities(entity, descriptor);

        operations.clear();
        buildUpdateRelationsOperations(entity, operations);
        applyOperations(operations, authority);

        closeSession(sessionCreator);
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

        Long rowId = Anuta.databaseTools.getRowId(entity);
        if (rowId == null || rowId == 0) {
            operations.addAll(generateOperationsToSave(uri, entity));
            return operations;
        }
        ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newUpdate(uri);
        operationBuilder.withValues(convertToContentValues(entity));
        operationBuilder.withSelection(ROW_ID_SELECTION, new String[]{String.valueOf(rowId)});
        operations.add(operationBuilder.build());

        return operations;
    }

    private <T> void buildUpdateRelationsOperations(T entity, ArrayList<ContentProviderOperation> operations) {
        Class<?> entityClass = entity.getClass();
        EntityDescriptor entityDescriptor = entityToDescriptor.get(entityClass);

        List<Field> oneRelationFields = new LinkedList<Field>();

        oneRelationFields.addAll(entityDescriptor.getEntityRelatedFields());
        putDeleteRelatedEntityRelationOperations(entity, operations, entityClass, entityDescriptor);

        oneRelationFields.addAll(entityDescriptor.getOneToManyFields());

        putDeleteOneToManyRelationOperations(entity, operations, entityClass, entityDescriptor);
        putAddOneToManyOperation(entity, operations, entityClass, entityDescriptor);

        putDeleteManyToManyRelationOperation(entity, operations, entityClass, entityDescriptor);
        putAddManyToManyOperation(entity, operations, entityClass, entityDescriptor);
    }

    private <T> ArrayList<ContentProviderOperation> generateOperationsToDelete(T entity, EntityDescriptor descriptor) {

        Uri uri = descriptor.getTableUri();
        Long rowId = Anuta.databaseTools.getRowId(entity);
        if (rowId == null || rowId == 0) {
            // entity not saved. No need to delete
            return new ArrayList<ContentProviderOperation>();
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newDelete(uri);
        operationBuilder.withSelection(ROW_ID_SELECTION, new String[]{String.valueOf(rowId)});

        Class<?> entityClass = entity.getClass();

        putDeleteRelatedEntityRelationOperations(entity, operations, entityClass, descriptor);

        putDeleteOneToManyRelationOperations(entity, operations, entityClass, descriptor);

        putDeleteManyToManyRelationOperation(entity, operations, entityClass, descriptor);

        operations.add(operationBuilder.build());
        return operations;
    }

    protected ContentProviderResult[] applyOperations(ArrayList<ContentProviderOperation> operations, String authority) {
        ContentProviderResult[] result;
        try {
            result = context.getContentResolver().applyBatch(authority, operations);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not apply operations", e);
            throw new OperationExecutionException(e);
        } catch (OperationApplicationException e) {
            Log.w(TAG, "Could not apply operations", e);
            throw new OperationExecutionException(e);
        }
        return result;
    }

    protected <T> boolean executeQuery(AnutaQuery<T> query, Uri uri) {
        try {
            switch (query.getType()) {
                case SELECT:
                    getCursorByQuery(query);
                    break;
                case UPDATE:
                    getContext().getContentResolver().update(uri, query.getContentValues(), query.getSelection(), query.getSelectionArgs());
                    break;
                case INSERT:
                    getContext().getContentResolver().insert(uri, query.getContentValues());
                    break;
                case DELETE:
                    getContext().getContentResolver().delete(uri, query.getSelection(), query.getSelectionArgs());
                    break;
            }
            return true;
        } catch (Throwable e) {
            Log.w(TAG, "An error occurred during query execution", e);
            return false;
        }
    }

    protected <T> Cursor getCursorByQuery(AnutaQuery<T> query) {
        if (query.getType() != AnutaQuery.QueryType.SELECT) {
            throw new InvalidQueryTypeException(query.getType(), "Could not be used to get cursor. Please use AnutaEntityManager.executeQuery method");
        }
        final Class<T> entityClass = query.getTargetClass();
        checkClassRegistered(entityClass);

        Uri uri = getUri(entityClass);
        String selection = query.getSelection();
        String[] args = query.getSelectionArgs();

        String limit = query.getLimit() == null || query.getLimit().isEmpty() ? null : "1 " + query.getLimit();
        return getContext().getContentResolver().query(uri, getProjectionWithRowId(entityClass), selection, args, limit);
    }

    @Nullable
    private <T> String getEntityId(T entity) {
        Object id = null;
        Field idField = entityToDescriptor.get(entity.getClass()).getIdField();
        id = Anuta.reflectionTools.getValue(idField, entity);
        String strId = null;
        if (id != null) {
            strId = id.toString();
        }
        return strId;
    }

    protected void checkClassRegistered(Class<?> entityClass) {
        if (!entityToDescriptor.keySet().contains(entityClass)) {
            throw new NotRegisteredEntityClassUsedException(entityClass, this.getClass());
        }
    }

    /**
     * Checks whether the all entities in the given collection are the instances of the same class and return this class
     * @param entities source collection to be checked
     * @return class of entities.
     * @throws DifferentEntityClassesException if there are different type entities
     */
    protected <T> Class<?> validateAllEntitiesSameClass(Collection<T> entities) {
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

    protected <T> Uri getUri(Class<T> entityClass) {
        checkClassRegistered(entityClass);
        return entityToDescriptor.get(entityClass).getTableUri();
    }

    private <T> String getIdColumnName(Class<T> entityClass) {
        return entityToDescriptor.get(entityClass).getIdColumnName();
    }

    protected  <T> T createEntity(Class<T> entityClass) {
        return Anuta.reflectionTools.createEntity(entityClass);
    }

    protected Context getContext() {
        return context;
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

    /**
     * Sets rowId ({@link BaseColumns#_ID}) for the entity
     * @param entity target entity to set row id
     * @param rowId rowId ({@link BaseColumns#_ID})
     */
    protected <T> void setEntityRowId(T entity, long rowId) {
        if (entity instanceof Persistable) {
            ((Persistable) entity).setRowId(rowId);
            return;
        }
        Field rowIdField = entityToDescriptor.get(entity.getClass()).getRowIdField();
        if (rowIdField != null) {
            Anuta.reflectionTools.setValue(rowIdField, entity, rowId);
        }
    }

    @Nullable
    private <T> Collection getRelatedEntitiesAsCollection(T entity, Field field) {
        return relationsHelper.getRelatedEntitiesAsCollection(entity, field);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to create relation between given entity and related entities
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param descriptor the descriptor of entity
     */
    private <T> void putAddRelatedEntityOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                  Class<?> entityClass, EntityDescriptor descriptor) {
        relationsHelper.putAddRelatedEntityOperation(entity, operations, entityClass, descriptor);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to create relations between given entity and related collections
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param descriptor the descriptor of entity
     */
    private <T> void putAddOneToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                              Class<?> entityClass, EntityDescriptor descriptor) {
        relationsHelper.putAddOneToManyOperation(entity, operations, entityClass, descriptor);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relations between given entity and related
     * collection of many-to-many relationship type
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param descriptor the descriptor of entity
     */
    private <T> void putAddManyToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                               Class<?> entityClass, EntityDescriptor descriptor) {
        relationsHelper.putAddManyToManyOperation(entity, operations, entityClass, descriptor);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related entities
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     */
    protected <T> void putDeleteRelatedEntityRelationOperations(T entity, ArrayList<ContentProviderOperation> operations,
                                                          Class<?> entityClass, EntityDescriptor entityDescriptor) {
        relationsHelper.putDeleteRelatedEntityRelationOperations(entity, operations, entityClass, entityDescriptor);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relations between given entity and related collections
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     */
    protected <T> void putDeleteOneToManyRelationOperations(T entity, ArrayList<ContentProviderOperation> operations,
                                                          Class<?> entityClass, EntityDescriptor entityDescriptor) {
        relationsHelper.putDeleteOneToManyRelationOperations(entity, operations, entityClass, entityDescriptor);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related
     * collection of many-to-many relationship type
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     */
    protected <T> void putDeleteManyToManyRelationOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                          Class<?> entityClass, EntityDescriptor entityDescriptor) {
        relationsHelper.putDeleteManyToManyRelationOperation(entity, operations, entityClass, entityDescriptor);
    }
}
