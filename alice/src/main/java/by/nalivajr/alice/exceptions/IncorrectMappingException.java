package by.nalivajr.alice.exceptions;

import java.lang.reflect.Field;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class IncorrectMappingException extends RuntimeException {
    public IncorrectMappingException(Field field) {
        super(String.format("Column names are not unique. Please check whether all columns have different name including parent objects columns if inheritance mode is enabled. Property: %s in class %s", field.getName(), field.getDeclaringClass().getName()));
    }
}
