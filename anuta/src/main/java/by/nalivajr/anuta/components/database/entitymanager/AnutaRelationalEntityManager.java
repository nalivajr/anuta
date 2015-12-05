package by.nalivajr.anuta.components.database.entitymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.FetchType;
import by.nalivajr.anuta.components.database.models.cache.EntityCache;
import by.nalivajr.anuta.components.database.models.descriptors.ColumnDescriptor;
import by.nalivajr.anuta.components.database.models.descriptors.EntityDescriptor;
import by.nalivajr.anuta.components.database.models.descriptors.RelationDescriptor;
import by.nalivajr.anuta.components.database.models.descriptors.RelationQueryDescriptor;
import by.nalivajr.anuta.components.database.models.enums.SqliteDataType;
import by.nalivajr.anuta.components.database.models.session.DatabaseAccessSession;
import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.components.database.stub.LazyInitializationCollection;
import by.nalivajr.anuta.components.database.stub.RelatedEntitiesProxyFactory;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaRelationalEntityManager extends AbstractEntityManager {

    public static final String TAG = AnutaRelationalEntityManager.class.getSimpleName();

    public AnutaRelationalEntityManager(Context context) {
        super(context);
    }

    @Override
    protected <T> String[] getProjection(Class<T> entityClass) {
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        Set<String> columns = descriptor.getFieldKeys();
        Set<RelationDescriptor> descriptors = descriptor.getRelationDescriptors();
        for (RelationDescriptor relationDescriptor : descriptors ) {
            columns.add(relationDescriptor.getRelationColumnName());
        }
        return columns.toArray(new String[columns.size()]);
    }

    @Override
    protected <T> ContentValues convertToContentValues(T entity) {
        Class<?> entityClass = entity.getClass();
        EntityDescriptor entityDescriptor = entityToDescriptor.get(entityClass);
        List<Field> fields = entityDescriptor.getFields();
        ContentValues contentValues = new ContentValues();
        for (Field field : fields) {
            String key = entityDescriptor.getFieldKey(field);
            if (key.equals(BaseColumns._ID)) {
                continue;
            }
            Column.DataType dataType = entityDescriptor.getFieldDescriptor(field).getColumnPersistingDataTypeStrategy();
            Object val = Anuta.databaseTools.getFieldValue(field, dataType, entity);
            Anuta.databaseTools.putValue(contentValues, key, val);
        }

        // covers related-entity and one-to-many during saving
        fields = entityDescriptor.getEntityRelatedFields();
        for (Field field : fields) {
            RelationDescriptor relationDescriptor = entityDescriptor.getRelationDescriptorForField(field);
            if (relationDescriptor.getRelationHoldingEntity() != entityClass) {
                continue;
            }
            Object parent = Anuta.reflectionTools.getValue(field, entity);
            Object foreignKey = null;
            if (parent != null) {
                String parentColumnName = relationDescriptor.getRelationReferencedColumnName();
                EntityDescriptor parentDescriptor = entityToDescriptor.get(parent.getClass());
                Field relationFieldInParent = parentDescriptor.getFieldForColumn(parentColumnName);
                foreignKey = Anuta.reflectionTools.getValue(relationFieldInParent, parent);
            }
            Anuta.databaseTools.putValue(contentValues, relationDescriptor.getJoinRelationColumnName(), foreignKey);
        }

        return contentValues;
    }

    @Override
    protected <T> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count, boolean closeAfter) {
        List<T> entities = count > 0 ? new ArrayList<T>(count) : new LinkedList<T>();
        Collection<ColumnDescriptor> columns = entityToDescriptor.get(entityClass).getFieldDescriptors();
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return entities;
            }
            boolean isCursorReady = true;
            int readLeft = count;
            while ((readLeft > 0 || count == -1) && isCursorReady) {
                T entity = cursorToEntity(entityClass, cursor, columns);
                entities.add(entity);
                isCursorReady = cursor.moveToNext();
                readLeft--;
            }
        } catch (Throwable e) {
            Log.e(TAG, String.format("Error during converting cursor to %s", entityClass.getName()), e);
            throw new RuntimeException(e);
        } finally {
            if (cursor != null && closeAfter) {
                cursor.close();
            }
        }
        return entities;
    }

    @Override
    protected <T> T cursorToEntity(Class<T> entityClass, Cursor cursor) {
        Collection<ColumnDescriptor> columns = entityToDescriptor.get(entityClass).getFieldDescriptors();
        try {
            return cursorToEntity(entityClass, cursor, columns);
        } catch (Throwable e) {
            Log.w(TAG, "Cold not convert cursor to entity", e);
        }
        return null;
    }

    private <T> T cursorToEntity(Class<T> entityClass, Cursor cursor, Collection<ColumnDescriptor> columns) {
        long rowId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

        boolean sessionCreator = openSession();
        DatabaseAccessSession accessSession = session.get();
        EntityCache cache = accessSession.getCache();
        T entityFromCache = cache.getByRowId(entityClass, rowId);
        if (entityFromCache != null) {
            closeSession(sessionCreator);
            return entityFromCache;
        }

        T entity = createEntity(entityClass);
        for (ColumnDescriptor column : columns) {
            SqliteDataType type = column.getSqlLiteDataType();
            Object converted = null;
            switch (type) {
                case BLOB:
                    converted = Anuta.databaseTools.convert(column, cursor.getBlob(cursor.getColumnIndex(column.getColumnName())));
                    break;
                case TEXT:
                    converted = Anuta.databaseTools.convert(column, cursor.getString(cursor.getColumnIndex(column.getColumnName())));
                    break;
                case INTEGER:
                    converted = Anuta.databaseTools.convert(column, cursor.getLong(cursor.getColumnIndex(column.getColumnName())));
                    break;
                case REAL:
                    converted = Anuta.databaseTools.convert(column, cursor.getDouble(cursor.getColumnIndex(column.getColumnName())));
                    break;
            }
            Anuta.reflectionTools.setValue(column.getField(), entity, converted);
        }
        setEntityRowId(entity, rowId);
        cache.put(entity, rowId);

        int level = accessSession.getLoadLevel();
        level = level > 0 ? level - 1 : level;
        accessSession.setLoadLevel(level);
        loadRelatedObjects(entityClass, cursor, entity, level);
        level = level >= 0 ? level + 1 : level;
        accessSession.setLoadLevel(level);

        closeSession(sessionCreator);
        return entity;
    }

    private <T> void loadRelatedObjects(Class<T> entityClass, Cursor cursor, T entity, int level) {
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        List<Field> fields = descriptor.getEntityRelatedFields();
        for (Field field : fields) {
            Collection related = getRelatedObjectCollection(cursor, descriptor, field, level);
            boolean lazy = related instanceof LazyInitializationCollection;
            if (related != null && !lazy && !related.isEmpty()) {
                Anuta.reflectionTools.setValue(field, entity, related.toArray()[0]);
            }
        }

        fields = descriptor.getOneToManyFields();
        loadAndBindCollection(cursor, entity, descriptor, fields, level);

        fields = descriptor.getManyToManyFields();
        loadAndBindCollection(cursor, entity, descriptor, fields, level);
    }

    private Collection getRelatedObjectCollection(Cursor cursor, EntityDescriptor descriptor, Field field, int level) {
        RelationQueryDescriptor queryDescriptor = descriptor.getRelationQueryDescriptorForField(field);
        AnutaQuery query = queryDescriptor.buildQuery(getContext().getContentResolver(), cursor);

        RelationDescriptor relationDescriptor = descriptor.getRelationDescriptorForField(field);
        FetchType fetchType = relationDescriptor.getFetchType();

        boolean lazyLoad =
                level == DatabaseAccessSession.LEVEL_ENTITY_ONLY ||
                (level == DatabaseAccessSession.LEVEL_ANNOTATION_BASED && fetchType == FetchType.LAZY);

        if (lazyLoad) {
            Class type = (Class) field.getType();
            return RelatedEntitiesProxyFactory.getNotInitializedCollection(type, query);
        }

        if (query == null) {
            return Collections.EMPTY_LIST;
        }

        return findByQuery(query);
    }

    private <T> void loadAndBindCollection(Cursor cursor, T entity, EntityDescriptor descriptor, List<Field> fields, int level) {
        for (Field field : fields) {
            Collection related = getRelatedObjectCollection(cursor, descriptor, field, level);
            Class type = (Class) field.getType();
            Object value = RelatedEntitiesProxyFactory.getCorrectTypeObject(type, related);
            Anuta.reflectionTools.setValue(field, entity, value);
        }
    }
}
