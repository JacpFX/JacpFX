/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IActionListener.java]
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
package org.jacp.api.action;

/**
 * Handles implementation specific Listener to perform events. This Interface is
 * an abstraction to hide toolkit specific event/action details.
 * 
 * @param <L>
 *            defines the basic listener type
 * @param <M>
 *            defines the type of message ActionEvent
 * @param <A>
 *            defines the type of ActionEvent
 * @author Andy Moncsek
 */
public interface IActionListener<L, A, M> {

	/**
	 * Notify component when action fired.
	 * 
	 * @param action ;  the action fired by components
	 */
	void notifyComponents(final IAction<A, M> action);

	/**
	 * Returns the action.
	 * 
	 * @return an action instance
	 */
	IAction<A, M> getAction();

	/**
	 * Returns implementation specific ActionListener. All listeners should
	 * extend java.util.EventListener.
	 * @param <C> ; the specific listener type 
	 * @return a new listener instance
	 */
	<C extends L> C getListener();

	/**
	 * Abstraction to handle actions/events uniform on different toolkits.
	 * Method invokes toolkit specific handle method perform event call.
	 * 
	 * @param event ; proxy to common event handlers
	 */
	void performAction(A event);
}
