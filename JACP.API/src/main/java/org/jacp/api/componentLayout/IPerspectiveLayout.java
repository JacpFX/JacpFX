/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IPerspectiveLayout.java]
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
package org.jacp.api.componentLayout;

import java.util.Map;

/**
 * Defines layout of a perspective and the container for included editors and
 * views (target components); for use in perspectives handle method.
 * 
 * @param <M>
 *            type of root component
 * @param <B>
 *            type of target components
 * @author Andy Moncsek
 */
public interface IPerspectiveLayout<M, B> {

	/**
	 * Set Layout-Wrapper for perspective; this wrapper contains wrappers for
	 * editors and views, define a valid component which is valid to hold
	 * subcomponents.
	 * 
	 * @param comp
	 *            the new root component
	 */
	void registerRootComponent(final M comp);

	/**
	 * Get the ' layoutwrapper' for perspective; a layout component is a
	 * component which can contain UI subcomponents.
	 * 
	 * @return the toolkit root component where all other UI components are
	 *         included
	 */
	M getRootComponent();

	/**
	 * Returns map of target components and ids key - id value - target
	 * component.
	 * 
	 * @return a map with all target UI components
	 */
	Map<String, B> getTargetLayoutComponents();

	/**
	 * Register a target component; a target component defines a wrapper where
	 * editors and views can "live" in; you can define a target for each editor
	 * or view component; create an root component, a complex layout an register
	 * all components where editors/views should displayed in.
	 * 
	 * @param id
	 *            the id
	 * @param target
	 *            the target
	 */
	void registerTargetLayoutComponent(final String id, final B target);

}
