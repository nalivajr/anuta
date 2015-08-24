package by.nalivajr.anuta.exceptions;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class InvalidMimeCodeException extends RuntimeException {

    public InvalidMimeCodeException(int code) {
        super(String.format("The MIME type code %d is already used. Please check MIME codes for your entities", code));
    }
}
