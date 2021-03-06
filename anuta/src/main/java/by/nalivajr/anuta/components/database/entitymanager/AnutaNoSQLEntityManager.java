package by.nalivajr.anuta.components.database.entitymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.components.database.models.descriptors.ColumnDescriptor;
import by.nalivajr.anuta.components.database.models.descriptors.EntityDescriptor;
import by.nalivajr.anuta.components.database.models.enums.SqliteDataType;
import by.nalivajr.anuta.tools.Anuta;
import by.nalivajr.anuta.tools.DatabaseTools;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com

 * @deprecated Not implemented totally, can be used only without relations
 */
@Deprecated
public abstract class AnutaNoSQLEntityManager extends AbstractEntityManager {

    public static final String TAG = AnutaNoSQLEntityManager.class.getSimpleName();

    private Gson gson;
    private Map<Class<?>, EntityDescriptor> entityToDescriptor;

    public AnutaNoSQLEntityManager(Context context) {
        super(context);
        List<Class<?>> entityClasses = getEntityClasses();
        final List<EntityDescriptor> entityDescriptors = Anuta.databaseTools.generateDescriptorsFor(entityClasses);
        gson = createGsonConverter(entityClasses);
        entityToDescriptor = new HashMap<Class<?>, EntityDescriptor>();
        for (EntityDescriptor descriptor : entityDescriptors) {
            entityToDescriptor.put(descriptor.getEntityClass(), descriptor);
        }
    }

    @Override
    protected <T> String[] getProjection(Class<T> entityClass) {
        String columnName = Anuta.databaseTools.buildJsonDataColumnName(entityClass);
        return new String[] {columnName};
    }

    @Override
    protected <T> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count, boolean closeAfter) {
        List<T> entities = count > 0 ? new ArrayList<T>(count) : new LinkedList<T>();
        String columnName = Anuta.databaseTools.buildJsonDataColumnName(entityClass);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return entities;
            }
            boolean isCursorReady = true;
            int readLeft = count;
            while ((readLeft > 0 || count == -1) && isCursorReady) {
                T entity = cursorToEntity(entityClass, cursor, columnName);
                entities.add(entity);
                isCursorReady = cursor.moveToNext();
                readLeft--;
            }
        } catch (Throwable e) {
            Log.e(TAG, String.format("Error during converting cursor to %s", entityClass.getName()), e);
            throw new RuntimeException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return entities;
    }

    @Override
    protected <T> T cursorToEntity(Class<T> entityClass, Cursor cursor) {
        String columnName = Anuta.databaseTools.buildJsonDataColumnName(entityClass);
        Collection<ColumnDescriptor> columns = entityToDescriptor.get(entityClass).getFieldDescriptors();
        try {
            return cursorToEntity(entityClass, cursor, columnName);
        } catch (Throwable e) {
            Log.w(TAG, "Cold not convert cursor to entity", e);
        }
        return null;
    }

    private <T> T cursorToEntity(Class<T> entityClass, Cursor cursor, String columnName) {
        long rowId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String json = cursor.getString(cursor.getColumnIndex(columnName));

        T entity = gson.fromJson(json, entityClass);

        setEntityRowId(entity, rowId);
        return entity;
    }

    @Override
    protected <T> ContentValues convertToContentValues(T entity) {
        EntityDescriptor entityDescriptor = entityToDescriptor.get(entity.getClass());
        List<Field> fields = entityDescriptor.getIndexFields();
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

        String entityName = entity.getClass().getAnnotation(Entity.class).name();
        if (entityName == null) {
            entityName = entity.getClass().getName();
        }
        contentValues.put(DatabaseTools.ENTITY_NAME_COLUMN, entityName);

        String jsonColumn = Anuta.databaseTools.buildJsonDataColumnName(entity.getClass());
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

            EntityDescriptor entityDescriptor = entityToDescriptor.get(src.getClass());
            List<Field> fields = entityDescriptor.getFields();

            for (Field field : fields) {
                String fieldKey = entityDescriptor.getFieldKey(field);
                if (fieldKey.equals(BaseColumns._ID)) {
                    continue;
                }
                Column.DataType dataType = entityDescriptor.getFieldDescriptor(field).getColumnPersistingDataTypeStrategy();
                Object val = Anuta.databaseTools.getFieldValue(field, dataType, src);
                jsonObject.add(fieldKey, context.serialize(val));
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
            EntityDescriptor entityDescriptor = entityToDescriptor.get(targetClass);

            Collection<ColumnDescriptor> descriptors = entityDescriptor.getFieldDescriptors();
            for (ColumnDescriptor column : descriptors) {
                String name = column.getColumnName();
                if (name.equals(BaseColumns._ID)) {
                    continue;
                }
                JsonElement element = object.get(name);
                SqliteDataType type = column.getSqlLiteDataType();
                Object converted = null;
                switch (type) {
                    case BLOB:
                        byte[] bytes = context.deserialize(element, byte[].class);
                        converted = Anuta.databaseTools.convert(column, bytes);
                        break;
                    case TEXT:
                        converted = Anuta.databaseTools.convert(column, element.getAsString());
                        break;
                    case INTEGER:
                        converted = Anuta.databaseTools.convert(column, element.getAsLong());
                        break;
                    case REAL:
                        converted = Anuta.databaseTools.convert(column, element.getAsDouble());
                        break;
                }
                Anuta.reflectionTools.setValue(column.getField(), entity, converted);
            }
            return entity;
        }
    }
}
