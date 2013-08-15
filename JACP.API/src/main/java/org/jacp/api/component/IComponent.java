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
package org.jacp.api.component;

import org.jacp.api.action.IActionListener;

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
public interface IComponent<L, A, M> extends Comparable<String>{

	/**
	 * Returns an action listener (for local use). Message will be send to
	 * caller component.
	 * @param message ; the initial message to be send by invoking the listener
	 * @return the action listener instance
	 */
    @Deprecated
	IActionListener<L, A, M> getActionListener(final M message);

	/**
	 * Returns an action listener (for global use). targetId defines the id or
	 * your receiver component
	 * @param message ; the message to send to target.
	 * @param targetId ; the targets component id.
	 * @return the action listener instance
	 */
    @Deprecated
	IActionListener<L, A, M> getActionListener(final String targetId, final M message);

	/**
	 * Returns the id of the component.
	 * 
	 * @return the component id
	 */
	String getId();


    /**
     * Set the component id.
     * @param id
     */
    void setId(final String id);


	/**
	 * Get the default active status of component.
	 * 
	 * @return the active state of component
	 */
	boolean isActive();

	/**
	 * Set default active state of component.
	 * 
	 * @param active ; the component active state.
	 */
	void setActive(final boolean active);

	/**
	 * Get if component was activated, can occur if message was send before
	 * "init" message arrived.
	 * 
	 * @return the active status
	 */
	boolean isStarted();

	/**
	 * Returns the name of a component.
	 * 
	 * @return the component name
	 */
	String getName();

    /**
     * Set the component name.
     * @param name
     */
    void setName(final String name);

    /**
     * Represents the Locale ID, see: http://www.oracle.com/technetwork/java/javase/locales-137662.html.
     * @return the locale id
     */
    String getLocaleID();

   /* *//**
     *  Set the Locale ID, see: http://www.oracle.com/technetwork/java/javase/locales-137662.html.
     * @param localeId
     *//*
    void setLocaleID(final String localeId);*/
    /**
     * Represents the location of your resource bundle file.
     * @return the url of resource bundle
     */
    String getResourceBundleLocation();

/*    *//**
     * Set the location of your resource bundle file.
     * @param location
     *//*
    void  setResourceBundleLocation(final String location);*/
}
