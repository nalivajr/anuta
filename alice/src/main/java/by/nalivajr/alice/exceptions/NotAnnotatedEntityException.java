package by.nalivajr.alice.exceptions;

import by.nalivajr.alice.annonatations.database.Entity;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotAnnotatedEntityException extends RuntimeException {
    public NotAnnotatedEntityException(Class<?> cls) {
        super(String.format("Target class %s should be annotated with %s to be used as entity", cls.getName(), Entity.class.getName()));
    }
}
