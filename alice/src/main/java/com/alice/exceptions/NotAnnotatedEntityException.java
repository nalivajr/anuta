package com.alice.exceptions;

import com.alice.annonatations.db.Entity;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotAnnotatedEntityException extends RuntimeException {
    public NotAnnotatedEntityException() {
        super(String.format("Target class should be annotated with %s to be used as entity",  Entity.class.getName()));
    }
}
