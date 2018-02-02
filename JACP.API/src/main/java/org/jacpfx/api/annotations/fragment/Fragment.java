/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [Fragment.java]
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
package org.jacpfx.api.annotations.fragment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jacpfx.api.fragment.Scope;

/**
 * Defines a managed dialog component, A dialog has a parent component
 * accessible by {@code @Ressource} annotation, the Fragment should either
 * extend a Node or define a viewLocation pointing to fxml.
 *
 * @author Andy Moncsek
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fragment {
    /**
     * The component id.
     *
     * @return The component Id.
     */
    String id();

    /**
     * Defines the Scope of the Fragment, default is Singleton.
     *
     * @return The dialog {@link Scope}.
     */
    Scope scope() default Scope.SINGLETON;

    /**
     * Represents the location (URI) of the declarative UI.
     *
     * @return The view location (like {@code bundle.messages}).
     */
    String viewLocation() default "";

    /**
     * Represents the location of your resource bundle file.
     *
     * @return The default resource bundle location (like {@code  bundle.messages}).
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
