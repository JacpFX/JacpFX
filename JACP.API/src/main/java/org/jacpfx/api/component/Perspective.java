/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [Perspective.java]
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
package org.jacpfx.api.component;

import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;
import org.jacpfx.api.coordinator.Coordinator;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.UIType;

import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Defines a perspective, a perspective is a root component handled by an
 * workbench and contains sub-component such as visible UI component or
 * background component. A workbench can handle one or more perspective (1-n)
 * and every perspective can handle one ore more component (1-n).
 *
 * @param <L> defines the message listener type
 * @param <A> defines the basic event type
 * @param <M> defines the basic message type
 * @author Andy Moncsek
 */
public interface Perspective<C,L, A, M> extends Component<L, M>,
        RootComponent<SubComponent<L, A, M>, Message<A, M>> {

    /**
     * The initialization method.
     *
     * @param componentDelegateQueue, component that should be delegated to an other perspective
     * @param messageDelegateQueue,   messages to component
     * @param messageCoordinator,     coordinates messages to component
     * @param launcher,               the component launcher
     */
    void init(
            final BlockingQueue<SubComponent<L, A, M>> componentDelegateQueue,
            final BlockingQueue<DelegateDTO<A, M>> messageDelegateQueue,
            final Coordinator<L, A, M> messageCoordinator, final Launcher<?> launcher);

    /**
     * post init method to set correct component handler and to initialize
     * component depending on objects created in startUp sequence.
     *
     * @param componentHandler, the component handler
     */
    void postInit(
            ComponentHandler<SubComponent<L, A, M>, Message<A, M>> componentHandler);

    /**
     * Returns all subcomponents in perspective.
     *
     * @return a list of all handled component in current perspective.
     */
    List<SubComponent<L, A, M>> getSubcomponents();


    /**
     * Handle a message call on perspective instance. This method should be
     * override to handle the layout of an perspective.
     *
     * @param message, the message to perspective.
     */
    void handlePerspective(final Message<A, M> message);

    /**
     * Returns delegate queue to delegate component to correct target
     *
     * @return the delegate queue
     */
    BlockingQueue<SubComponent<L, A, M>> getComponentDelegateQueue();

    /**
     * Returns delegate queue to delegate actions to correct target
     *
     * @return the delegate queue
     */
    BlockingQueue<DelegateDTO<A, M>> getMessageDelegateQueue();

    /**
     * returns the component coordinator message queue;
     *
     * @return message queue
     */
    BlockingQueue<Message<A, M>> getMessageQueue();

    /**
     * Returns the injected perspective representation. This Injectable is the implementation of a perspective which includes all handle methods.
     *
     * @return the perspective implementation.
     */
    Injectable getPerspective();

    /**
     * Returns layout dto.
     *
     * @return an PerspectiveLayoutInterface instance to define basic layout stuff for
     * perspective
     */
    PerspectiveLayoutInterface<C, C> getIPerspectiveLayout();


    /**
     * Set the default perspective layout entity for the perspective.
     *
     * @param layout, The layout dto
     */
    void setIPerspectiveLayout(PerspectiveLayoutInterface<C, C> layout);

    /**
     * Contains the document url describing the UI.
     *
     * @return the document url
     */
    String getViewLocation();

    /**
     * Set the viewLocation location on component start.
     *
     * @param documentURL , the url of the FXML document
     */
    void setViewLocation(final String documentURL);

    /**
     * The document URL describing the UI.
     *
     * @return the document url
     */
    URL getDocumentURL();


    /**
     * Distinguish component types.
     *
     * @return the type of the component.
     */
    UIType getType();

    /**
     * Set the UI type to distinguish component types
     *
     * @param type, PROGRAMMATIC / DECLARATIVE type
     */
    void setUIType(final UIType type);

}
