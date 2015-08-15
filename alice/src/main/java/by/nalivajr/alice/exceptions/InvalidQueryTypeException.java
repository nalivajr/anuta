package by.nalivajr.alice.exceptions;

import by.nalivajr.alice.components.database.query.AliceQuery;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidQueryTypeException extends RuntimeException {
    public InvalidQueryTypeException(AliceQuery.QueryType type, String msg) {
        super(String.format("Query type: %s: Message: %s", type, msg));
    }
}
