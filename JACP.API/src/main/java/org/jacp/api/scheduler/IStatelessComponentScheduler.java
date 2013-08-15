/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IStatelessComponentScheduler.java]
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
package org.jacp.api.scheduler;

import org.jacp.api.action.IAction;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.IStatelessCallabackComponent;

/**
 * Handles instances of a state less component; delegates message to a non
 * blocked component instance or if all components are blocked message is
 * delegated to queue in one of existing instances
 * 
 * @author Andy Moncsek
 * 
 * @param <L> The listener type.
 * @param <A> The action type.
 * @param <M> The Message type.
 */
public interface IStatelessComponentScheduler<L, A, M> {
	/**
	 * Handles incoming message to managed state less component.
	 * 
	 * @param message
	 */
	void incomingMessage(final IAction<A, M> message,
			IStatelessCallabackComponent<L, A, M> component);

	/**
	 * Returns a new instance of managed state less component.
	 * 
	 * @param <T>
	 * @param clazz
	 * @return an cloned instance of a state less component.
	 */
	<T extends IStatelessCallabackComponent<L, A, M>, H extends IComponentHandle> IStatelessCallabackComponent<L, A, M> getCloneBean(
			IStatelessCallabackComponent<L, A, M> component,
			final Class<H> clazz);

}