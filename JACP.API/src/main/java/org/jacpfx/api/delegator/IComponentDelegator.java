/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [IComponentDelegator.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
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
package org.jacpfx.api.delegator;

import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.coordinator.IDelegator;

import java.util.concurrent.BlockingQueue;

/**
 * A component delegate handles delegate actions.
 * 
 * @author Andy Moncsek
 * 
 * @param <L> the basic listener type
 * @param <A> the basic event type
 * @param <M> the basic message type
 */
public interface IComponentDelegator<L, A, M> extends IDelegator<L, A, M> {

	/**
	 * Handles delegate of a component. Puts the component to delegate queue.
	 * 
	 * @param component, The component to delegate.
	 */
	void delegateComponent(final ISubComponent<L, A, M> component);

	/**
	 * Get the delegate queue to add components to be delegated.
	 * 
	 * @return delegateQueue, The delegate queue.
	 */
	BlockingQueue<ISubComponent<L, A, M>> getComponentDelegateQueue();

}
