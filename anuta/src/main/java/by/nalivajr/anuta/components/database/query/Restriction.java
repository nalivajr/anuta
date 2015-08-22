package by.nalivajr.anuta.components.database.query;

/**
 * Describes the restrictions for the signle property (column) of entity
 */
public interface Restriction {

    /**
     * @return the property, which is restricting
     */
    public String getProperty();

    /**
     * @return the arguments of restriction
     */
    public String[] getArgs();

}
