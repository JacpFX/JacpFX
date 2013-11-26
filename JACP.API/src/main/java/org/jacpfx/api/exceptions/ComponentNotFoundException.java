package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 21:05
 * This Exception will be thrown when no workbench/perspective/component was found
 */
public class ComponentNotFoundException extends RuntimeException {

    public ComponentNotFoundException() {

    }

    public ComponentNotFoundException(String message) {
            super(message);
    }

    public ComponentNotFoundException(String message,Throwable e) {
        super(message,e);
    }

    public ComponentNotFoundException(Throwable e) {
        super(e);
    }
}
