/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
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

import org.jacp.api.dialog.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a managed dialog component, A dialog has a parent component
 * accessible by @Ressource annotation, the Dialog should either extend a Node
 * or define a viewLocation pointing to fxml.
 * 
 * @author Andy Moncsek
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dialog {
	/**
	 * The component id.
	 * 
	 * @return The component Id
	 */
	String id();

	/**
	 * Defines the Scope of the Dialog, default is Singleton.
	 * 
	 * @return The dialog {@link Scope}
	 */
	Scope scope() default Scope.SINGLETON;

	/**
	 * Represents the location (URI) of the declarative UI.
	 * 
	 * @return The view location (like bundle.messages)
	 */
	String viewLocation() default "";

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
