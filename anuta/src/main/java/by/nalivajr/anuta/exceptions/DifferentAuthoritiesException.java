package by.nalivajr.anuta.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class DifferentAuthoritiesException extends RuntimeException {

    public DifferentAuthoritiesException() {
        super("Entity classes have different authorities");
    }
}
