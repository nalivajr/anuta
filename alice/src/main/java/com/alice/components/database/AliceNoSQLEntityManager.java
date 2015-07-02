package com.alice.components.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.components.database.models.Persistable;
import com.alice.tools.Alice;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceNoSQLEntityManager extends AbstractEntityManager {

    public static final String TAG = AliceNoSQLEntityManager.class.getSimpleName();

    private Gson gson;

    public AliceNoSQLEntityManager(Context context) {
        super(context);
        gson = new Gson();
    }

    @Override
    protected <I, T extends Persistable<I>> String[] getProjection(Class<T> entityClass) {
        String columnName = Alice.DatabaseTools.buildJsonDataColumnName(entityClass);
        return new String[]{columnName};
    }

    @Override
    protected <I, T extends Persistable<I>> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count) {
        List<T> entities = count > 0 ? new ArrayList<T>(count) : new LinkedList<T>();
        String columnName = Alice.DatabaseTools.buildJsonDataColumnName(entityClass);
        try {
            if (cursor == null || cursor.moveToFirst()) {
                return entities;
            }
            boolean isCursorReady = true;
            while ((count > 0 || count == -1) && isCursorReady) {
                String json = cursor.getString(cursor.getColumnIndex(columnName));
                Map data = gson.fromJson(json, LinkedTreeMap.class);
                entities.add(mapToEntity(entityClass, data));
                isCursorReady = cursor.moveToNext();
                count--;
            }
        } catch (Throwable e) {
            Log.w(TAG, String.format("Error during converting cursor to %s", entityClass.getName()), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return entities;
    }

    private <I, T extends Persistable<I>> T mapToEntity(Class<T> entityClass, Map data) {
        T entity = createEntity(entityClass);
        List<Field> fields = Alice.DatabaseTools.extractFields(entity.getClass());
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.value();
            Column.DataType dataType = columnAnnotation.dataType();
            try {
                field.setAccessible(true);
                Object val = data.get(columnName);
                val = Alice.DatabaseTools.convert(field, dataType, val);
                field.set(entity, val);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Could not convert data to entity's property", e);
                throw new RuntimeException("Could not convert data to entity's property", e);
            } finally {
                field.setAccessible(false);
            }
        }
        return entity;
    }

    @Override
    protected  <I, T extends Persistable<I>> ContentValues convertToContentValues(T entity) {
        List<Field> fields = Alice.DatabaseTools.extractFields(entity.getClass());
        LinkedTreeMap<String, Object> jsonMap = new LinkedTreeMap<>();
        ContentValues contentValues = new ContentValues();
        for (Field field : fields) {
            Column annotation = field.getAnnotation(Column.class);
            if (annotation == null || annotation.value().equals(BaseColumns._ID)) {
                continue;
            }
            String key = getFieldKey(field);
            Object val = Alice.DatabaseTools.getFieldValue(field, entity);
            if (annotation.index()) {
                Alice.DatabaseTools.putValue(contentValues, key, val);
            }
            jsonMap.put(key, val);
        }

        String entityName = entity.getClass().getAnnotation(Entity.class).name();
        if (entityName == null) {
            entityName = entity.getClass().getName();
        }
        contentValues.put(Alice.DatabaseTools.ENTITY_NAME_COLUMN, entityName);

        String jsonColumn = Alice.DatabaseTools.buildJsonDataColumnName(entity.getClass());
        contentValues.put(jsonColumn, gson.toJson(jsonMap));
        return contentValues;
    }
}
