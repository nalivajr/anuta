package by.nalivajr.alice.exceptions;

import java.lang.reflect.Field;

import by.nalivajr.alice.annonatations.database.Entity;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotEntityClassUsedInRelation extends RuntimeException {
    public NotEntityClassUsedInRelation(Class<?> entityClass, Field field) {
        super(String.format("The type of field %s in class %s is not entity type. Check it's annotated with %s",
                field.getName(), entityClass.getName(), Entity.class.getName()));
    }
}
