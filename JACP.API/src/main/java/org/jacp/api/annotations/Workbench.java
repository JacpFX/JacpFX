package org.jacp.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 16.08.13
 * Time: 14:18
 * Defines a workbench class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Workbench {
    /**
     * The components name.
     *
     * @return The component name
     */
    String name();

    /**
     * Define all perspective id's which belongs to workbench..
     * @return all related component ids
     */
    String[] perspectives();
}
