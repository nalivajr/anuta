package by.nalivajr.anuta.exceptions;

import by.nalivajr.anuta.components.database.entitymanager.AnutaEntityManager;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotRegisteredEntityClassUsedException extends RuntimeException {
    public NotRegisteredEntityClassUsedException(Class<?> entityClass, Class<? extends AnutaEntityManager> manager) {
        super(String.format("Entity class %s is not registered in %s but attended to be used as entity class.", entityClass.getName(), manager.getName()));
    }
}
