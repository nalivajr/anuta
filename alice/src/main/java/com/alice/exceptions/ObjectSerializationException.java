package com.alice.exceptions;

import com.alice.annonatations.db.Column;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class ObjectSerializationException extends RuntimeException {
    public ObjectSerializationException(Column.DataType dataType, Field field) {
        super(String.format("Unable to serialize value of property %s with data type %s in class %s",
                field.getName(), dataType.name(), field.getDeclaringClass().getName()));
    }
}
