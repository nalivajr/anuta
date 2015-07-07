package com.alice.annonatations.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityCollection {

    /**
     * Indicates if the collection of entities should be persisted or updated automatically
     * @return true (default) if all operation are cascade
     */
    boolean cascadeAll() default true;
}
