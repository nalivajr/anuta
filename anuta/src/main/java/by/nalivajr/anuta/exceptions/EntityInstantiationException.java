package by.nalivajr.anuta.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class EntityInstantiationException extends RuntimeException {
    public EntityInstantiationException(Class entityClass, Throwable e) {
        super(String.format("Could not instantiate entity of type %s. Please provide default public constructor for non abstract class", entityClass.getName()), e);
    }
}
