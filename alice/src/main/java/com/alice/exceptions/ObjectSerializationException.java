package com.alice.exceptions;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class ObjectSerializationException extends InvalidDataTypeException {

    public ObjectSerializationException(Field field) {
        super(String.format("Unable to serialize value of property %s with data type %s in class %s",
                field.getName(), getDataType(field), field.getDeclaringClass().getName()));
    }
}
