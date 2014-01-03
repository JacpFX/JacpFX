/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [IDelegator.java]
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
package org.jacpfx.api.coordinator;


import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.message.Message;

/**
 * Basic delegate interface
 * 
 * @author Andy Moncsek
 * 
 * @param <L>, the listener type
 * @param <A>, the event type
 * @param <M>, the message type
 */
public interface IDelegator<L, A, M> {

	/**
	 * Set the associated perspective handler.
	 * 
	 * @param handler, the perspective handler.
     * @param  <P> P should extend an IComponent
	 */
	<P extends IComponent<L, M>> void setPerspectiveHandler(
            IComponentHandler<P, Message<A, M>> handler);

}
