package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 10.09.13
 * Time: 22:19
 * This Exception will be thrown when required attributes on Annotations are missing.
 */
public class AttributeNotFoundException extends RuntimeException {

    public AttributeNotFoundException() {

    }

    public AttributeNotFoundException(String message) {
        super(message);
    }

    public AttributeNotFoundException(String message,Throwable e) {
        super(message,e);
    }

    public AttributeNotFoundException(Throwable e) {
        super(e);
    }
}
