/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [JacpContext.java]
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

package org.jacpfx.api.context;

import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 27.06.13
 * Time: 21:12
 * The JacpContext interface gives access to component basic meta data as well as listeners and other services.
 *
 * @param <L> defines the listener type
 * @param <M> defines the basic message type
 */
public interface JacpContext<L, M> {


    /**
     * Send a message to caller component itself.
     *
     * @param message, The message object.
     */
    void send(final M message);

    /**
     * Send a message to defined targetId.
     *
     * @param targetId, The target id for the message.
     * @param message,  The message object.
     */
    void send(final String targetId, final M message);

    /**
     * Returns an event handler that handles messages to caller component
     *
     * @param message, The message object.
     * @return an JavaFX event handler.
     */
    @Deprecated
    L getEventHandler(final M message);


    /**
     * Returns an event handler that handles messages to target component
     *
     * @param message  ; the message to send to target.
     * @param targetId ; the targets component id.
     * @return an JavaFX event handler.
     */
    @Deprecated
    L getEventHandler(final String targetId, final M message);

    /**
     * Returns the id of the component.
     *
     * @return the component id
     */
    String getId();

    /**
     * Returns the ID of parent component/perspective.
     *
     * @return a component id
     */
    String getParentId();

    /**
     * Returns the fully qualified id like "parentId.componentId"
     * @return The qualified Id
     */
    String getFullyQualifiedId();


    /**
     * Returns the component resource bundle.
     *
     * @return the defined resource bundle
     */
    ResourceBundle getResourceBundle();

    /**
     * Get the default active status of component.
     *
     * @return the active state of component
     */
    boolean isActive();

    /**
     * Set default active state of component.
     *
     * @param active ; the component active state.
     */
    void setActive(final boolean active);

    /**
     * Set component targetId which is the target of a background component return
     * value; the return value will be handled like an average message and will
     * be delivered to targeted component.
     *
     * @param componentTargetId ; represents a component id to return the value to
     */
    void setReturnTarget(final String componentTargetId) throws IllegalStateException;

    /**
     * Defines the perspective in which the component should executed in.
     *
     * @param id, the id of the parent perspective where the component should be executed in.
     */
    void setExecutionTarget(final String id) throws IllegalStateException;

    /**
     * Defines the target layoutId, where the UI component should appear in,the layout is registered in perspective and is a placeholder for the component.
     *
     * @param targetLayout, a target layout label.
     */
    void setTargetLayout(final String targetLayout) throws IllegalStateException;


}
