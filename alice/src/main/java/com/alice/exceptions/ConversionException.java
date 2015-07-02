package com.alice.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class ConversionException extends RuntimeException {
    public ConversionException(Object val, Class targetType) {
        super(String.format("Could not convert %s value of type %s", val.toString(), targetType.getName()));
    }
}
