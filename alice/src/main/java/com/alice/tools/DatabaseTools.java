package com.alice.tools;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.util.Log;

import com.alice.annonatations.database.Column;
import com.alice.annonatations.database.Entity;
import com.alice.annonatations.database.Id;
import com.alice.components.database.models.Identifiable;
import com.alice.components.database.models.Persistable;
import com.alice.exceptions.IncorrectMappingException;
import com.alice.exceptions.InvalidDataTypeException;
import com.alice.exceptions.InvalidEntityIdMappingException;
import com.alice.exceptions.NotAnnotatedEntityException;
import com.alice.exceptions.NotAnnotatedFieldException;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class DatabaseTools {
    public static final String ENTITY_NAME_COLUMN = "entityName";

    public static final String SQLITE_TYPE_TEXT = "TEXT";
    public static final String SQLITE_TYPE_INTEGER = "INTEGER";
    public static final String SQLITE_TYPE_REAL = "REAL";
    public static final String SQLITE_TYPE_BLOB = "BLOB";

    private static final String EMPTY_COLUMN_MANE = "";

    private static final String TAG = DatabaseTools.class.getSimpleName();
    private static final Gson gson = new Gson();
    public static final String JSON_DATA_COLUMN_SUFFIX = "JsonData";

    DatabaseTools() {
    }

    /**
     * Generates tables creation script for the given list of entity classes
     * @param entityClasses classes of entities
     * @return string, representing table creation script;
     * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
     */
    public <T extends Identifiable> String generateRelationalTableScript(List<Class<T>> entityClasses) {
        StringBuilder builder = new StringBuilder();
        for (Class entityClass : entityClasses) {
            builder.append(generateRelationalTableScript(entityClass));
        }
        return builder.toString();
    }

    /**
     * Generates NoSQL tables creation script for the given list of entity classes
     * @param entityClasses classes of entities
     * @return string, representing table creation script;
     * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
     */
    public <T extends Identifiable> String generateNoSQLTableScript(List<Class<T>> entityClasses) {
        StringBuilder builder = new StringBuilder();
        for (Class entityClass : entityClasses) {
            builder.append(generateNoSQLTableScript(entityClass));
        }
        return builder.toString();
    }

    /**
     * Generate script, which drops tables from database
     * @param entityClasses entity classes
     */
    public <T extends Identifiable> String generateTableDeletionScript(List<Class<T>> entityClasses) {
        StringBuilder builder = new StringBuilder();
        for (Class entityClass : entityClasses) {
            builder.append(generateDropTableScript(entityClass));
        }
        return builder.toString();
    }

    /**
     * Generates table creation script for the given entity class
     * @param cls the class of entity
     * @return string, representing table creation script;
     * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
     */
    public <T> String generateRelationalTableScript(Class<T> cls) {
        validateEntityClass(cls);
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName();
        if (tableName.isEmpty()) {
            tableName = cls.getSimpleName();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE %s (", tableName));
        List<Field> columnFields = extractFields(cls);
        validateFields(columnFields, cls);

        for (Field field : columnFields) {
            String columnScript = buildColumnDefinition(field);
            builder.append(columnScript);
            builder.append(',');
        }

        String primaryKeyScript = buildPrimaryKeyScript(columnFields);
        builder.append(primaryKeyScript);
        builder.append(')').append(';');
        return builder.toString();
    }

    /**
     * Generates NoSQL mode table creation script for the given entity class
     * @param cls the class of entity
     * @return string, representing table creation script;
     * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
     */
    public <T> String generateNoSQLTableScript(Class<T> cls) {
        validateEntityClass(cls);
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName();
        if (tableName.isEmpty()) {
            tableName = cls.getSimpleName();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE %s (", tableName));
        List<Field> columnFields = extractFields(cls);
        validateFields(columnFields, cls);

        for (Field field : columnFields) {
            boolean index;
            Id idAnno = field.getAnnotation(Id.class);
            Column columnAnno = field.getAnnotation(Column.class);
            String columnName = columnAnno.value();
            if (columnName.isEmpty()) {
                columnName = field.getName();
            }
            index = idAnno != null || columnAnno.index() || columnName.equals(BaseColumns._ID);
            if (!index) {
                continue;
            }
            String columnScript = buildColumnDefinition(field);
            builder.append(columnScript);
            builder.append(',');
        }
        // Appends definition of columns for entity name and JSON data
        builder
                .append(ENTITY_NAME_COLUMN + " TEXT,")
                .append(buildJsonDataColumnName(cls)).append(" TEXT,");

        String primaryKeyScript = buildPrimaryKeyScript(columnFields);
        builder.append(primaryKeyScript);
        builder.setCharAt(builder.length() - 1, ')');
        builder.append(';');
        return builder.toString();
    }

    /**
     * Generates script, which removes table for entity from database
     * @param cls entity class
     */
    public <T> String generateDropTableScript(Class<T> cls) {
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException(cls);
        }
        String tableName = entityAnnotation.tableName();
        if (tableName.isEmpty()) {
            tableName = cls.getSimpleName();
        }
        return String.format("DROP TABLE %s;", tableName);
    }


    private <T> void validateFields(List<Field> columnFields, Class<T> cls) {
        Set<String> columnNames = new HashSet<>();
        int ids = 0;
        for(Field field : columnFields) {
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno == null) {
                continue;
            }
            if (field.getAnnotation(Id.class) != null) {
                ids++;
            }
            String name = columnAnno.value();
            if (name.isEmpty()) {
                name = field.getName();
            }
            if (columnNames.contains(name)) {
                throw new IncorrectMappingException(field);
            }
            columnNames.add(name);
        }
        if (ids != 1) {
            throw new InvalidEntityIdMappingException(ids, cls);
        }
    }

    public <T> String buildJsonDataColumnName(Class<T> cls) {
        return cls.getSimpleName() + JSON_DATA_COLUMN_SUFFIX;
    }

    /**
     * Extract all fields, which are annotated with {@link Column} and should be persisted. Do not extracts _id field
     */
    public <T> List<Field> extractFields(Class<T> cls) {
        return extractColumnsFields(cls, null);
    }

    /**
     * Extract all fields, which are annotated with {@link Column} having {@link Column#index()} set to true and should be persisted. Do not extracts _id field
     */
    public  <T> List<Field> extractIndexedFields(Class<T> cls) {
        return extractColumnsFields(cls, true);
    }

    private <T> List<Field> extractColumnsFields(Class<T> cls, Boolean indexedOnly) {
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException(cls);
        }

        List<Field> resultList = new ArrayList<>();
        resultList.addAll(Arrays.asList(cls.getDeclaredFields()));

        Class processing = cls;
        Entity.InheritancePolicy policy = entityAnnotation.inheritColumns();
        while (entityAnnotation != null && policy != Entity.InheritancePolicy.NO) {
            processing = processing.getSuperclass();
            entityAnnotation = (Entity) processing.getAnnotation(Entity.class);
            resultList.addAll(Arrays.asList(processing.getDeclaredFields()));

            if (policy == Entity.InheritancePolicy.PARENT_ONLY) {
                break;
            } else if (policy == Entity.InheritancePolicy.SEQUENTIAL_NO_ID) {
                policy = entityAnnotation.inheritColumns();
            }
        }

        for (int i = 0; i < resultList.size(); i++) {
            Field field = resultList.get(i);
            Column columnAnnotation = field.getAnnotation(Column.class);
            Id idAnnotation = field.getAnnotation(Id.class);
            if (columnAnnotation == null && idAnnotation == null) {
                resultList.remove(i--);
                continue;
            }
            if (idAnnotation == null && (indexedOnly != null && columnAnnotation.index() != indexedOnly)) {
                resultList.remove(i--);
                continue;
            }
            String name = columnAnnotation == null ? EMPTY_COLUMN_MANE : columnAnnotation.value();
            if (name.isEmpty()) {
                name = field.getName();
            }
            if (name.equals(BaseColumns._ID)) {
                resultList.remove(i--);
                continue;
            }
            if (idAnnotation != null && !isIdShouldBeInherited(cls, field)) {
                resultList.remove(i--);
            }
        }
        return resultList;
    }

    private <T> boolean isIdShouldBeInherited(Class<T> cls, Field field) {
        Class fieldClass = field.getDeclaringClass();
        return fieldClass == cls;
    }

    /**
     * Generates script, which describes column in database
     */
    private String buildColumnDefinition(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        String columnName = columnAnnotation.value();
        if (columnName.isEmpty()) {
            columnName = field.getName();
        }
        String type = dispatchType(field);
        StringBuilder builder = new StringBuilder(columnName.length() + type.length() + 1);
        builder.append(columnName);
        builder.append(' ');
        builder.append(type);
        if (columnName.equals(BaseColumns._ID)) {
            builder.append(" PRIMARY KEY AUTOINCREMENT");
        } else if (field.getAnnotation(Id.class) != null) {
            builder.append(" UNIQUE");
        }
        return builder.toString();
    }

    private String buildPrimaryKeyScript(List<Field> columnFields) {
        boolean hasRowIdDefinition = false;
        for (Field column : columnFields) {
            Column columnAnnotation = column.getAnnotation(Column.class);
            String name = columnAnnotation.value();
            if (name.isEmpty()) {
                name = column.getName();
            }
            if (name.equals(BaseColumns._ID) &&
                    Number.class.isAssignableFrom(column.getType()) &&
                    column.getType() != Float.class && column.getType() != Float.TYPE &&
                    column.getType() != Double.class && column.getType() != Double.TYPE) {
                hasRowIdDefinition = true;
            }
        }
        StringBuilder builder = new StringBuilder();
        if (!hasRowIdDefinition)  {
            builder.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
        }
        return builder.toString();
    }

    /**
     * Detects which SQLite data type should be used to store data for the given field
     * @param field property which data should be stored
     * @return string value which is one of
     * <ul>
     *     <li>
     *
     *     </li>
     * </ul>
     */
    public String dispatchType(Field field) {
        Column.DataType dataType = field.getAnnotation(Column.class).dataType();

        switch (dataType) {
            case DATE_MILLIS:
            case ENUM_ORDINAL:
                return SQLITE_TYPE_INTEGER;
            case DATE_TIMESTAMP:
            case ENUM_STRING:
            case JSON_STRING:
                return SQLITE_TYPE_TEXT;
            case SERIALIZABLE:
            case BLOB:
            case BLOB_STRING:
                return SQLITE_TYPE_BLOB;
            default:
            case AUTO:
                Class cls = field.getType();
                if (cls == Boolean.TYPE || cls == Boolean.class) {
                    return SQLITE_TYPE_TEXT;
                }
                if (cls == Character.TYPE || cls == Character.class) {
                    return SQLITE_TYPE_TEXT;
                }
                if (cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
                    if (cls == Float.TYPE || cls == Float.class ||
                            cls == Double.TYPE || cls == Double.class) {
                        return SQLITE_TYPE_REAL;
                    }
                    return SQLITE_TYPE_INTEGER;
                }
                if (Date.class.isAssignableFrom(cls)) {
                    return SQLITE_TYPE_INTEGER;
                }
                if (cls.isEnum()) {     //ENUM_STRING
                    return SQLITE_TYPE_TEXT;
                }
                if (String.class.isAssignableFrom(cls)) {
                    return SQLITE_TYPE_TEXT;
                }
                return SQLITE_TYPE_TEXT;
        }
    }

    /**
     * Extracts fields values and adopts them according to {@link com.alice.annonatations.database.Column.DataType} for this field
     * @param field value source field
     * @param entity source entity
     * @return converted value, corresponding to {@link com.alice.annonatations.database.Column.DataType}
     */
    public Object getFieldValue(Field field, Object entity) {
        Column annotation = field.getAnnotation(Column.class);
        if (annotation == null) {
            throw new NotAnnotatedFieldException(field);
        }
        Column.DataType dataType = annotation.dataType();

        Object val = null;
        field.setAccessible(true);
        try {
            val = field.get(entity);
        } catch (Throwable e) {
            Log.e(TAG, "Could not get value", e);
            return null;
        } finally {
            field.setAccessible(false);
        }
        if (val == null) {
            return null;
        }

        switch (dataType) {
            case DATE_MILLIS:
                return ((Date) val).getTime();
            case ENUM_ORDINAL:
                return ((Enum) val).ordinal();
            case DATE_TIMESTAMP:
                return SimpleDateFormat.getDateTimeInstance().format(val);
            case ENUM_STRING:
                return ((Enum) val).name();
            case JSON_STRING:
                return gson.toJson(val);
            case SERIALIZABLE:
                return Converter.toByteArray(field, val);
            case BLOB_STRING:
                return ((String)val).getBytes();
            case BLOB:
                if (val instanceof byte[]) {
                    return val;
                }
            default:
            case AUTO:
                Class cls = field.getType();
                if (cls == Boolean.TYPE || cls == Boolean.class) {
                    return String.valueOf(val);
                }
                if (cls == Character.TYPE || cls == Character.class) {
                    return String.valueOf(val);
                }
                if (cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
                    return val;
                }
                if (cls.isEnum()) {     //ENUM_STRING
                    return ((Enum)val).name();
                }
                if (val instanceof String) {
                    return val;
                }
                if (val instanceof Date) {
                    return ((Date)val).getTime();
                }
                return gson.toJson(val);
        }
    }

    /**
     * Converts value from data type to entity's property value
     * @param field target field
     * @param val the value to be converted
     * @return converted value
     */
    public Object convert(Field field, Object val) {

        if (val == null) {
            return null;
        }

        Column.DataType dataType = field.getAnnotation(Column.class).dataType();

        switch (dataType) {
            case DATE_MILLIS:
                return new Date((Long) val);
            case ENUM_ORDINAL:
                int index = ((Number) val).intValue();
                return (field.getType().getEnumConstants())[index];
            case DATE_TIMESTAMP:
                try {
                    return SimpleDateFormat.getDateTimeInstance().parse((String) val);
                } catch (ParseException e) {
                    Log.e(TAG, "Could not parse timestamp to date", e);
                }
            case ENUM_STRING:
                Class<?> type = field.getType();
                if (!type.isEnum()) {
                    throw new InvalidDataTypeException(field);
                }
                return Converter.getStringAsEnum((String) val, type);
            case JSON_STRING:
                return gson.fromJson((String)val, field.getType());
            case SERIALIZABLE:
                return Converter.readObject(field, (byte[]) val);
            case BLOB_STRING:
                return new String((byte[])val);
            case BLOB:
                if (val instanceof byte[]) {
                    return val;
                }
            default:
            case AUTO:
                Class cls = field.getType();
                if (cls == Boolean.TYPE || cls == Boolean.class) {
                    return Boolean.parseBoolean((String) val);
                }
                if (cls == Character.class || cls == Character.TYPE) {
                    return ((String)val).charAt(0);
                }
                if (cls.isPrimitive() || Number.class.isAssignableFrom(cls) || Character.class.isAssignableFrom(cls)) {
                    if (cls == Byte.class || cls == Byte.TYPE) {
                        return ((Number)val).byteValue();
                    }
                    if (cls == Short.class || cls == Short.TYPE) {
                        return ((Number)val).shortValue();
                    }
                    if (cls == Integer.class || cls == Integer.TYPE) {
                        return ((Number)val).intValue();
                    }
                    if (cls == Long.class || cls == Long.TYPE) {
                        return ((Number)val).longValue();
                    }
                    if (cls == Float.class || cls == Float.TYPE) {
                        return ((Number)val).floatValue();
                    }
                    return ((Number)val).doubleValue();
                }
                if (cls == Date.class) {
                    return new Date(((Number)val).longValue());
                }
                if (cls.isEnum()) {     //ENUM_STRING
                    return Converter.getStringAsEnum((String) val, cls);
                }
                if (val instanceof byte[]) {
                    return Converter.readObject(field, (byte[])val);
                }
                if (String.class.isAssignableFrom(field.getType())) {
                    return val;
                }
                return gson.fromJson((String) val, field.getType());
        }
    }

    /**
     * Puts value according to its type to {@link ContentValues} object
     * @param contentValues destination
     * @param key value key
     * @param val the value to be put
     */
    public void putValue(ContentValues contentValues, String key, Object val) {
        if (val == null) {
            contentValues.putNull(key);
        } else if (val instanceof Boolean) {
            contentValues.put(key, (Boolean)val);
        } else if (val instanceof String) {
            contentValues.put(key, (String)val);
        } else if (val instanceof byte[]) {
            contentValues.put(key, (byte)val);
        } else if (val instanceof Byte) {
            contentValues.put(key, (Byte)val);
        } else if (val instanceof Short) {
            contentValues.put(key,((Short)val));
        } else if (val instanceof Integer) {
            contentValues.put(key, (Integer)val);
        } else if (val instanceof Long) {
            contentValues.put(key, (Long)val);
        } else if (val instanceof Float) {
            contentValues.put(key, (Float)val);
        } else if (val instanceof Double) {
            contentValues.put(key, (Double)val);
        }
    }

    /**
     * Tries to set rowId to field with column name or property with name {@link BaseColumns#_ID}
     * @param entity target entity
     * @param rowId the id to be set
     */
    public <T> void setRowId(T entity, long rowId) {
        if (entity instanceof Persistable) {
            ((Persistable) entity).setRowId(rowId);
        }
        Field[] fields = entity.getClass().getDeclaredFields();
        Field target = null;
        for (Field field : fields) {
            if (field.getAnnotation(Id.class) != null && field.getName().equals(BaseColumns._ID)) {
                target = field;
                break;
            }
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno == null) {
                continue;
            }
            String name = columnAnno.value().isEmpty() ? field.getName() : columnAnno.value();
            if (name.equals(BaseColumns._ID)) {
                target = field;
                break;
            }
        }
        if (target != null) {
            Alice.reflectionTools.setValue(target, entity, rowId);
        }
    }
    /**
     * Tries to get rowId to field with column name or property with name {@link BaseColumns#_ID}
     * @param entity target entity
     * @return extracted rowId if possible and null otherwise
     */
    public <T> Long getRowId(T entity) {
        if (entity instanceof Persistable) {
            ((Persistable) entity).getRowId();
        }
        Field[] fields = entity.getClass().getDeclaredFields();
        Field target = null;
        for (Field field : fields) {
            if (field.getAnnotation(Id.class) != null && field.getName().equals(BaseColumns._ID)) {
                target = field;
                break;
            }
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno == null) {
                continue;
            }
            String name = columnAnno.value().isEmpty() ? field.getName() : columnAnno.value();
            if (name.equals(BaseColumns._ID)) {
                target = field;
                break;
            }
        }
        if (target != null) {
            return ((Long) Alice.reflectionTools.getValue(target, entity));
        }
        return null;
    }

    /**
     * Validates if each class in the given collection is mapped correctly
     * @param entityClasses collection of classes to be validated
     */
    public void validateEntityClasses(Collection<Class<?>> entityClasses) {
        for (Class<?> cls : entityClasses) {
            validateEntityClass(cls);
        }
    }

    /**
     * Validates if entity class is mapped correctly
     * @param cls a class to be validated
     */
    public void validateEntityClass(Class<?> cls) {
        Entity entityAnno = cls.getAnnotation(Entity.class);
        if (entityAnno == null) {
            throw new NotAnnotatedEntityException(cls);
        }
        List<Field> columnFields = extractFields(cls);
        validateFields(columnFields, cls);
    }
}
