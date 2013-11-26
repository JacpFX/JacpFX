/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IComponent.java]
 * AHCP Project (http://jacp.googlecode.com)
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
package org.jacpfx.api.component;

import org.jacpfx.api.context.Context;

/**
 * This Interface represents a very basic component that can exists in JACP
 * environment.
 * 
 * @author Andy Moncsek
 * 
 * @param <L>
 *            defines the action listener type
 * @param <A>
 *            defines the basic action type
 * @param <M>
 *            defines the basic message type
 */
public interface IComponent<L, A, M> extends Comparable<IComponent<L, A, M>>{




	/**
	 * Get if component was activated, can occur if message was send before
	 * "init" message arrived.
	 * 
	 * @return the active status
	 */
	boolean isStarted();

    /**
     * Set once when component was first started
     * @param started
     */
    void setStarted(final boolean started);

    /**
     * Represents the Locale ID, see: http://www.oracle.com/technetwork/java/javase/locales-137662.html.
     * @return the locale id
     */
    String getLocaleID();

   /**
     *  Set the Locale ID, see: http://www.oracle.com/technetwork/java/javase/locales-137662.html.
     * @param localeId
     */
    void setLocaleID(final String localeId);

    /**
     * Represents the location of your resource bundle file.
     * @return the url of resource bundle
     */
    String getResourceBundleLocation();

    /**
     * Set the location of your resource bundle file.
     * @param location
     */
    void  setResourceBundleLocation(final String location);

    /**
     * Returns the components context object.
     * @return the context object.
     */
    Context<L, A, M> getContext();
}
