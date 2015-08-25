package by.nalivajr.anuta.exceptions;

import by.nalivajr.anuta.annonatations.database.Id;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidEntityIdMappingException extends RuntimeException {
    public <T> InvalidEntityIdMappingException(int ids, Class<T> cls) {
        super(generateMessage(ids, cls));
    }

    private static <T> String generateMessage(int ids, Class<T> cls) {
        if (ids > 1) {
            return String.format("Entity should have only one column field, annotated with %s field. Please check class %s", Id.class.getName(), cls.getName());
        } else {
            return String.format("At least one column property expected to be annotated with %s. Please check class %s", Id.class.getName(), cls.getName());
        }
    }
}
