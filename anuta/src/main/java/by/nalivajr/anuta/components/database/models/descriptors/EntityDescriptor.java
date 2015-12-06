package by.nalivajr.anuta.components.database.models.descriptors;

import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import by.nalivajr.anuta.components.database.models.enums.SqliteDataType;
import by.nalivajr.anuta.exceptions.NotAnnotatedEntityException;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class EntityDescriptor {

    private Class<?> entityClass;
    private String entityName;
    private String tableName;
    private String authority;
    private String idColumnName;
    private Uri tableUri;

    private Field rowIdField;
    private Field idField;

    private List<Field> fields;
    private List<Field> indexFields;
    private List<Field> entityRelatedFields;
    private List<Field> oneToManyFields;
    private List<Field> manyToManyFields;

    private Map<Field, ColumnDescriptor> fieldsToDescriptorMap;
    private Map<String, Field> columnNameToField;
    private Map<Field, RelationDescriptor> fieldsToRelationDescriptorMap;
    private Map<Field, RelationQueryDescriptor> fieldsToRelationQueryDescriptorMap;

    public EntityDescriptor(Class<?> entityClass) {
        Anuta.databaseTools.validateEntityClass(entityClass);
        this.entityClass = entityClass;
        initData();
    }

    private void initData() {
        Entity entityAnno = entityClass.getAnnotation(Entity.class);
        if (entityAnno == null) {
            throw new NotAnnotatedEntityException(entityClass);
        }
        initTableName(entityAnno);
        initEntityName(entityAnno);
        initAuthorityAndUri(entityAnno);
        initMaps();
        initIdColumnName();
        initRowIdField();
    }

    private void initRowIdField() {
        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            if (fieldType != Long.class && fieldType != Long.TYPE) {
                continue;
            }
            if (field.getAnnotation(Id.class) != null && field.getName().equals(BaseColumns._ID)) {
                rowIdField = field;
                break;
            }
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno == null) {
                continue;
            }
            String name = columnAnno.value().isEmpty() ? field.getName() : columnAnno.value();
            if (name.equals(BaseColumns._ID)) {
                rowIdField = field;
                break;
            }
        }
    }

    private void initTableName(Entity entityAnno) {
        tableName = Anuta.databaseTools.getEntityTableName(entityClass, entityAnno);
    }

    private void initEntityName(Entity entityAnno) {
        entityName = entityAnno.name();
        if (entityName.isEmpty()) {
            entityName = entityClass.getName();
        }
    }

    private void initAuthorityAndUri(Entity entityAnno) {
        authority = entityAnno.authority();

        tableUri = Anuta.databaseTools.buildUriForTableName(tableName, authority);
    }

    private void initIdColumnName() {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) == null) {
                continue;
            }
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno == null || columnAnno.value().isEmpty()) {
                idColumnName = field.getName();
            } else {
                idColumnName = columnAnno.value();
            }
            idField = field;
            break;
        }
    }

    private void initMaps() {
        initRelationsMap();

        fields = new ArrayList<Field>(Anuta.databaseTools.extractFields(entityClass));
        indexFields = new ArrayList<Field>(fields.size());

        fieldsToDescriptorMap = new HashMap<Field, ColumnDescriptor>(fields.size());
        for (Field field : fields) {
            ColumnDescriptor columnDescriptor = new ColumnDescriptor(field);
            fieldsToDescriptorMap.put(field, columnDescriptor);
            if (columnDescriptor.isIndexed()) {
                indexFields.add(field);
            }
        }
        fields = Collections.unmodifiableList(fields);
        indexFields = Collections.unmodifiableList(indexFields);
        fieldsToDescriptorMap = Collections.unmodifiableMap(fieldsToDescriptorMap);
        columnNameToField = new HashMap<>();
        for (Field field : fieldsToDescriptorMap.keySet()) {
            ColumnDescriptor columnDescriptor = fieldsToDescriptorMap.get(field);
            columnNameToField.put(columnDescriptor.getColumnName(), field);
        }
    }

    private void initRelationsMap() {
        entityRelatedFields = Collections.unmodifiableList(Anuta.reflectionTools.getFieldsAnnotatedWith(entityClass, RelatedEntity.class));
        oneToManyFields = Collections.unmodifiableList(Anuta.reflectionTools.getFieldsAnnotatedWith(entityClass, OneToMany.class));
        manyToManyFields = Collections.unmodifiableList(Anuta.reflectionTools.getFieldsAnnotatedWith(entityClass, ManyToMany.class));

        fieldsToRelationDescriptorMap = new HashMap<Field, RelationDescriptor>();
        fieldsToRelationQueryDescriptorMap = new HashMap<Field, RelationQueryDescriptor>();

        buildDescriptiors(entityRelatedFields);
        buildDescriptiors(oneToManyFields);
        buildDescriptiors(manyToManyFields);

        fieldsToRelationDescriptorMap = Collections.unmodifiableMap(fieldsToRelationDescriptorMap);
        fieldsToRelationQueryDescriptorMap = Collections.unmodifiableMap(fieldsToRelationQueryDescriptorMap);
    }

    private void buildDescriptiors(List<Field> fields) {
        for (Field field : fields) {
            RelationDescriptor relationDescriptor = new RelationDescriptor(entityClass, field);
            fieldsToRelationDescriptorMap.put(field, relationDescriptor);
            RelationQueryDescriptor queryDescriptor = new RelationQueryDescriptor(relationDescriptor.getRelatedEntity(), relationDescriptor);
            fieldsToRelationQueryDescriptorMap.put(field, queryDescriptor);
        }
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getFieldKey(Field field) {
        return fieldsToDescriptorMap.get(field).getColumnName();
    }

    public SqliteDataType getFieldSqltype(Field field) {
        return fieldsToDescriptorMap.get(field).getSqlLiteDataType();
    }

    public Uri getTableUri() {
        return tableUri;
    }

    public String getAuthority() {
        return authority;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public Field getRowIdField() {
        return rowIdField;
    }

    public Field getIdField() {
        return idField;
    }

    public ColumnDescriptor getFieldDescriptor(Field field) {
        return fieldsToDescriptorMap.get(field);
    }

    public List<Field> getIndexFields() {
        return indexFields;
    }

    public Set<String> getFieldKeys() {
        Set<String> result = new HashSet<String>(fields.size());
        for (Field field : fields) {
           result.add(fieldsToDescriptorMap.get(field).getColumnName());
        }
        return result;
    }

    public Collection<ColumnDescriptor> getFieldDescriptors() {
        return fieldsToDescriptorMap.values();
    }

    public List<Field> getEntityRelatedFields() {
        return entityRelatedFields;
    }

    public List<Field> getOneToManyFields() {
        return oneToManyFields;
    }

    public List<Field> getManyToManyFields() {
        return manyToManyFields;
    }

    public RelationDescriptor getRelationDescriptorForField(Field field) {
        return fieldsToRelationDescriptorMap.get(field);
    }

    public Set<RelationDescriptor> getRelationDescriptors() {
        return new HashSet<RelationDescriptor>(fieldsToRelationDescriptorMap.values());
    }

    public RelationQueryDescriptor getRelationQueryDescriptorForField(Field field) {
        return fieldsToRelationQueryDescriptorMap.get(field);
    }

    public Field getFieldForColumn(String columnName) {
        return columnNameToField.get(columnName);
    }


}
