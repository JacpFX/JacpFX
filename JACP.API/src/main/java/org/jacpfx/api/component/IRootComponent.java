/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IRootComponent.java]
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

import org.jacpfx.api.handler.IComponentHandler;

/**
 * All root components have containing sub components (workspace ->
 * perspectives; perspective - editors) and listeners; all sub components have
 * to be initialized, registered and handled
 * 
 * @author Andy Moncsek
 * 
 * @param <T>
 *            component type to register
 * @param <A>
 *            message type to use in registration process
 */
public interface IRootComponent<T, A> {

	/**
	 * Register the component at the listener.
	 * 
	 * @param component
	 */
	void registerComponent(final T component);

    /**
     * Add a component, this does not fully register the component.
     * If you want to add a newly created component use registerComponent instead.
     * @param component
     */
    void addComponent(final T component);

	/**
	 * Unregister component from current perspective.
	 * 
	 * @param component
	 */
	void unregisterComponent(final T component);


    /**
     * Remove all components when perspective is shut down.
     */
    void removeAllCompnents();

	/**
	 * Handles initialization of subcomponents.
	 * 
	 * @param action
	 */
	void initComponents(final A action);

	/**
	 * Returns component handler to handle initialization and reassignment of
	 * subcomponents.
	 * 
	 * @return the component handler
	 */
	IComponentHandler<T, A> getComponentHandler();

}
