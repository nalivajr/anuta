package com.alice.exceptions;

import com.alice.annonatations.db.Column;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class ObjectDeserializationException extends RuntimeException {
    public ObjectDeserializationException(Column.DataType dataType, Field field) {
        super(String.format("Unable to deserialize value of property %s with data type %s in class %s",
                field.getName(), dataType.name(), field.getDeclaringClass().getName()));
    }
}
