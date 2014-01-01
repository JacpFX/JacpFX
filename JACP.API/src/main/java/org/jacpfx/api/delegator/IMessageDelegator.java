/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IMessageDelegator.java]
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
package org.jacpfx.api.delegator;

import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.coordinator.IDelegator;

import java.util.concurrent.BlockingQueue;

/**
 * Defines an interface for a message delegator.
 * 
 * @author Andy Moncsek
 * 
 * @param <L>
 * @param <A>
 * @param <M>
 */
public interface IMessageDelegator<L, A, M> extends IDelegator<L, A, M> {


	/**
	 * Returns the delegate queue.
	 * 
	 * @return the delegate queue
	 */
	BlockingQueue<IDelegateDTO<A, M>> getMessageDelegateQueue();

}