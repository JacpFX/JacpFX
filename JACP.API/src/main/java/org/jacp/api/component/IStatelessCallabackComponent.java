/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IStateLessCallabackComponent.java]
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a state less background/callback component. This component has a
 * typical handle method, the return value is typically a non UI value (but it
 * can be). Every message to this component should be handled in a separate
 * thread and component instance, managed by an executor (to avoid garbage). Do
 * not use private members as it is not guaranteed that that you contact the
 * same instance twice. This component type is good for scaling tasks like
 * performing operations on many folders or tables in database. The return value
 * will be send to message caller or to specified handleTargetId.
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
public interface IStatelessCallabackComponent<L, A, M> extends
        ISubComponent<L, A, M> {

	/**
	 * return instances of current state less component
	 * 
	 * @return an new callback instance
	 */
	List<ISubComponent<L, A, M>> getInstances();

	/**
	 * returns thread counter to coordinate amount of existing instances of
	 * sateless callback component
	 * 
	 * @return the counter
	 */
	AtomicInteger getThreadCounter();

	/**
	 * returns associated executor service
	 * 
	 * @return the execution service
	 */
	ExecutorService getExecutorService();
}
