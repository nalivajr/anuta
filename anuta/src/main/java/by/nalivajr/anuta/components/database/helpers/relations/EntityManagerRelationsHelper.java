package by.nalivajr.anuta.components.database.helpers.relations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.components.database.entitymanager.AbstractEntityManager;
import by.nalivajr.anuta.components.database.models.descriptors.EntityDescriptor;
import by.nalivajr.anuta.components.database.models.descriptors.RelationDescriptor;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class EntityManagerRelationsHelper {
    private final Map<Class<?>, EntityDescriptor> entityToDescriptor;

    public EntityManagerRelationsHelper(Map<Class<?>, EntityDescriptor> entityToDescriptor) {
        this.entityToDescriptor = entityToDescriptor;
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

    public  <T> void putAddManyToManyOperation(T entity, ArrayList<ContentProviderOperation> operations,
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
}
