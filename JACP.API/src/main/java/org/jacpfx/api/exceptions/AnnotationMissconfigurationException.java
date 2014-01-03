package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 21:09
 * This Exception will be thrown when annotation attributes are missing or used incorrectly.
 */
public class AnnotationMissconfigurationException extends RuntimeException {

    public AnnotationMissconfigurationException() {

    }

    public AnnotationMissconfigurationException(String message) {
        super(message);
    }

    public AnnotationMissconfigurationException(String message, Throwable e) {
        super(message, e);
    }

    public AnnotationMissconfigurationException(Throwable e) {
        super(e);
    }
}