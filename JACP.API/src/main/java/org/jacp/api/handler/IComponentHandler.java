/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IComponentHandler.java]
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
package org.jacp.api.handler;

/**
 * A component handler handles initialization and reassignment of components
 * 
 * @author Andy Moncsek
 * 
 * @param <T>
 *            component type to register
 * @param <A>
 *            action type to use in registration process
 */
public interface IComponentHandler<T, A> {
	/**
	 * Handles initialization of a single component.
	 * 
	 * @param action
	 * @param component
	 */
	void initComponent(final A action, final T component);

	/**
	 * Runs 'handle' method and replace of subcomponent in perspective.
	 * 
	 * @param component
	 * @param action
	 */
	void handleAndReplaceComponent(final A action, final T component);
}
