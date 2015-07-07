package com.alice.exceptions;

import com.alice.annonatations.database.Column;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotAnnotatedFieldException extends RuntimeException {
    public NotAnnotatedFieldException(Field field) {
        super(String.format("Target entity's %s property %s should be annotated with %s to be stored into database",
                field.getDeclaringClass().getName(), field.getName(), Column.class.getName()));
    }
}
