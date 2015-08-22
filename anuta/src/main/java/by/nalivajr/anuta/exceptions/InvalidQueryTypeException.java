package by.nalivajr.anuta.exceptions;

import by.nalivajr.anuta.components.database.query.AnutaQuery;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidQueryTypeException extends RuntimeException {
    public InvalidQueryTypeException(AnutaQuery.QueryType type, String msg) {
        super(String.format("Query type: %s: Message: %s", type, msg));
    }
}
