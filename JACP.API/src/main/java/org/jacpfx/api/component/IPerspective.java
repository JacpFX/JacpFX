/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [IPerspective.java]
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
package org.jacpfx.api.component;

import org.jacpfx.api.coordinator.ICoordinator;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.launcher.Launcher;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Defines a perspective, a perspective is a root component handled by an
 * workbench and contains subcomponents such as visibla UI components or
 * background components. A workbench can handle one or more perspectives (1-n)
 * and every perspective can handle one ore more components (1-n).
 * 
 * @author Andy Moncsek
 * 
 * @param <L>
 *            defines the message listener type
 * @param <A>
 *            defines the basic message type
 * @param <M>
 *            defines the basic message type
 */
public interface IPerspective<L, A, M> extends IComponent<L, M>,
		IRootComponent<ISubComponent<L, A, M>, Message<A, M>>{

	/**
	 * The initialization method.
	 * 
	 * @param componentDelegateQueue, components that should be delegated to an other perspective
	 * @param messageDelegateQueue, messages to components
     * @param  messageCoordinator
     * @param  launcher
	 */
	void init(
			final BlockingQueue<ISubComponent<L, A, M>> componentDelegateQueue,
			final BlockingQueue<IDelegateDTO<A, M>> messageDelegateQueue,
            final ICoordinator<L,A,M> messageCoordinator,final Launcher<?> launcher);

	/**
	 * post init method to set correct component handler and to initialize
	 * components depending on objects created in startUp sequence.
	 * 
	 * @param componentHandler
	 */
	void postInit(
			IComponentHandler<ISubComponent<L, A, M>, Message<A, M>> componentHandler);

	/**
	 * Returns all subcomponents in perspective.
	 * 
	 * @return a list of all handled components in current perspective.
	 */
	List<ISubComponent<L, A, M>> getSubcomponents();


	/**
	 * Handle a message call on perspective instance. This method should be
	 * override to handle the layout of an perspective.
	 * 
	 * @param action
	 */
	void handlePerspective(final Message<A, M> action);

	/**
	 * Returns delegate queue to delegate components to correct target
	 * 
	 * @return the delegate queue
	 */
	BlockingQueue<ISubComponent<L, A, M>> getComponentDelegateQueue();

	/**
	 * Returns delegate queue to delegate actions to correct target
	 * 
	 * @return the delegate queue
	 */
	BlockingQueue<IDelegateDTO<A, M>> getMessageDelegateQueue();

	/**
	 * returns the components coordinator message queue;
	 * 
	 * @return message queue
	 */
	BlockingQueue<Message<A, M>> getMessageQueue();

    /**
     * Returns the injected perspective representation. This Injectable is the implementation of a perspective which includes all handle methods.
     * @return
     */
    Injectable getPerspective();

}
