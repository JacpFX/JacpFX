/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [ICoordinator.java]
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
import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.message.Message;

import java.util.concurrent.BlockingQueue;

/**
 * Defines a basic observer for component messages; handles the message and
 * delegate to responsible component.
 *
 * @param <L> defines the message listener type
 * @param <A> defines the basic event type
 * @param <M> defines the basic message type
 * @author Andy Moncsek
 */
public interface ICoordinator<L, A, M> {

    /**
     * Handles message to specific component addressed by the id.
     *
     * @param id,      the target id.
     * @param message, the message
     */
    void handleMessage(final String id, final Message<A, M> message);

    /**
     * Returns the message queue of coordinator.
     *
     * @return the message queue
     */
    BlockingQueue<Message<A, M>> getMessageQueue();


    /**
     * set associated componentHandler
     *
     * @param <P>,     is type of IComponent
     * @param handler, the component handler that handles component execution.
     */
    <P extends IComponent<L, M>> void setComponentHandler(
            final IComponentHandler<P, Message<A, M>> handler);

    /**
     * set associated perspectiveHandler
     *
     * @param <P>,     is type of IComponent
     * @param handler, the perspective handler that handles perspective execution.
     */
    <P extends IComponent<L, M>> void setPerspectiveHandler(
            final IComponentHandler<P, Message<A, M>> handler);

    /**
     * Set the delegate queue, which delegates messages to correct responsible perspective.
     *
     * @param delegateQueue, The delegate queue.
     */
    void setDelegateQueue(final BlockingQueue<IDelegateDTO<A, M>> delegateQueue);

}
