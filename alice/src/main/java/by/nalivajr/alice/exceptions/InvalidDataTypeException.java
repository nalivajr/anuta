package by.nalivajr.alice.exceptions;

import by.nalivajr.alice.annonatations.database.Column;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidDataTypeException extends RuntimeException {

    protected InvalidDataTypeException(String msg) {
        super(msg);
    }

    public InvalidDataTypeException(Field field) {
        super(String.format("Attempt to use %s strategy for property %s which is not instance of %s in class %s",
                getDataType(field), field.getName(), Serializable.class.getName(), field.getDeclaringClass().getName()));
    }

    protected static String getDataType(Field field) {
        Column anno = field.getAnnotation(Column.class);
        Column.DataType dataType = anno.dataType();
        return dataType.name();
    }
}
