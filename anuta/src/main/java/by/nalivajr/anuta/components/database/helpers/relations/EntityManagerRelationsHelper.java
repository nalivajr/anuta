package by.nalivajr.anuta.components.database.helpers.relations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import by.nalivajr.anuta.components.database.entitymanager.AbstractEntityManager;
import by.nalivajr.anuta.components.database.entitymanager.AnutaEntityManager;
import by.nalivajr.anuta.components.database.models.descriptors.EntityDescriptor;
import by.nalivajr.anuta.components.database.models.descriptors.RelationDescriptor;
import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.components.database.query.AnutaQueryBuilder;
import by.nalivajr.anuta.components.database.query.AnutaQueryWithUri;
import by.nalivajr.anuta.components.database.query.AnutaQueryWrapper;
import by.nalivajr.anuta.components.database.stub.LazyInitializationCollection;
import by.nalivajr.anuta.tools.Anuta;
import by.nalivajr.anuta.utils.CollectionsUtil;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class EntityManagerRelationsHelper {
    private final Map<Class<?>, EntityDescriptor> entityToDescriptor;
    private AnutaEntityManager entityManager;

    public EntityManagerRelationsHelper(AnutaEntityManager entityManager, Map<Class<?>, EntityDescriptor> entityToDescriptor) {
        this.entityToDescriptor = entityToDescriptor;
        this.entityManager = entityManager;
    }

    /**
     * Presents related entities, which are the value of field {@code field} as instance of collection
     * @param entity the entity to extract collection of related entities
     * @param field the field, containing collection
     * @return instance of {@link Collection} of related entities
     */
    @SuppressWarnings("unchecked")
    public <T> Collection getRelatedEntitiesAsCollection(T entity, Field field) {
        Object relatedEntities = Anuta.reflectionTools.getValue(field, entity);
        if (relatedEntities == null) {
            return Collections.EMPTY_LIST;
        }
        if (relatedEntities instanceof LazyInitializationCollection) {
            relatedEntities = entityManager.initialize((Collection) relatedEntities);
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

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related one
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     * @param field the field which contains related entity
     */
    public <T> void putDeleteRelatedEntityRelationOperations(T entity, ArrayList<ContentProviderOperation> operations,
                                                             Class<?> entityClass, EntityDescriptor entityDescriptor, Field field) {
        putDeleteOneToManyRelationOperations(entity, operations, entityClass, entityDescriptor, field);
    }


    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related one
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     */
    public <T> void putDeleteRelatedEntityRelationOperations(T entity, ArrayList<ContentProviderOperation> operations,
                                                             Class<?> entityClass, EntityDescriptor entityDescriptor) {
        List<Field> fields = entityDescriptor.getEntityRelatedFields();
        for (Field field : fields) {
            putDeleteRelatedEntityRelationOperations(entity, operations, entityClass, entityDescriptor, field);
        }
    }

    public <T> void putDeleteOneToManyRelationOperations(T entity, ArrayList<ContentProviderOperation> operations,
                                                         Class<?> entityClass, EntityDescriptor entityDescriptor) {
        List<Field> fields = entityDescriptor.getOneToManyFields();
        for (Field field : fields) {
            putDeleteOneToManyRelationOperations(entity, operations, entityClass, entityDescriptor, field);
        }
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related items
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     */
    public <T> ArrayList<ContentProviderOperation> putDeleteManyToManyRelationOperation(T entity) {
        Class<?> cls = entity.getClass();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        putDeleteManyToManyRelationOperation(entity, operations);
        return operations;
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related items
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     */
    public <T> void putDeleteManyToManyRelationOperation(T entity, ArrayList<ContentProviderOperation> operations) {
        Class<?> cls = entity.getClass();
        EntityDescriptor descriptor = entityToDescriptor.get(cls);
        putDeleteManyToManyRelationOperation(entity, operations, cls, descriptor);
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related items
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     */
    public <T> void putDeleteManyToManyRelationOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                         Class<?> entityClass, EntityDescriptor entityDescriptor) {
        List<Field> fields = entityDescriptor.getManyToManyFields();
        for (Field field : fields) {
            putDeleteManyToManyRelationOperation(entity, operations, entityClass, entityDescriptor, field);
        }
    }

    public <T> void putAddRelatedEntityOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                  Class<?> entityClass, EntityDescriptor descriptor) {
        List<Field> fields = descriptor.getEntityRelatedFields();
        for (Field field : fields) {
            Object relatedEntity = Anuta.reflectionTools.getValue(field, entity);
            if (relatedEntity == null) {
                continue;
            }
            Long relatedEntityRowId = Anuta.databaseTools.getRowId(relatedEntity);
            if (relatedEntityRowId == null || relatedEntityRowId == 0) {
                continue;
            }

            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            Class<?> relationHoldingEntityClass = relationDescriptor.getRelationHoldingEntity();
            EntityDescriptor relationEntityDescriptor = entityToDescriptor.get(relationHoldingEntityClass);
            Uri relationTableUri = relationEntityDescriptor.getTableUri();

            String column = relationDescriptor.getJoinReferencedRelationColumnName();       // parent_id column in child
            String relationColumnName = relationDescriptor.getRelationColumnName();         // column in this entity
            Field keyField = Anuta.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
            Object keySource = entity;

            if (entityClass == relationHoldingEntityClass) {
                column = relationDescriptor.getJoinRelationColumnName();
                relationColumnName = relationDescriptor.getRelationReferencedColumnName();
                keyField = Anuta.databaseTools.getFieldForColumnName(relationColumnName, relatedEntity.getClass());
                keySource = relatedEntity;
                relatedEntityRowId = Anuta.databaseTools.getRowId(entity);
            }

            Object value = Anuta.reflectionTools.getValue(keyField, keySource);
            ContentValues contentValues = new ContentValues();
            Anuta.databaseTools.putValue(contentValues, column, value);

            ContentProviderOperation.Builder relationOperationBuilder = ContentProviderOperation.newUpdate(relationTableUri);
            relationOperationBuilder.withValues(contentValues);
            relationOperationBuilder.withSelection(AbstractEntityManager.ROW_ID_SELECTION, new String[]{String.valueOf(relatedEntityRowId)});
            operations.add(relationOperationBuilder.build());
        }
    }

    public <T> void putAddOneToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
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

            Field keyField = Anuta.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
            Object value = Anuta.reflectionTools.getValue(keyField, entity);

            for (Object relatedEntity : relatedEntityCollection) {

                Long relatedEntityRowId = Anuta.databaseTools.getRowId(relatedEntity);
                if (relatedEntityRowId == null || relatedEntityRowId == 0) {
                    continue;
                }
                ContentValues contentValues = new ContentValues();
                Anuta.databaseTools.putValue(contentValues, column, value);

                ContentProviderOperation.Builder relationOperationBuilder = ContentProviderOperation.newUpdate(relationTableUri);
                relationOperationBuilder.withValues(contentValues);
                relationOperationBuilder.withSelection(AbstractEntityManager.ROW_ID_SELECTION, new String[]{String.valueOf(relatedEntityRowId)});
                operations.add(relationOperationBuilder.build());
            }
        }
    }

    public <T> void putAddManyToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                               Class<?> entityClass, EntityDescriptor descriptor) {
        List<Field> fields = descriptor.getManyToManyFields();
        for (Field field : fields) {
            Collection relatedCollection = getRelatedEntitiesAsCollection(entity, field);
            if (relatedCollection == null || relatedCollection.isEmpty()) {
                continue;
            }

            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            String relationTableName = relationDescriptor.getRelationTable();
            Uri relationTableUri = Anuta.databaseTools.buildUriForTableName(relationTableName, descriptor.getAuthority());

            String joinRelationColumnName = relationDescriptor.getJoinRelationColumnName();                     // parent_id column in relation table
            String joinReferencedRelationColumnName = relationDescriptor.getJoinReferencedRelationColumnName(); // child_id column in child

            String relationColumnName = relationDescriptor.getRelationColumnName();                         // column in this entity
            String relationReferencedColumnName = relationDescriptor.getRelationReferencedColumnName();     // column in child entity

            for (Object related : relatedCollection) {
                ContentValues contentValues = new ContentValues();
                Field keyField = Anuta.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
                Object entityKey = Anuta.reflectionTools.getValue(keyField, entity);
                if (entityKey == null) {
                    continue;
                }
                Anuta.databaseTools.putValue(contentValues, joinRelationColumnName, entityKey);

                Field keyRefField = Anuta.databaseTools.getFieldForColumnName(relationReferencedColumnName, related.getClass());
                Object entityRelKey = Anuta.reflectionTools.getValue(keyRefField, related);
                if (entityRelKey == null) {
                    continue;
                }
                Anuta.databaseTools.putValue(contentValues, joinReferencedRelationColumnName, entityRelKey);

                ContentProviderOperation.Builder relationOperationBuilder = ContentProviderOperation.newInsert(relationTableUri);
                relationOperationBuilder.withValues(contentValues);
                operations.add(relationOperationBuilder.build());
            }
        }
    }

    /**
     * Adds {@link ContentProviderOperation} operations, to remove relation between given entity and related one
     * @param entity the entity, which is going to be removed and relations with what should be destroyed
     * @param operations list of operations to add
     * @param entityClass the class of entity
     * @param entityDescriptor the descriptor of entity
     * @param field the field which contains related entity
     */
    public <T> void putDeleteOneToManyRelationOperations(T entity, ArrayList<ContentProviderOperation> operations,
                                                          Class<?> entityClass, EntityDescriptor entityDescriptor, Field field) {
        RelationDescriptor relationDescriptor = entityDescriptor.getRelationDescriptorForField(field);
        if (relationDescriptor.getRelationHoldingEntity() == entityClass) {
            return;
        }

        Uri relationTableUri = entityToDescriptor.get(relationDescriptor.getRelationHoldingEntity()).getTableUri();

        ContentProviderOperation.Builder updateRelationOperationBuilder = ContentProviderOperation.newUpdate(relationTableUri);
        String foreignKeyColumn = relationDescriptor.getRelationColumnName();
        Field foreignKeyField = Anuta.databaseTools.getFieldForColumnName(foreignKeyColumn, entityClass);
        Object val = Anuta.reflectionTools.getValue(foreignKeyField, entity);

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

    public <T> void putDeleteManyToManyRelationOperation(T entity, ArrayList<ContentProviderOperation> operations,
                                                          Class<?> entityClass, EntityDescriptor entityDescriptor, Field field) {
        RelationDescriptor relationDescriptor = entityDescriptor.getRelationDescriptorForField(field);
        String relationTableName = relationDescriptor.getRelationTable();
        Uri relationTableUri = Anuta.databaseTools.buildUriForTableName(relationTableName, entityDescriptor.getAuthority());

        String selection = relationDescriptor.getJoinRelationColumnName() + " = ?";
        Field relationField = Anuta.databaseTools.getFieldForColumnName(relationDescriptor.getRelationColumnName(), entityClass);
        Object val = Anuta.reflectionTools.getValue(relationField, entity);
        if (val != null) {
            String[] selectionArgs = new String[]{String.valueOf(val)};
            ContentProviderOperation.Builder deleteRelationBuilder = ContentProviderOperation.newDelete(relationTableUri);
            deleteRelationBuilder.withSelection(selection, selectionArgs);
            operations.add(deleteRelationBuilder.build());
        }
    }

    /**
     * Expands entity and extracts all entities which is going to be deleted
     * @param entity the root entity to deleted
     * @param entitiesMap the map, which will be used to store all entities which is going to be deleted
     * @return the map, representing set of ids of entities to be deleted
     */
    public <T> Map<Class<?>, Set<Long>> expandEntity(T entity, Map<Class<?>, Set> entitiesMap) {
        Map<Class<?>, Set<Long>> mappedIds = new HashMap<Class<?>, Set<Long>>();
        expandForDelete(mappedIds, entitiesMap, entity);
        return mappedIds;
    }

    private <T> void expandForDelete(Map<Class<?>, Set<Long>> map, Map<Class<?>, Set> entitiesMap, T entity) {
        if (entity == null) {
            return;
        }
        Long rowId = Anuta.databaseTools.getRowId(entity);
        if (rowId == null || rowId == 0) {
            return;
        }
        Set<Long> ids = map.get(entity.getClass());
        if (ids == null) {
            ids = new HashSet<Long>();
            map.put(entity.getClass(), ids);
        }
        if (ids.contains(rowId)) {
            return;
        }
        Set entities = entitiesMap.get(entity.getClass());
        if (entities == null) {
            entities = new HashSet();
            entitiesMap.put(entity.getClass(), entities);
        }
        ids.add(rowId);
        entities.add(entity);

        EntityDescriptor descriptor = entityToDescriptor.get(entity.getClass());
        expandRelatedEntityToDelete(map, entitiesMap, entity, descriptor);

        List<Field> fields = descriptor.getOneToManyFields();
        expandRelatedCollectionToDelete(map, entitiesMap, entity, descriptor, fields);

        fields = descriptor.getManyToManyFields();
        expandRelatedCollectionToDelete(map, entitiesMap, entity, descriptor, fields);
    }

    private <T> void expandRelatedEntityToDelete(Map<Class<?>, Set<Long>> map, Map<Class<?>, Set> entitiesMap,
                                                 T entity, EntityDescriptor descriptor) {
        List<Field> fields = descriptor.getEntityRelatedFields();
        for (Field field : fields) {
            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            if (!relationDescriptor.isCascadeDelete()) {
                continue;
            }
            Object related = Anuta.reflectionTools.getValue(field, entity);
            expandForDelete(map, entitiesMap, related);
        }
    }

    private <T> void expandRelatedCollectionToDelete(Map<Class<?>, Set<Long>> map, Map<Class<?>, Set> entitiesMap,
                                                     T entity, EntityDescriptor descriptor, List<Field> fields) {
        for (Field field : fields) {
            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            if (!relationDescriptor.isCascadeDelete()) {
                continue;
            }
            Collection items = getRelatedEntitiesAsCollection(entity, field);
            for (Object item : items) {
                expandForDelete(map, entitiesMap, item);
            }
        }
    }

    /**
     * Builds queries, which removes entity and all related entities (if cascade deletion) and removes all relation foreign keys
     * @param entity the entity to remove
     * @return list of queries to remove the entity
     */
    public  <T> List<AnutaQuery> buildDeletionQueries(T entity) {
        Map<Class<?>, Set> entitiesMap = new LinkedHashMap<Class<?>, Set>();
        Map<Class<?>, Set<Long>> idsMap = expandEntity(entity, entitiesMap);

        List<AnutaQuery> queries = buildDeletionQueries(idsMap, entitiesMap);

        return queries;
    }

    /**
     * Builds queries, which removes entity and all related entities (if cascade deletion) and removes all relation foreign keys
     * @param idsMap the map of ids of entities to remove
     * @param entitiesMap the map of entities to remove. Must contain entities for ids provided in first param
     * @return list of queries to remove the entity
     */
    public  <T> List<AnutaQuery> buildDeletionQueries(Map<Class<?>, Set<Long>> idsMap, Map<Class<?>, Set> entitiesMap) {

        Map<Uri, AnutaQueryBuilder> deleteQueryBuilderMap = new LinkedHashMap<Uri, AnutaQueryBuilder>();
        Map<Class<?>, AnutaQueryBuilder> updateQueryBuilderMap = new LinkedHashMap<Class<?>, AnutaQueryBuilder>();
        Map<AnutaQueryBuilder, ContentValues> updateRelationsValuesMap = new LinkedHashMap<AnutaQueryBuilder, ContentValues>();

        for (Class<?> cls : idsMap.keySet()) {
            AnutaQueryBuilder queryBuilder = entityManager.getQueryBuilder(cls);
            Set<Long> ids = idsMap.get(cls);
            String[] idsArgs = CollectionsUtil.toStringArray(ids);
            queryBuilder.or(queryBuilder.in(BaseColumns._ID, idsArgs));
            deleteQueryBuilderMap.put(entityToDescriptor.get(cls).getTableUri(), queryBuilder);
        }

        for (Class<?> cls : entitiesMap.keySet()) {
            Set entitiesToDelete = entitiesMap.get(cls);
            EntityDescriptor descriptor = entityToDescriptor.get(cls);

            for (Object entityToDelete : entitiesToDelete) {

                updateRemoveRelationQueries(updateQueryBuilderMap, updateRelationsValuesMap, cls, entityToDelete, descriptor);

                updateRemoveManyToManyRelationQuery(deleteQueryBuilderMap, cls, entityToDelete, descriptor);
            }
        }

        List<AnutaQuery> queries = new LinkedList<AnutaQuery>();

        for (final Map.Entry<Uri, AnutaQueryBuilder> entry : deleteQueryBuilderMap.entrySet()) {
            AnutaQuery query = entry.getValue().buildDelete();
            query = wrapQuery(query, entry.getKey());
            queries.add(query);
        }

        for (Map.Entry<Class<?>, AnutaQueryBuilder> entry : updateQueryBuilderMap.entrySet()) {
            AnutaQueryBuilder queryBuilder = entry.getValue();
            ContentValues values = updateRelationsValuesMap.get(queryBuilder);
            AnutaQuery query = queryBuilder.buildUpdate(values);
            queries.add(query);
        }
        return queries;
    }

    private void updateRemoveRelationQueries(Map<Class<?>, AnutaQueryBuilder> updateQueryBuilderMap,
                                             Map<AnutaQueryBuilder, ContentValues> updateRelationsValuesMap,
                                             Class<?> cls, Object entityToDelete, EntityDescriptor descriptor) {
        List<Field> simpleRelationFileds = new LinkedList<Field>(descriptor.getEntityRelatedFields());
        simpleRelationFileds.addAll(descriptor.getOneToManyFields());

        for (Field field : simpleRelationFileds) {
            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            Class<?> relationHoldingEntityClass = relationDescriptor.getRelationHoldingEntity();
            if (relationHoldingEntityClass == cls) {
                continue;
            }

            AnutaQueryBuilder queryBuilder = updateQueryBuilderMap.get(relationHoldingEntityClass);
            if (queryBuilder == null) {
                queryBuilder = entityManager.getQueryBuilder(relationHoldingEntityClass);
                updateQueryBuilderMap.put(relationHoldingEntityClass, queryBuilder);
            }

            String foreignKeyColumn = relationDescriptor.getRelationColumnName();
            Field foreignKeyField = Anuta.databaseTools.getFieldForColumnName(foreignKeyColumn, cls);
            Object val = Anuta.reflectionTools.getValue(foreignKeyField, entityToDelete);

            if (val == null) {
                continue;
            }

            String columnToUpdate = relationDescriptor.getJoinReferencedRelationColumnName();
            ContentValues contentValues = updateRelationsValuesMap.get(queryBuilder);
            if (contentValues == null) {
                contentValues = new ContentValues();
                updateRelationsValuesMap.put(queryBuilder, contentValues);
            }
            contentValues.putNull(columnToUpdate);
            queryBuilder.or(queryBuilder.equal(columnToUpdate, String.valueOf(val)));
        }
    }

    private void updateRemoveManyToManyRelationQuery(Map<Uri, AnutaQueryBuilder> deleteQueryBuilderMap, Class<?> cls,
                                                     Object entityToDelete, EntityDescriptor descriptor) {
        List<Field> manyToManyFields = descriptor.getManyToManyFields();
        for (Field field : manyToManyFields) {

            RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
            String relationTableName = relationDescriptor.getRelationTable();
            Uri relationTableUri = Anuta.databaseTools.buildUriForTableName(relationTableName, descriptor.getAuthority());

            Field relationField = Anuta.databaseTools.getFieldForColumnName(relationDescriptor.getRelationColumnName(), cls);
            Object val = Anuta.reflectionTools.getValue(relationField, entityToDelete);

            if (val != null) {
                AnutaQueryBuilder queryBuilder = deleteQueryBuilderMap.get(relationTableUri);
                if (queryBuilder == null) {
                    queryBuilder = entityManager.getQueryBuilder(cls);
                    deleteQueryBuilderMap.put(relationTableUri, queryBuilder);
                }
                queryBuilder.or(queryBuilder.equal(relationDescriptor.getJoinRelationColumnName(), String.valueOf(val)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private AnutaQueryWithUri wrapQuery(AnutaQuery query, final Uri uri) {
        return new AnutaQueryWrapper(query) {
            @Override
            public Uri getUri() {
                return uri;
            }
        };
    }
}
