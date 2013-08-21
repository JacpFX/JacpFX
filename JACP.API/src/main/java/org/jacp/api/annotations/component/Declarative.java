package org.jacp.api.annotations.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 15.08.13
 * Time: 21:46
 * Marks component as declarative component.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Declarative {
    /**
     * Represents the location (URI) of the declarative UI.
     * @return The view location.
     */
    String viewLocation();
}
