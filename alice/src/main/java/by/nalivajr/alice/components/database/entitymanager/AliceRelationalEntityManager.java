package by.nalivajr.alice.components.database.entitymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import by.nalivajr.alice.annonatations.database.Column;
import by.nalivajr.alice.components.database.models.ColumnDescriptor;
import by.nalivajr.alice.components.database.models.EntityCache;
import by.nalivajr.alice.components.database.models.EntityDescriptor;
import by.nalivajr.alice.components.database.models.RelationQueryDescriptor;
import by.nalivajr.alice.components.database.models.SimpleEntityCache;
import by.nalivajr.alice.components.database.models.SqliteDataType;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceRelationalEntityManager extends AbstractEntityManager {

    public static final String TAG = AliceRelationalEntityManager.class.getSimpleName();

    public AliceRelationalEntityManager(Context context) {
        super(context);
    }

    @Override
    protected <T> String[] getProjection(Class<T> entityClass) {
        Set<String> columns = entityToDescriptor.get(entityClass).getFieldKeys();
        return columns.toArray(new String[columns.size()]);
    }

    @Override
    protected <T> ContentValues convertToContentValues(T entity) {
        EntityDescriptor entityDescriptor = entityToDescriptor.get(entity.getClass());
        List<Field> fields = entityDescriptor.getFields();
        ContentValues contentValues = new ContentValues();
        for (Field field : fields) {
            String key = entityDescriptor.getFieldKey(field);
            if (key.equals(BaseColumns._ID)) {
                continue;
            }
            Column.DataType dataType = entityDescriptor.getFieldDescriptor(field).getColumnPersistingDataTypeStrategy();
            Object val = Alice.databaseTools.getFieldValue(field, dataType, entity);
            Alice.databaseTools.putValue(contentValues, key, val);
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

        boolean cacheCreator = false;
        EntityCache cache = entityCache.get();
        if (cache == null) {
            cache = new SimpleEntityCache();
            entityCache.set(cache);
            cacheCreator = true;
        }
        T entityFromCache = cache.getByRowId(entityClass, rowId);
        if (entityFromCache != null) {
            return entityFromCache;
        }

        T entity = createEntity(entityClass);
        for (ColumnDescriptor column : columns) {
            SqliteDataType type = column.getSqlLiteDataType();
            Object converted = null;
            switch (type) {
                case BLOB:
                    converted = Alice.databaseTools.convert(column, cursor.getBlob(cursor.getColumnIndex(column.getColumnName())));
                    break;
                case TEXT:
                    converted = Alice.databaseTools.convert(column, cursor.getString(cursor.getColumnIndex(column.getColumnName())));
                    break;
                case INTEGER:
                    converted = Alice.databaseTools.convert(column, cursor.getLong(cursor.getColumnIndex(column.getColumnName())));
                    break;
                case REAL:
                    converted = Alice.databaseTools.convert(column, cursor.getDouble(cursor.getColumnIndex(column.getColumnName())));
                    break;
            }
            Alice.reflectionTools.setValue(column.getField(), entity, converted);
        }
        setEntityRowId(entity, rowId);
        cache.put(entity, rowId);

        loadRelatedObjects(entityClass, cursor, entity);

        if (cacheCreator){
            cache.clear();
            entityCache.remove();
        }
        return entity;
    }

    private <T> void loadRelatedObjects(Class<T> entityClass, Cursor cursor, T entity) {
        EntityDescriptor descriptor = entityToDescriptor.get(entityClass);
        List<Field> fields = descriptor.getEntityRelatedFields();
        for (Field field : fields) {
            List related = getRelatedObjectList(cursor, descriptor, field);
            if (related != null && !related.isEmpty()) {
                Alice.reflectionTools.setValue(field, entity, related.get(0));
            }
        }

        fields = descriptor.getOneToManyFields();
        loadAndBindCollection(cursor, entity, descriptor, fields);

        fields = descriptor.getManyToManyFields();
        loadAndBindCollection(cursor, entity, descriptor, fields);
    }

    private List getRelatedObjectList(Cursor cursor, EntityDescriptor descriptor, Field field) {
        RelationQueryDescriptor queryDescriptor = descriptor.getRelationQueryDescriptorForField(field);
        AliceQuery query = queryDescriptor.buildQuery(getContext().getContentResolver(), cursor);
        return findByQuery(query);
    }

    private <T> void loadAndBindCollection(Cursor cursor, T entity, EntityDescriptor descriptor, List<Field> fields) {
        for (Field field : fields) {
            List related = getRelatedObjectList(cursor, descriptor, field);
            Class type = (Class) field.getType();
            if (Set.class.isAssignableFrom(type)) {
                Alice.reflectionTools.setValue(field, entity, new HashSet(related));
            } else if (type.isArray()) {
                Alice.reflectionTools.setValue(field, entity, Array.newInstance(type.getComponentType(), related.size()));
            } else if (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) {
                Alice.reflectionTools.setValue(field, entity, related);
            } else {
                throw new RuntimeException("Could not load entities to unknown collection " + type.getName());
            }
        }
    }
}
