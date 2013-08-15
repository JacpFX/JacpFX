/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IHandleable.java]
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

import org.jacp.api.action.IAction;

/**
 * This interface defines components which are able to handle a single message.
 * 
 * @author Andy Moncsek
 * @param <A>
 *            defines the basic action type
 * @param <M>
 *            defines the basic message type
 */
public interface IHandleable<A, M> {
	/**
	 * Handles component when called. The handle method in subcomponents is
	 * always executed in a separate thread;
	 * 
	 * @param action , the triggering action
	 * @param <C> the node return type
	 * @return view component
	 */
	<C> C handle(final IAction<A, M> action);
}
