package org.jacp.api.annotations.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 29.08.13
 * Time: 08:35
 * Defines a component that returns a view node to be displayed in a perspective target
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface View {
    /**
     * Defines the target layout id where the component should be displayed in.
     * This id is defined in the parent perspective and can be changed at runtime (context.setLayoutTargetId).
     * @return  A valid targetLayout id from perspective.
     */
    String initialTargetLayoutId();
}
