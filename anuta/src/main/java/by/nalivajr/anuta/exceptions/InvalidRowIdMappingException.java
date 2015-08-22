package by.nalivajr.anuta.exceptions;

import android.provider.BaseColumns;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidRowIdMappingException extends RuntimeException {
    public InvalidRowIdMappingException(Class<?> entity) {
        super(String.format("The field with key %s should have long or %s type. Check class %s", BaseColumns._ID, Long.class.getName(), entity.getClass().getName()));
    }
}
