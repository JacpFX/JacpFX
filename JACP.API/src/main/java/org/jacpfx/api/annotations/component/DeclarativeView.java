
/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [DeclarativeView.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx.api.annotations.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jacpfx.api.context.JacpContext;

/**
 * Marks component as declarative component.
 * <P>
 * Created with IntelliJ IDEA.<BR>
 * User: Andy Moncsek<BR>
 * Date: 15.08.13<BR>
 * Time: 21:46
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeclarativeView {

    /**
     * The component id.
     *
     * @return The component Id.
     */
    String id();

    /**
     * The active state at start time.
     *
     * @return {@code true}.
     */
    boolean active() default true;

    /**
     * Represents the location (URI) of the declarative UI.
     *
     * @return The view location.
     */
    String viewLocation();

    /**
     * Defines the target layout id where the component should be displayed in.
     * This id is defined in the parent perspective and can be changed at runtime 
     * ({@link JacpContext#setTargetLayout(java.lang.String)}).
     *
     * @return A valid targetLayout id from perspective.
     */
    String initialTargetLayoutId();

    /**
     * Represents the location of your resource bundle file.
     *
     * @return The default resource bundle location (like {@code bundle.messages}).
     */
    String resourceBundleLocation() default "";

    /**
     * Represents the Locale ID.
     *
     * @return The default locale Id.
     * @see <a href="http://www.oracle.com/technetwork/java/javase/locales-137662.html">JDK 6 and JRE 6 Supported Locales</a>
     */
    String localeID() default "";
}
