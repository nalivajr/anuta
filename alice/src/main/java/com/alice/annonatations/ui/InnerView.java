package com.alice.annonatations.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface InnerView {

    /**
     * @return the id of the view to be found in root view
     */
    public int value();
}