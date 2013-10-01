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
public @interface DeclarativeView {
    /**
     * The components name.
     *
     * @return The component name
     */
    String name();

    /**
     * The component id.
     *
     * @return The component Id
     */
    String id();

    /**
     * The active state at start time.
     *
     * @return True
     */
    boolean active() default true;
    /**
     * Represents the location (URI) of the declarative UI.
     * @return The view location.
     */
    String viewLocation();

    /**
     * Defines the target layout id where the component should be displayed in.
     * This id is defined in the parent perspective and can be changed at runtime (context.setLayoutTargetId).
     * @return  A valid targetLayout id from perspective.
     */
    String initialTargetLayoutId();

    /**
     * Represents the location of your resource bundle file.
     *
     * @return The default resource bundle location (like bundle.messages)
     */
    String resourceBundleLocation() default "";

    /**
     * Represents the Locale ID. see:
     * http://www.oracle.com/technetwork/java/javase/locales-137662.html
     *
     * @return The default locale Id
     */
    String localeID() default "";
}
