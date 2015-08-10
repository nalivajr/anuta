package com.alice.annonatations.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * Represents the name of column. By default property name will be used.
     * <br/>
     * Be aware using default value as if property will be renamed then database information become inconsistent
     * @return the name of column to store annotated property.
     */
    String value() default "";

    /**
     * Used to describe type of content in
     * @return the strategy of storing data for the property
     */
    DataType dataType() default DataType.AUTO;

    /**
     * Used to create index columns
     * @return true if column should be used as index and false (by default) otherwise
     */
    boolean index() default false;

    public static enum DataType {
        /**
         * Used by default to store enum values by its names
         */
        ENUM_STRING,
        /**
         * Used to store enum values as its ordinal
         */
        ENUM_ORDINAL,

        /**
         * Used by default to store date as long value in millis
         */
        DATE_MILLIS,

        /**
         * Used to store data as timestamp value
         */
        DATE_TIMESTAMP,

        /**
         * Used by default to store objects as JSON string
         */
        JSON_STRING,

        /**
         * Used to store data as byte array of serialized object. Property type should implement {@link java.io.Serializable}
         */
        SERIALIZABLE,

        /**
         * Used to store data as blob.
         */
        BLOB,

        /**
         * Used to store string as blob.
         */
        BLOB_STRING,

        /**
         * Used by default to detect automatically appropriate data type.
         * <ul>
         * <li>
         *     If field type is primitive number or instance of {@link Number}, that it will be stored as is INTEGER or REAL
         * </li>
         * <li>
         *     If field type is {@code boolean} or instance of {@link Boolean}, that it will be stored as {@code true}/{@code false}
         * string
         * </li>
         * <li>
         *     If field type is {@link java.util.Date} that it will be stored as long
         * </li>
         * <li>
         *     If field type is enum type that it will be stored as TEXT (enum constant name)
         * </li>
         * <li>
         *     If field type is byte array that it will be stored as BLOB
         * </li>
         * <li>
         *     If field type is {@link String} that it will be stored as TEXT
         * </li>
         * <li>
         *     If field type is other object type that it will be stored as TEXT using JSON_STRING strategy
         * </li>
         * </ul>
         */
        AUTO
    }
}
