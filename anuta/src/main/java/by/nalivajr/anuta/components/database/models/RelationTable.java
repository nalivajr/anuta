package by.nalivajr.anuta.components.database.models;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class RelationTable {
    private String name;
    private String entity1RelationColumnName;
    private String entity2RelationColumnName;
    private Class<?> entity1;
    private Class<?> entity2;

    public RelationTable(Field entity1Field, Class<?> entity2) {
        this.entity1 = entity1Field.getDeclaringClass();
        this.entity2 = entity2;
    }

    public String getName() {
        return name;
    }

    public String getColumnNameForEntity(Class<?> entity) {
        if (entity == entity1) {
            return entity1RelationColumnName;
        }
        if (entity == entity2) {
            return entity2RelationColumnName;
        }
        throw new IllegalArgumentException(String.format("This instance of %s is not for entity class %s", RelationTable.class.getName(), entity.getName()));
    }

}
