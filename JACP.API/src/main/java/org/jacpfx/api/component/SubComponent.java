/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [SubComponent.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */
package org.jacpfx.api.component;

import org.jacpfx.api.message.Message;

import java.util.concurrent.TransferQueue;

/**
 * Defines a subcomponent handled by a root component. A subcomponent is running
 * in a perspective environment an can be represented by an visible UI or non
 * visible background component.
 *
 * @param <L> defines the message listener type
 * @param <A> defines the basic event type
 * @param <M> defines the basic message type
 * @author Andy Moncsek
 */
public interface SubComponent<L, A, M> extends ComponentBase<L, M> {

    /**
     * Returns true if component has message in pipe.
     *
     * @return returns true if incoming message is in queue
     */
    boolean hasIncomingMessage();

    /**
     * Add new message to component.
     *
     * @param message, the message.
     */
    void putIncomingMessage(final Message<A, M> message);

    /**
     * Returns next message in pipe.
     *
     * @return the next message to handle
     * @throws java.lang.InterruptedException , the Exception will be thrown when main thread is interrupted and not delegated to the developer
     */
    Message<A, M> getNextIncomingMessage() throws InterruptedException;

    /**
     * Component is blocked when executed in thread.
     *
     * @return blocked state
     */
    boolean isBlocked();

    /**
     * Lock Component for execution in thread.
     */
    void lock();

    /**
     * Release lock after execution in thread.
     */
    void release();


    /**
     * Set parentId and global message queue to component
     *
     * @param parentId,     the parent id of the current component (the perspective id)
     * @param messageQueue, the message queue
     */
    void initEnv(final String parentId,
                 final TransferQueue<Message<A, M>> messageQueue);


    /**
     * Returns the component handle class, this is the users implementation of the component.
     *
     * @param <X>, the type
     * @return ComponentHandle, the component handle.
     */
    <X extends ComponentHandle<?, A, M>> X getComponent();

    /**
     * Set the component handle class. This is the users implementation of the component.
     *
     * @param handle, the component
     * @param <X>,    the type
     */
    <X extends ComponentHandle<?, A, M>> void setComponent(final X handle);


}
