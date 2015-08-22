package by.nalivajr.anuta.annonatations.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface AutoActivity {
    /**
     * Specifies the resource layout id
     * @return the id of layout
     */
    public int layoutId();

    /**
     * If true, then all sub views in hierarchy will be checked and initialized too
     */
    public boolean recursive() default false;
}
