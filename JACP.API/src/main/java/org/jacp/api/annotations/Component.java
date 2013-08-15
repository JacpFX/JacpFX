/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [Component.java]
 * AHCP Project (http://jacp.googlecode.com/)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacp.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines the meta definition of an JACP UI component.
 * 
 * @author Andy Moncsek
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
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
     * The execution target at start time.
     * 
     * @return The default execution target defined by perspective
     */
    String defaultExecutionTarget() default "";

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
