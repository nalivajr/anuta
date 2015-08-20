package by.nalivajr.alice.tools;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import by.nalivajr.alice.annonatations.database.Column;
import by.nalivajr.alice.annonatations.database.Entity;
import by.nalivajr.alice.annonatations.database.Id;
import by.nalivajr.alice.annonatations.database.ManyToMany;
import by.nalivajr.alice.annonatations.database.OneToMany;
import by.nalivajr.alice.annonatations.database.RelatedEntity;
import by.nalivajr.alice.components.database.models.ColumnDescriptor;
import by.nalivajr.alice.components.database.models.EntityDescriptor;
import by.nalivajr.alice.components.database.models.Identifiable;
import by.nalivajr.alice.components.database.models.Persistable;
import by.nalivajr.alice.components.database.models.RelationDescriptor;
import by.nalivajr.alice.components.database.models.SqliteDataType;
import by.nalivajr.alice.exceptions.IncorrectMappingException;
import by.nalivajr.alice.exceptions.InvalidDataTypeException;
import by.nalivajr.alice.exceptions.InvalidEntityIdMappingException;
import by.nalivajr.alice.exceptions.InvalidRowIdMappingException;
import by.nalivajr.alice.exceptions.NotAnnotatedEntityException;
import by.nalivajr.alice.exceptions.NotEntityClassUsedInRelation;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class DatabaseTools {
    public static final String ENTITY_NAME_COLUMN = "entityName";

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
        Map<Class<?>, Set<String>> addedColumns = new HashMap<Class<?>, Set<String>>();
        Set<RelationDescriptor> descriptors = new HashSet<RelationDescriptor>();
        Map<String, RelationDescriptor> addedTables = new HashMap<String, RelationDescriptor>();

        for (Class<?> entityClass : entityClasses) {
            List<Field> relationEntityFields = Alice.reflectionTools.getFieldsAnnotatedWith(entityClass, RelatedEntity.class);

            for (Field field : relationEntityFields) {
                RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
                descriptors.add(relationDescriptor);

                String script = generateAddColumnScript(addedColumns, entityClass, relationDescriptor);
                builder.append(script);

            }

            List<Field> oneToManyColumns = Alice.reflectionTools.getFieldsAnnotatedWith(entityClass, OneToMany.class);
            for (Field field : oneToManyColumns) {
                RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
                descriptors.add(relationDescriptor);

                String script = generateAddColumnScript(addedColumns, entityClass, relationDescriptor);
                builder.append(script);
            }

            List<Field> manyToManyColumns = Alice.reflectionTools.getFieldsAnnotatedWith(entityClass, ManyToMany.class);
            for (Field field : manyToManyColumns) {
                RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
                addedTables.put(relationDescriptor.getRelationTable(), relationDescriptor);
            }
        }

        for (String tableName : addedTables.keySet()) {
            RelationDescriptor descriptor = addedTables.get(tableName);
            builder.append("CREATE TABLE ")
                    .append(tableName)
                    .append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT, ")
                    .append(descriptor.getJoinRelationColumnName())
                    .append(' ')
                    .append(descriptor.getRelationColumnType())
                    .append(',').append(' ')
                    .append(descriptor.getJoinReferencedRelationColumnName())
                    .append(' ')
                    .append(descriptor.getRelationReferencedColumnType())
                    .append(')').append(';');
        }

        return builder.toString();
    }

    protected String generateAddColumnScript(Map<Class<?>, Set<String>> addedColumns, Class<?> entityClass, RelationDescriptor relationDescriptor) {
        Class<?> relationHoldingEntity = relationDescriptor.getRelationHoldingEntity();
        SqliteDataType dataType = relationHoldingEntity == entityClass ? relationDescriptor.getRelationColumnType() : relationDescriptor.getRelationReferencedColumnType();
        String columnName = relationHoldingEntity == entityClass ? relationDescriptor.getJoinRelationColumnName() : relationDescriptor.getJoinReferencedRelationColumnName();
        Set<String> addedColumnsSet = addedColumns.get(relationHoldingEntity);
        if (addedColumnsSet == null) {
            addedColumnsSet = new HashSet<String>();
            addedColumns.put(relationHoldingEntity, addedColumnsSet);
        }
        if (addedColumnsSet.contains(columnName) || Alice.databaseTools.getFieldForColumnName(columnName, relationHoldingEntity) != null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("ALTER TABLE ")
                .append(relationDescriptor.getRelationTable())
                .append(" ADD COLUMN ")
                .append(columnName)
                .append(' ')
                .append(dataType)
                .append(';');
        addedColumnsSet.add(columnName);
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
        String tableName = Alice.databaseTools.getEntityTableName(cls, entityAnnotation);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE %s (", tableName));
        List<Field> columnFields = extractFields(cls);

        for (Field field : columnFields) {
            String columnScript = buildColumnDefinition(field);
            builder.append(columnScript);
            builder.append(',');
        }

        String primaryKeyScript = buildPrimaryKeyScript(columnFields);
        builder.append(primaryKeyScript);
        builder.setCharAt(builder.length() - 1, ')');
        builder.append(';');
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
        String tableName = getEntityTableName(cls, entityAnnotation);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE %s (", tableName));
        List<Field> columnFields = extractFields(cls);

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
                .append(ENTITY_NAME_COLUMN + " TEXT, ")
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
        String tableName = getEntityTableName(cls, entityAnnotation);
        return String.format("DROP TABLE %s;", tableName);
    }


    private <T> void validateColumnFields(List<Field> columnFields, Class<T> cls) {
        Set<String> columnNames = new HashSet<String>();
        int ids = 0;
        for(Field field : columnFields) {
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno == null) {
                continue;
            }
            boolean isIdField = field.getAnnotation(Id.class) != null;
            if (isIdField) {
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
            if (isIdField && name.equals(BaseColumns._ID)) {
                Class type = field.getType();
                if (type != Long.class && type != Long.TYPE) {
                    throw new InvalidRowIdMappingException(cls);
                }
            }
        }
        if (ids != 1) {
            throw new InvalidEntityIdMappingException(ids, cls);
        }
    }

    public <T> String buildJsonDataColumnName(Class<T> cls) {
        return cls.getSimpleName() + JSON_DATA_COLUMN_SUFFIX;
    }

    /**
     * Extract all fields, which are annotated with {@link Id} or {@link Column} and should be persisted. Do not extracts _id field
     */
    public <T> List<Field> extractFields(Class<T> cls) {
        return extractColumnsFields(cls, null);
    }

    /**
     * Extract all fields, which are annotated with {@link Id} or {@link Column} having {@link Column#index()} set to true and should be persisted. Do not extracts _id field
     */
    public  <T> List<Field> extractIndexedFields(Class<T> cls) {
        return extractColumnsFields(cls, true);
    }

    private <T> List<Field> extractColumnsFields(Class<T> cls, Boolean indexedOnly) {
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException(cls);
        }

        List<Field> resultList = new ArrayList<Field>();
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
        String type = dispatchType(field).name();
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
            if (name.equals(BaseColumns._ID)) {
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
     *          {@link SqliteDataType#NULL}
     *     </li>
     *     <li>
     *          {@link SqliteDataType#INTEGER}
     *     </li>
     *     <li>
     *          {@link SqliteDataType#REAL}
     *     </li>
     *     <li>
     *          {@link SqliteDataType#TEXT}
     *     </li>
     *     <li>
     *          {@link SqliteDataType#BLOB}
     *     </li>
     * </ul>
     */
    public SqliteDataType dispatchType(Field field) {
        Column.DataType dataType = field.getAnnotation(Column.class).dataType();

        switch (dataType) {
            case DATE_MILLIS:
            case ENUM_ORDINAL:
                return SqliteDataType.INTEGER;
            case DATE_TIMESTAMP:
            case ENUM_STRING:
            case JSON_STRING:
                return SqliteDataType.TEXT;
            case SERIALIZABLE:
            case BLOB:
            case BLOB_STRING:
                return SqliteDataType.BLOB;
            default:
            case AUTO:
                Class cls = field.getType();
                if (cls == Boolean.TYPE || cls == Boolean.class) {
                    return SqliteDataType.TEXT;
                }
                if (cls == Character.TYPE || cls == Character.class) {
                    return SqliteDataType.TEXT;
                }
                if (cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
                    if (cls == Float.TYPE || cls == Float.class ||
                            cls == Double.TYPE || cls == Double.class) {
                        return SqliteDataType.REAL;
                    }
                    return SqliteDataType.INTEGER;
                }
                if (Date.class.isAssignableFrom(cls)) {
                    return SqliteDataType.INTEGER;
                }
                if (cls == byte[].class) {
                    return SqliteDataType.BLOB;
                }
                if (cls.isEnum()) {     //ENUM_STRING
                    return SqliteDataType.TEXT;
                }
                if (String.class.isAssignableFrom(cls)) {
                    return SqliteDataType.TEXT;
                }
                return SqliteDataType.TEXT;
        }
    }

    /**
     * Extracts fields values and adopts them according to {@link Column.DataType} for this field
     * @param field value source field
     * @param entity source entity
     * @return converted value, corresponding to {@link Column.DataType}
     */
    public Object getFieldValue(Field field, Column.DataType dataType, Object entity) {
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
                if (cls == byte[].class) {
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
     * @param fieldDescriptor target field descriptor
     * @param val the value to be converted
     * @return converted value
     */
    public Object convert(ColumnDescriptor fieldDescriptor, Object val) {

        if (val == null) {
            return null;
        }

        Field field = fieldDescriptor.getField();
        Column.DataType dataType = fieldDescriptor.getColumnPersistingDataTypeStrategy();

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
                if (cls == byte[].class) {
                    return val;
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
            contentValues.put(key, (byte[])val);
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
        validateColumnFields(columnFields, cls);
        List<Field> relatedEntityFields = Alice.reflectionTools.getFieldsAnnotatedWith(cls, RelatedEntity.class);
        validateRelatedEntityFields(relatedEntityFields, cls);
        List<Field> oneToManyFields = Alice.reflectionTools.getFieldsAnnotatedWith(cls, OneToMany.class);
        validateOneToManyFields(oneToManyFields, cls);
        List<Field> manyToManyFields = Alice.reflectionTools.getFieldsAnnotatedWith(cls, ManyToMany.class);
        validateManyToManyFields(manyToManyFields, cls);
    }

    private void validateRelatedEntityFields(List<Field> relatedEntityFields, Class<?> cls) {
        for (Field field : relatedEntityFields) {
            RelatedEntity anno = field.getAnnotation(RelatedEntity.class);
            Class<?> relationClass = anno.dependentEntityClass();
            if (!Alice.reflectionTools.isEntityClass(relationClass)) {
                throw new RuntimeException(String.format("Relation class %s is not an entity", relationClass));
            }
            Class<?> relatedEntity = field.getType();
            if (!Alice.reflectionTools.isEntityClass(relatedEntity)) {
                throw new NotEntityClassUsedInRelation(relatedEntity, field);
            }
        }
    }

    private void validateOneToManyFields(List<Field> oneToManyFields, Class<?> cls) {
        for (Field field : oneToManyFields) {
             validateFieldAndGetRelatedEntityClass(field);
        }
    }

    private void validateManyToManyFields(List<Field> manyToManyFields, Class<?> cls) {
        for (Field field : manyToManyFields) {
            validateFieldAndGetRelatedEntityClass(field);
        }
    }

    private boolean hasColumn(Class<?> relatedEntity, String columnName) {
        List<Field> columnFields = extractColumnsFields(relatedEntity, false);
        for (Field field : columnFields) {
            Column column = field.getAnnotation(Column.class);
            String name = column.value();
            if (name.isEmpty()) {
                continue;
            }
            if (name.equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates relation field and returns the class of entity in relation
     * @param field the field to validate
     * @return the class of entity in relation
     */
    public Class<?> validateFieldAndGetRelatedEntityClass(Field field) {
        Class<?> relatedEntity = getRelatedGenericClass(field);
        if (!Alice.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }
        return relatedEntity;
    }

    public Class<?> getRelatedGenericClass(Field field) {
        Class<?> relatedEntity = field.getType();
        if (relatedEntity.isArray()) {
            relatedEntity = relatedEntity.getComponentType();
        } else if (!Collection.class.isAssignableFrom(relatedEntity)) {
            throw new RuntimeException(String.format("Only arrays and %s can be used in OneToMany relations", Collection.class.getName()));
        } else {
            List<Class<?>> relationClasses = Alice.reflectionTools.getGenericClasses(field);
            if (relationClasses.isEmpty()) {
                throw new RuntimeException(String.format("Could not detect the type of related entities for field %s in class %s", field.getName(), field.getDeclaringClass()));
            }
            relatedEntity = relationClasses.get(0);
        }
        return relatedEntity;
    }

    public List<EntityDescriptor> generateDescriptorsFor(List<Class<?>> classes) {
        validateEntityClasses(classes);
        List<EntityDescriptor> result = new ArrayList<EntityDescriptor>(classes.size());
        for (Class<?> cls : classes) {
            EntityDescriptor entityDescriptor = new EntityDescriptor(cls);
            result.add(entityDescriptor);
        }
        return result;
    }

    /**
     * Extracts the name of table for the entity
     * @param cls class to check
     * @param entityAnnotation the instance of {@link Entity} annotation for the given class
     * @return the name of table for the entity
     */
    public <T> String getEntityTableName(Class<T> cls, Entity entityAnnotation) {
        String tableName = entityAnnotation.tableName();
        if (tableName.isEmpty()) {
            tableName = cls.getSimpleName();
        }
        return tableName;
    }

    /**
     * Extracts the name of table for the entity
     * @param cls class to check
     * @return the name of table for the entity or null if the given class is not an entity
     */
    public <T> String getEntityTableName(Class<T> cls) {
        if (!Alice.reflectionTools.isEntityClass(cls)) {
            return null;
        }
        return getEntityTableName(cls, cls.getAnnotation(Entity.class));
    }

    public Field getFieldForColumnName(String columnName, Class<?> entityClass) {
        List<Field> fields = extractColumnsFields(entityClass, false);
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            String name = column.value();
            if (name.isEmpty()) {
                name = field.getName();
            }
            if (name.equals(columnName)) {
                return field;
            }
        }
        return null;
    }

    public List<String> getRelatedTablesNames(Class<?> cls) {
        List<String> tables = new LinkedList<String>();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            ManyToMany anno = field.getAnnotation(ManyToMany.class);
            if (anno == null) {
                continue;
            }
            String tableName = anno.relationTableName();
            if (tableName.isEmpty()) {
                Class<?> related = getRelatedGenericClass(field);
                tableName = Alice.databaseTools.buildRelationTableName(cls, related);
            }
            tables.add(tableName);
        }
        return tables;
    }

    /**
     * Generates the name of many-to-many relation table for the given entities
     * @return generated name of the table
     */
    public String buildRelationTableName(Class<?> entity1, Class<?> entity2) {
        String entity1Table = getEntityTableName(entity1).toLowerCase();
        String entity2Table = getEntityTableName(entity2).toLowerCase();

        String first = entity1Table.compareTo(entity2Table) > 0 ? entity2Table : entity1Table;
        String second = entity1Table.compareTo(entity2Table) > 0 ? entity1Table : entity2Table;
        return String.format("%s_%s", first, second);
    }

    public Uri buildUriForTableName(String tableName, String authority) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(tableName)
                .build();
    }
}
