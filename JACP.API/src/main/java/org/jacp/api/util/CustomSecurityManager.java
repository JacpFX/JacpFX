package org.jacp.api.util;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 18.07.13
 * Time: 15:34
 * A custom security manager that exposes the getClassContext() information
 */
public class CustomSecurityManager extends SecurityManager {
    private final static int CALL_STACK_DEPTH=2;
    public String getCallerClassName() {
        return getClassContext()[CALL_STACK_DEPTH].getName();
    }

}
