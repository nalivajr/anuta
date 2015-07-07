package com.alice.components.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.alice.annonatations.database.Entity;
import com.alice.tools.Alice;
import com.alice.tools.DatabaseTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
    private Map<Class<?>, List<Field>> entityToFields;
    private Map<Field, String> fieldToKey;

    public AliceNoSQLEntityManager(Context context) {
        super(context);
        final List<Class<?>> entityClasses = getEntityClasses();
        gson = createGsonConverter(entityClasses);
        entityToFields = new HashMap<>();
        fieldToKey = new HashMap<>();
        for (Class<?> cls : entityClasses) {
            List<Field> fields = Alice.databaseTools.extractFields(cls);
            entityToFields.put(cls, fields);
            for (Field field : fields) {
                String key = getFieldKey(field);
                fieldToKey.put(field, key);
            }
        }
    }

    @Override
    protected <T> String[] getProjection(Class<T> entityClass) {
        String columnName = Alice.databaseTools.buildJsonDataColumnName(entityClass);
        return new String[] {columnName};
    }

    @Override
    protected <T> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count) {
        List<T> entities = count > 0 ? new ArrayList<T>(count) : new LinkedList<T>();
        String columnName = Alice.databaseTools.buildJsonDataColumnName(entityClass);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return entities;
            }
            boolean isCursorReady = true;
            int readLeft = count;
            long start = System.currentTimeMillis();
            long coversionTime = 0;
            while ((readLeft > 0 || count == -1) && isCursorReady) {
                long rowId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                String json = cursor.getString(cursor.getColumnIndex(columnName));

                long convStart = System.currentTimeMillis();
                T entity = gson.fromJson(json, entityClass);
                long convEnd = System.currentTimeMillis();
                coversionTime += (convEnd - convStart);

                Alice.databaseTools.setRowId(entity, rowId);
                entities.add(entity);
                isCursorReady = cursor.moveToNext();
                readLeft--;
            }
            long end = System.currentTimeMillis();
            Log.i("[PERFORMANCE]", String.format("To read %d items were spent %d millis", entities.size(), (end - start)));
            Log.i("[PERFORMANCE]", String.format("To convert %d items were spent %d millis", entities.size(), coversionTime));
        } catch (Throwable e) {
            Log.e(TAG, String.format("Error during converting cursor to %s", entityClass.getName()), e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return entities;
    }

    @Override
    protected <T> ContentValues convertToContentValues(T entity) {
        List<Field> fields = Alice.databaseTools.extractIndexedFields(entity.getClass());
        ContentValues contentValues = new ContentValues();
        for (Field field : fields) {
            String key = getFieldKey(field);
            Object val = Alice.databaseTools.getFieldValue(field, entity);
            Alice.databaseTools.putValue(contentValues, key, val);
        }

        String entityName = entity.getClass().getAnnotation(Entity.class).name();
        if (entityName == null) {
            entityName = entity.getClass().getName();
        }
        contentValues.put(DatabaseTools.ENTITY_NAME_COLUMN, entityName);

        String jsonColumn = Alice.databaseTools.buildJsonDataColumnName(entity.getClass());
        contentValues.put(jsonColumn, gson.toJson(entity));
        return contentValues;
    }

    protected Gson createGsonConverter(List<Class<?>> entityClasses) {
        GsonBuilder builder = new GsonBuilder();

        for (final Class cls : entityClasses) {
            builder
                    .registerTypeAdapter(cls, new EntitySerializer())
                    .registerTypeAdapter(cls, new EntityDeserializer(cls));
        }
        return builder.create();
    }

    /**
     * This class provides logic for serializing entities to JSON
     */
    private class EntitySerializer implements JsonSerializer {

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            List<Field> fields = entityToFields.get(src.getClass());

            for (Field field : fields) {
                Object val = Alice.databaseTools.getFieldValue(field, src);
                jsonObject.add(getFieldKey(field), context.serialize(val));
            }
            return jsonObject;
        }
    }

    /**
     * This class provides logic to deserialize JSON to entity object
     */
    private class EntityDeserializer implements JsonDeserializer {

        private Class<?> targetClass;

        public EntityDeserializer(Class<?> target) {
            this.targetClass = target;
        }

        @Override
        public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Object entity = createEntity(targetClass);

            JsonObject object = json.getAsJsonObject();
            long start = System.currentTimeMillis();
            List<Field> fields = entityToFields.get(targetClass);

            for (Field field : fields) {
                String name = fieldToKey.get(field);
                JsonElement element = object.get(name);
                String type = Alice.databaseTools.dispatchType(field);
                Object converted = null;
                switch (type) {
                    case DatabaseTools.SQLITE_TYPE_BLOB:
                        byte[] bytes = context.deserialize(element, byte[].class);
                        converted = Alice.databaseTools.convert(field, bytes);
                        break;
                    case DatabaseTools.SQLITE_TYPE_TEXT:
                        converted = Alice.databaseTools.convert(field, element.getAsString());
                        break;
                    case DatabaseTools.SQLITE_TYPE_INTEGER:
                        converted = Alice.databaseTools.convert(field, element.getAsLong());
                        break;
                    case DatabaseTools.SQLITE_TYPE_REAL:
                        converted = Alice.databaseTools.convert(field, element.getAsDouble());
                        break;
                }
                Alice.reflectionTools.setValue(field, entity, converted);
            }
            long end = System.currentTimeMillis();
            Log.i("[PERFORMANCE]", String.format("To convert an item were spent %d millis", (end - start)));
            return entity;
        }
    }
}
