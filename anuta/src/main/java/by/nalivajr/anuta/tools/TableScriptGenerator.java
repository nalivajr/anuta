package by.nalivajr.anuta.tools;

import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.ManyToMany;
import by.nalivajr.anuta.annonatations.database.OneToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.components.database.models.Identifiable;
import by.nalivajr.anuta.components.database.models.descriptors.RelationDescriptor;
import by.nalivajr.anuta.components.database.models.enums.SqliteDataType;
import by.nalivajr.anuta.exceptions.NotAnnotatedEntityException;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class TableScriptGenerator {

    public static final String ENTITY_NAME_COLUMN = "entityName";
    public static final String JSON_DATA_COLUMN_SUFFIX = "JsonData";

    TableScriptGenerator() {
    }

    /**
     * Generates tables creation script for the given list of entity classes
     * @param entityClasses classes of entities
     * @return string, representing table creation script;
     * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
     */
    public <T extends Identifiable> String generateRelationalTableScript(List<Class<T>> entityClasses) {
        Set<String> tables = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        for (Class entityClass : entityClasses) {
            builder.append(generateRelationalTableScript(entityClass));
            String tableName = Anuta.databaseTools.getEntityTableName(entityClass);
            tables.add(tableName);
        }
        Map<Class<?>, Set<String>> addedColumns = new HashMap<Class<?>, Set<String>>();
        Set<RelationDescriptor> descriptors = new HashSet<RelationDescriptor>();
        Map<String, RelationDescriptor> addedTables = new HashMap<String, RelationDescriptor>();

        for (Class<?> entityClass : entityClasses) {
            List<Field> relationEntityFields = Anuta.reflectionTools.getFieldsAnnotatedWith(entityClass, RelatedEntity.class);

            for (Field field : relationEntityFields) {
                RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
                descriptors.add(relationDescriptor);

                String script = generateAddColumnScript(addedColumns, entityClass, relationDescriptor);
                builder.append(script);

            }

            List<Field> oneToManyColumns = Anuta.reflectionTools.getFieldsAnnotatedWith(entityClass, OneToMany.class);
            for (Field field : oneToManyColumns) {
                RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
                descriptors.add(relationDescriptor);

                String script = generateAddColumnScript(addedColumns, entityClass, relationDescriptor);
                builder.append(script);
            }

            List<Field> manyToManyColumns = Anuta.reflectionTools.getFieldsAnnotatedWith(entityClass, ManyToMany.class);
            for (Field field : manyToManyColumns) {
                RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
                addedTables.put(relationDescriptor.getRelationTable(), relationDescriptor);
            }
        }

        for (String tableName : addedTables.keySet()) {
            if (tables.contains(tableName)) {       //to avoid creation if join table is another entity
                continue;
            }
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
        if (addedColumnsSet.contains(columnName) || Anuta.databaseTools.getFieldForColumnName(columnName, relationHoldingEntity) != null) {
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
        Anuta.databaseTools.validateEntityClass(cls);
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        String tableName = Anuta.databaseTools.getEntityTableName(cls, entityAnnotation);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE %s (", tableName));
        List<Field> columnFields = Anuta.databaseTools.extractFields(cls);

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
        Anuta.databaseTools.validateEntityClass(cls);
        Entity entityAnnotation = cls.getAnnotation(Entity.class);
        String tableName = Anuta.databaseTools.getEntityTableName(cls, entityAnnotation);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE %s (", tableName));
        List<Field> columnFields = Anuta.databaseTools.extractFields(cls);

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
        String tableName = Anuta.databaseTools.getEntityTableName(cls, entityAnnotation);
        return String.format("DROP TABLE %s;", tableName);
    }

    public <T> String buildJsonDataColumnName(Class<T> cls) {
        return cls.getSimpleName() + JSON_DATA_COLUMN_SUFFIX;
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
        String type = Anuta.databaseTools.dispatchType(field).name();
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
}
