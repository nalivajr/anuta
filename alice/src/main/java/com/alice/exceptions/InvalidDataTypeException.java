package com.alice.exceptions;

import com.alice.annonatations.db.Column;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidDataTypeException extends RuntimeException {
    public InvalidDataTypeException(Column.DataType dataType, Field field) {
        super(String.format("Attempt to use %s strategy for property %s which is not instance of %s in class %s",
                dataType.name(), field.getName(), Serializable.class.getName(), field.getDeclaringClass().getName()));
    }
}
