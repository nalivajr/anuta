package com.alice.components.database.models;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.alice.annonatations.database.Column;
import com.alice.annonatations.database.Entity;
import com.alice.annonatations.database.Id;
import com.alice.exceptions.NotAnnotatedEntityException;
import com.alice.tools.Alice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<Field, ColumnDescriptor> fieldsToDescriptorMap;

    public EntityDescriptor(Class<?> entityClass) {
        Alice.databaseTools.validateEntityClass(entityClass);
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
        tableName = entityAnno.tableName();
        if (tableName.isEmpty()) {
            tableName = entityClass.getSimpleName();
        }
    }

    private void initEntityName(Entity entityAnno) {
        entityName = entityAnno.name();
        if (entityName.isEmpty()) {
            entityName = entityClass.getName();
        }
    }

    private void initAuthorityAndUri(Entity entityAnno) {
        authority = entityAnno.authority();

        tableUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(tableName)
                .build();
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
        fields = new ArrayList<>(Alice.databaseTools.extractFields(entityClass));
        indexFields = new ArrayList<>(fields.size());
        fieldsToDescriptorMap = new HashMap<>(fields.size());
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
        Set<String> result = new HashSet<>(fields.size());
        for (Field field : fields) {
           result.add(fieldsToDescriptorMap.get(field).getColumnName());
        }
        return result;
    }

    public Collection<ColumnDescriptor> getFieldDescriptors() {
        return fieldsToDescriptorMap.values();
    }
}
