package com.alice.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class OperationExecutionException extends RuntimeException {

    public OperationExecutionException(Throwable e) {
        super("Unable to execute batch operation", e);
    }
}
