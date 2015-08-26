package by.nalivajr.anuta.annonatations.database;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public enum FetchType {

    /**
     * Sets, that related collection or entity should not be loaded when parent entity is loading.
     */
    LAZY,

    /**
     * Sets that related collection or entity should be initialized when parent entity is loading
     */
    EAGER
}
