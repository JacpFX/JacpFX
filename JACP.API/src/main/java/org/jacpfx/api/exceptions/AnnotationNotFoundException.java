package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 21:07
 * This Exception will be thrown when essential annotations are missing.
 */
public class AnnotationNotFoundException extends RuntimeException {

    public AnnotationNotFoundException() {

    }

    public AnnotationNotFoundException(String message) {
        super(message);
    }

    public AnnotationNotFoundException(String message,Throwable e) {
        super(message,e);
    }

    public AnnotationNotFoundException(Throwable e) {
        super(e);
    }
}