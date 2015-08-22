package by.nalivajr.anuta.components.database.models;

import by.nalivajr.anuta.tools.Anuta;
import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Id;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class ColumnDescriptor {

    private Field field;
    private String columnName;
    private boolean indexed;
    private SqliteDataType sqlLiteDataType;
    private Column.DataType columnPersistingDataTypeStrategy;
    private boolean isIdColumn;

    public ColumnDescriptor(Field field) {
        this.field = field;
        initData();
    }

    private void initData() {
        Column columnAnno = field.getAnnotation(Column.class);
        Id idAnno = field.getAnnotation(Id.class);

        if (columnAnno == null && idAnno != null) {
            columnName = field.getName();
        }
        if (columnAnno != null) {
            columnName = columnAnno.value();
            if (columnName.isEmpty()) {
                columnName = field.getName();
            }
            indexed = columnAnno.index() || idAnno != null;
            columnPersistingDataTypeStrategy = columnAnno.dataType();
        }
        isIdColumn = idAnno != null;
        sqlLiteDataType = Anuta.databaseTools.dispatchType(field);
    }

    public Field getField() {
        return field;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public SqliteDataType getSqlLiteDataType() {
        return sqlLiteDataType;
    }

    public Column.DataType getColumnPersistingDataTypeStrategy() {
        return columnPersistingDataTypeStrategy;
    }

    public boolean isIdColumn() {
        return isIdColumn;
    }
}
