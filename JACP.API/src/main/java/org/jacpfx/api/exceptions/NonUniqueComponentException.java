package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 21:05
 * This Exception will be thrown when component id's are not unique in classpath
 */
public class NonUniqueComponentException extends RuntimeException {

    public NonUniqueComponentException() {

    }

    public NonUniqueComponentException(String message) {
            super(message);
    }

    public NonUniqueComponentException(String message, Throwable e) {
        super(message,e);
    }

    public NonUniqueComponentException(Throwable e) {
        super(e);
    }
}
