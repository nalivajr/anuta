package by.nalivajr.alice.components.database.query;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */


/**
 * Describes an interface which are used for selection
 */
public interface AliceQuery<T> {
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
}
