package by.nalivajr.anuta.exceptions;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class ObjectDeserializationException extends InvalidDataTypeException {
    public ObjectDeserializationException(Field field) {
        super(String.format("Unable to deserialize value of property %s with data type %s in class %s",
                field.getName(), getDataType(field), field.getDeclaringClass().getName()));
    }
}
