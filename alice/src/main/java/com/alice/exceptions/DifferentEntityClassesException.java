package com.alice.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class DifferentEntityClassesException extends RuntimeException {

    public DifferentEntityClassesException() {
        super("Different classes are used for batch operation");
    }
}
