package by.nalivajr.alice.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class DifferentDataTypesInRelationMappingException extends RuntimeException {

    public DifferentDataTypesInRelationMappingException(Class<?> parent, String parentColumn, Class<?> child, String childColumn) {
        super(String.format("Could not build relation for field %s in class %s and field %s in class %s. Columns have different SLQ type",
                parent.getName(), parentColumn, child.getName(), childColumn));
    }
}
