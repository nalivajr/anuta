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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import by.nalivajr.alice.components.database.cursor.AliceBaseEntityCursor;
import by.nalivajr.alice.components.database.cursor.AliceEntityCursor;
import by.nalivajr.alice.components.database.models.DatabaseAccessSession;
import by.nalivajr.alice.components.database.models.EntityCache;
import by.nalivajr.alice.components.database.models.EntityDescriptor;
import by.nalivajr.alice.components.database.models.Persistable;
import by.nalivajr.alice.components.database.models.RelationDescriptor;
import by.nalivajr.alice.components.database.models.SimpleDatabaseAccessSession;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;
import by.nalivajr.alice.components.database.query.BaseAliceQueryBuilder;
import by.nalivajr.alice.exceptions.DifferentEntityClassesException;
import by.nalivajr.alice.exceptions.InvalidQueryTypeException;
import by.nalivajr.alice.exceptions.NotRegisteredEntityClassUsedException;
import by.nalivajr.alice.exceptions.OperationExecutionException;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractEntityManager implements AliceEntityManager {

    public static final String TAG = AbstractEntityManager.class.getSimpleName();
    public static final String ROW_ID_SELECTION = BaseColumns._ID + " = ?";

    private Context context;
    private HashSet<Class<?>> entitiesSet;
    protected Map<Class<?>, EntityDescriptor> entityToDescriptor;
    protected ThreadLocal<DatabaseAccessSession> session;

    public AbstractEntityManager(Context context) {
        this.context = context;
        List<Class<?>> entityClasses = getEntityClasses();
        final List<EntityDescriptor> entityDescriptors = Alice.databaseTools.generateDescriptorsFor(entityClasses);
        entityToDescriptor = new HashMap<Class<?>, EntityDescriptor>();
        for (EntityDescriptor descriptor : entityDescriptors) {
            entityToDescriptor.put(descriptor.getEntityClass(), descriptor);
        }
        entitiesSet = new HashSet<Class<?>>(entityClasses);
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
        DatabaseAccessSession accessSession = new SimpleDatabaseAccessSession();
        accessSession.setLoadLevel(DatabaseAccessSession.LEVEL_ENTITY_ONLY);
        session.set(accessSession);

        T entity = find(entityClass, id);

        accessSession.getCache().clear();
        session.remove();
        return entity;
    }

    @Override
    public <T> T initialize(T entity) {
        return initialize(entity, DatabaseAccessSession.LEVEL_ALL);
    }

    @Override
    public <T> T initialize(T entity, int level) {
        if(entity == null) {
            return entity;
        }
        DatabaseAccessSession accessSession = new SimpleDatabaseAccessSession();
        accessSession.setLoadLevel(level);
        session.set(accessSession);

        String id = getEntityId(entity);
        entity = (T) find(entity.getClass(), id);

        accessSession.getCache().clear();
        session.remove();
        return entity;
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

    @Override
    public <T> boolean executeQuery(AliceQuery<T> query) {
        checkClassRegistered(query.getTargetClass());
        Uri uri = getUri(query.getTargetClass());

        executeQuery(query, uri);
        return false;
    }

    @Override
    public <T> AliceQueryBuilder<T> getQueryBuilder(Class<T> cls) {
        return new BaseAliceQueryBuilder<T>(cls);
    }

    /**
     * Saves entity and all related entities to database recursively.
     * @param entity the entity to be saved
     * @param descriptor the descriptor for the entity
     * @return saved entity instance
     */
    protected <T> T saveEntity(T entity, EntityDescriptor descriptor) {

        Long rowId = Alice.databaseTools.getRowId(entity);
        if (rowId != null && rowId != 0) {
            updateEntity(entity, descriptor);
            return entity;
        }

        boolean cacheCreator = false;
        DatabaseAccessSession accessSession = session.get();
        if (accessSession == null) {
            accessSession = new SimpleDatabaseAccessSession();
            session.set(accessSession);
            cacheCreator = true;
        }
        EntityCache cache = accessSession.getCache();
        if (isInCache(entity, cache)) {
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

        if (cacheCreator) {
            cache.clear();
            session.remove();
        }
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
            Object related = Alice.reflectionTools.getValue(field, entity);
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
        Long rowId = Alice.databaseTools.getRowId(entity);
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

//        buildAddRelationsOperations(entity, operations);
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
        Long rowId = Alice.databaseTools.getRowId(entity);
        if (rowId == null || rowId == 0) {
            saveEntity(entity, descriptor);
            return;
        }

        boolean cacheCreator = false;
        DatabaseAccessSession accessSession = session.get();
        if (accessSession == null) {
            accessSession = new SimpleDatabaseAccessSession();
            session.set(accessSession);
            cacheCreator = true;
        }
        EntityCache cache = accessSession.getCache();
        if (isInCache(entity, cache)) {
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

        if (cacheCreator) {
            cache.clear();
            session.remove();
        }
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

        Long rowId = Alice.databaseTools.getRowId(entity);
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
        oneRelationFields.addAll(entityDescriptor.getOneToManyFields());

        for (Field field : oneRelationFields) {
            putRemoveOneToManyRelations(entity, operations, entityClass, entityDescriptor, field);
        }
        putAddOneToManyOperation(entity, operations, entityClass, entityDescriptor);

        List<Field> manyToManyFields = entityDescriptor.getManyToManyFields();
        for (Field field : manyToManyFields) {
            //first need to delete old relations
            putDeleteManyToManyRelationOperation(entity, operations, entityClass, entityDescriptor, field);
        }
        putAddManyToManyOperation(entity, operations, entityClass, entityDescriptor);
    }

    private <T> ArrayList<ContentProviderOperation> generateOperationsToDelete(T entity, EntityDescriptor descriptor) {

        Uri uri = descriptor.getTableUri();
        Long rowId = Alice.databaseTools.getRowId(entity);
        if (rowId == null || rowId == 0) {
            // entity not saved. No need to delete
            return new ArrayList<ContentProviderOperation>();
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newDelete(uri);
        operationBuilder.withSelection(ROW_ID_SELECTION, new String[]{String.valueOf(rowId)});

        List<Field> oneRelationFields = new LinkedList<Field>();

        oneRelationFields.addAll(descriptor.getEntityRelatedFields());
        oneRelationFields.addAll(descriptor.getOneToManyFields());

        Class<?> entityClass = entity.getClass();
        for (Field field : oneRelationFields) {
            putRemoveOneToManyRelations(entity, operations, entityClass, descriptor, field);
        }

        List<Field> manyToManyFields = descriptor.getManyToManyFields();
        for (Field field : manyToManyFields) {
            //first need to delete old relations
            putDeleteManyToManyRelationOperation(entity, operations, entityClass, descriptor, field);
        }

        operations.add(operationBuilder.build());
        return operations;
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

    protected <T> boolean executeQuery(AliceQuery<T> query, Uri uri) {
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

    protected <T> Cursor getCursorByQuery(AliceQuery<T> query) {
        if (query.getType() != AliceQuery.QueryType.SELECT) {
            throw new InvalidQueryTypeException(query.getType(), "Could not be used to get cursor. Please use AliceEntityManager.executeQuery method");
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
        return Alice.reflectionTools.createEntity(entityClass);
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
            Alice.reflectionTools.setValue(rowIdField, entity, rowId);
        }
    }

    @Nullable
    private <T> Collection getRelatedEntitiesAsCollection(T entity, Field field) {
        Object relatedEntities = Alice.reflectionTools.getValue(field, entity);
        if (relatedEntities == null) {
            return Collections.EMPTY_LIST;
        }
        Collection relatedCollection = null;
        if (relatedEntities.getClass().isArray()) {
            Object[] entityArray = (Object[]) relatedEntities;
            ArrayList entities = new ArrayList(entityArray.length);
            for (Object relatedEntity : entityArray) {
                if (relatedEntity != null) {
                    entities.add(relatedEntity);
                }
            }
            relatedCollection = entities;
        } else {
            relatedCollection = (Collection) relatedEntities;
        }
        List relatedList = new ArrayList(relatedCollection);
        while (relatedCollection.contains(null)) {
            relatedCollection.remove(null);
        }
        return relatedList;
    }

    private <T> void putAddRelatedEntityOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                  Class<?> entityClass, EntityDescriptor descriptor) {
        List<Field> fields = descriptor.getEntityRelatedFields();
        for (Field field : fields) {
            Object relatedEntity = Alice.reflectionTools.getValue(field, entity);
            if (relatedEntity == null) {
                continue;
            }
            Long relatedEntityRowId = Alice.databaseTools.getRowId(relatedEntity);
            if (relatedEntityRowId == null || relatedEntityRowId == 0) {
                continue;
            }

            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            Class<?> relationHoldingEntityClass = relationDescriptor.getRelationHoldingEntity();
            EntityDescriptor relationEntityDescriptor = entityToDescriptor.get(relationHoldingEntityClass);
            Uri relationTableUri = relationEntityDescriptor.getTableUri();

            String column = relationDescriptor.getJoinReferencedRelationColumnName();       // parent_id column in child
            String relationColumnName = relationDescriptor.getRelationColumnName();         // column in this entity
            Field keyField = Alice.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
            Object keySource = entity;

            if (entityClass == relationHoldingEntityClass) {
                column = relationDescriptor.getJoinRelationColumnName();
                relationColumnName = relationDescriptor.getRelationReferencedColumnName();
                keyField = Alice.databaseTools.getFieldForColumnName(relationColumnName, relatedEntity.getClass());
                keySource = relatedEntity;
                relatedEntityRowId = Alice.databaseTools.getRowId(entity);
            }

            Object value = Alice.reflectionTools.getValue(keyField, keySource);
            ContentValues contentValues = new ContentValues();
            Alice.databaseTools.putValue(contentValues, column, value);

            ContentProviderOperation.Builder relationOperationBuilder = ContentProviderOperation.newUpdate(relationTableUri);
            relationOperationBuilder.withValues(contentValues);
            relationOperationBuilder.withSelection(ROW_ID_SELECTION, new String[]{String.valueOf(relatedEntityRowId)});
            operations.add(relationOperationBuilder.build());
        }
    }

    private <T> void putAddOneToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                              Class<?> entityClass, EntityDescriptor descriptor) {
        List<Field> fields = descriptor.getOneToManyFields();
        for (Field field : fields) {
            Collection relatedEntityCollection = getRelatedEntitiesAsCollection(entity, field);
            if (relatedEntityCollection == null || relatedEntityCollection.isEmpty()) {
                continue;
            }

            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            Class<?> relationHoldingEntityClass = relationDescriptor.getRelationHoldingEntity();

            EntityDescriptor relationEntityDescriptor = entityToDescriptor.get(relationHoldingEntityClass);
            Uri relationTableUri = relationEntityDescriptor.getTableUri();

            String column = relationDescriptor.getJoinReferencedRelationColumnName();       // parent_id column in child
            String relationColumnName = relationDescriptor.getRelationColumnName();         // column in this entity

            Field keyField = Alice.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
            Object value = Alice.reflectionTools.getValue(keyField, entity);

            for (Object relatedEntity : relatedEntityCollection) {

                Long relatedEntityRowId = Alice.databaseTools.getRowId(relatedEntity);
                if (relatedEntityRowId == null || relatedEntityRowId == 0) {
                    continue;
                }
                ContentValues contentValues = new ContentValues();
                Alice.databaseTools.putValue(contentValues, column, value);

                ContentProviderOperation.Builder relationOperationBuilder = ContentProviderOperation.newUpdate(relationTableUri);
                relationOperationBuilder.withValues(contentValues);
                relationOperationBuilder.withSelection(ROW_ID_SELECTION, new String[]{String.valueOf(relatedEntityRowId)});
                operations.add(relationOperationBuilder.build());
            }
        }
    }

    private <T> void putAddManyToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                               Class<?> entityClass, EntityDescriptor descriptor) {
        List<Field> fields = descriptor.getManyToManyFields();
        for (Field field : fields) {
            Collection relatedCollection = getRelatedEntitiesAsCollection(entity, field);
            if (relatedCollection == null || relatedCollection.isEmpty()) {
                continue;
            }

            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            String relationTableName = relationDescriptor.getRelationTable();
            Uri relationTableUri = Alice.databaseTools.buildUriForTableName(relationTableName, descriptor.getAuthority());

            String joinRelationColumnName = relationDescriptor.getJoinRelationColumnName();                     // parent_id column in relation table
            String joinReferencedRelationColumnName = relationDescriptor.getJoinReferencedRelationColumnName(); // child_id column in child

            String relationColumnName = relationDescriptor.getRelationColumnName();                         // column in this entity
            String relationReferencedColumnName = relationDescriptor.getRelationReferencedColumnName();     // column in child entity

            for (Object related : relatedCollection) {
                ContentValues contentValues = new ContentValues();
                Field keyField = Alice.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
                Object entityKey = Alice.reflectionTools.getValue(keyField, entity);
                if (entityKey == null) {
                    continue;
                }
                Alice.databaseTools.putValue(contentValues, joinRelationColumnName, entityKey);

                Field keyRefField = Alice.databaseTools.getFieldForColumnName(relationReferencedColumnName, related.getClass());
                Object entityRelKey = Alice.reflectionTools.getValue(keyRefField, related);
                if (entityRelKey == null) {
                    continue;
                }
                Alice.databaseTools.putValue(contentValues, joinReferencedRelationColumnName, entityRelKey);

                ContentProviderOperation.Builder relationOperationBuilder = ContentProviderOperation.newInsert(relationTableUri);
                relationOperationBuilder.withValues(contentValues);
                operations.add(relationOperationBuilder.build());
            }
        }
    }

    private <T> void putRemoveOneToManyRelations(T entity, ArrayList<ContentProviderOperation> operations,
                                                 Class<?> entityClass, EntityDescriptor entityDescriptor, Field field) {
        RelationDescriptor relationDescriptor = entityDescriptor.getRelationDescriptorForField(field);
        if (relationDescriptor.getRelationHoldingEntity() == entityClass) {
            return;
        }

        Uri relationTableUri = entityToDescriptor.get(relationDescriptor.getRelationHoldingEntity()).getTableUri();

        ContentProviderOperation.Builder updateRelationOperationBuilder = ContentProviderOperation.newUpdate(relationTableUri);
        String foreignKeyColumn = relationDescriptor.getRelationColumnName();
        Field foreignKeyField = Alice.databaseTools.getFieldForColumnName(foreignKeyColumn, entityClass);
        Object val = Alice.reflectionTools.getValue(foreignKeyField, entity);

        if (val == null) {
            return;
        }

        String columnToUpdate = relationDescriptor.getJoinReferencedRelationColumnName();
        ContentValues contentValues = new ContentValues();
        contentValues.putNull(columnToUpdate);
        updateRelationOperationBuilder.withSelection(columnToUpdate + "= ?", new String[]{String.valueOf(val)});
        updateRelationOperationBuilder.withValues(contentValues);
        operations.add(updateRelationOperationBuilder.build());
    }

    private <T> void putDeleteManyToManyRelationOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                          Class<?> entityClass, EntityDescriptor entityDescriptor, Field field) {
        RelationDescriptor relationDescriptor = entityDescriptor.getRelationDescriptorForField(field);
        String relationTableName = relationDescriptor.getRelationTable();
        Uri relationTableUri = Alice.databaseTools.buildUriForTableName(relationTableName, entityDescriptor.getAuthority());

        String selection = relationDescriptor.getJoinRelationColumnName() + " = ?";
        Field relationField = Alice.databaseTools.getFieldForColumnName(relationDescriptor.getRelationColumnName(), entityClass);
        Object val = Alice.reflectionTools.getValue(relationField, entity);
        if (val != null) {
            String[] selectionArgs = new String[]{String.valueOf(val)};
            ContentProviderOperation.Builder deleteRelationBuilder = ContentProviderOperation.newDelete(relationTableUri);
            deleteRelationBuilder.withSelection(selection, selectionArgs);
            operations.add(deleteRelationBuilder.build());
        }
    }
}
