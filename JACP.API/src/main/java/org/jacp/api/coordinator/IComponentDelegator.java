/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IComponentDelegator.java]
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
package org.jacp.api.coordinator;

import org.jacp.api.component.ISubComponent;

import java.util.concurrent.BlockingQueue;

/**
 * A component delegate handles delegate actions.
 * 
 * @author Andy Moncsek
 * 
 * @param <L>
 * @param <A>
 * @param <M>
 */
public interface IComponentDelegator<L, A, M> extends IDelegator<L, A, M> {

	/**
	 * Handles delegate of a component.
	 * 
	 * @param component
	 */
	void delegateComponent(ISubComponent<L, A, M> component);

	/**
	 * Get the delegate queue to add components to be delegated.
	 * 
	 * @return delegateQueue
	 */
	BlockingQueue<ISubComponent<L, A, M>> getComponentDelegateQueue();

}
