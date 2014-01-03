package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 20.11.13
 * Time: 21:02
 * This Exception will be thrown when a component in intermediate state like "shutdown" should be executed. This can happen on messages to components that are in shutdown process.
 */
public class InvalidComponentMatch extends RuntimeException {

    public InvalidComponentMatch() {

    }

    public InvalidComponentMatch(String message) {
        super(message);
    }

    public InvalidComponentMatch(String message, Throwable e) {
        super(message, e);
    }

    public InvalidComponentMatch(Throwable e) {
        super(e);
    }
}
