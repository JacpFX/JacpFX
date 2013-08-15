/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IComponentCoordinator.java]
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

import org.jacp.api.action.IDelegateDTO;

import java.util.concurrent.BlockingQueue;

/**
 * Notifies the components included in perspective.
 * 
 * @param <L>
 *            defines the action listener type
 * @param <A>
 *            defines the basic action type
 * @param <M>
 *            defines the basic message type
 * 
 * @author Andy Moncsek
 */
public interface IComponentCoordinator<L, A, M> extends ICoordinator<L, A, M> {

	/**
	 * set delegate queue
	 * 
	 * @param delegateQueue
	 */
	void setMessageDelegateQueue(
			final BlockingQueue<IDelegateDTO<A, M>> delegateQueue);

	/**
	 * set id of parent component
	 * 
	 * @param parentId
	 */
	void setParentId(final String parentId);

}
