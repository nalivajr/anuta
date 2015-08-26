package by.nalivajr.anuta.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotInitializedException extends RuntimeException {

    public NotInitializedException() {
        super("Collection is not initialized. Please call AnutaEntityManager.initialize for to load collection");
    }
}
