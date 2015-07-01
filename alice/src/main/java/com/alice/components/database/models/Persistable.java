package com.alice.components.database.models;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface Persistable<I> extends Identifiable<I> {

    /**
     * @return _id value of entity
     */
    public Integer getRowId();

    /**
     * Sets _id for entity
     * @param id the _id value
     */
    public void setRowId(Integer id);

    /**
     * Provides the name of identifier column. It can be either {@value android.provider.BaseColumns#_ID} or
     * any other column which identifies entity
     * @return the name of identifier column
     */
    public String getIdColumnName();
}
