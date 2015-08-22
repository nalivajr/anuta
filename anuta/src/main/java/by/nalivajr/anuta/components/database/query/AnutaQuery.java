package by.nalivajr.anuta.components.database.query;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */


import android.content.ContentValues;

/**
 * Describes an interface which are used for selection
 */
public interface AnutaQuery<T> {
    /**
     * @return the selection query which describes clauses for objects to be retrieved
     */
    public String getSelection();

    /**
     * @return arguments, which are used in query
     */
    public String[] getSelectionArgs();

    /**
     * @return the class of entity to be retrieved
     */
    public Class<T> getTargetClass();

    /**
     * @return the type of query
     */
    public QueryType getType();

    /**
     * @return content values, specified for query
     */
    public ContentValues getContentValues();

    public String getLimit();

    public static enum QueryType {
        SELECT,
        INSERT,
        UPDATE,
        DELETE
    }
}
