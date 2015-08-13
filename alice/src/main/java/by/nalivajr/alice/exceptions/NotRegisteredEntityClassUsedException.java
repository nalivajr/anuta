package by.nalivajr.alice.exceptions;

import by.nalivajr.alice.components.database.entitymanager.AliceEntityManager;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotRegisteredEntityClassUsedException extends RuntimeException {
    public NotRegisteredEntityClassUsedException(Class<?> entityClass, Class<? extends AliceEntityManager> manager) {
        super(String.format("Entity class %s is not registered in %s but attended to be used as entity class.", entityClass.getName(), manager.getName()));
    }
}
