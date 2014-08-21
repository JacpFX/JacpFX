/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [FX2ComponentDelegator.java]
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
package org.jacpfx.rcp.delegator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.Component;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.QueueSizes;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * The component delegator handles a component target change, find the correct
 * perspective an add component to correct perspective
 * 
 * @author Andy Moncsek
 * 
 */
public class ComponentDelegator extends Thread implements
        org.jacpfx.api.delegator.ComponentDelegator<EventHandler<Event>, Event, Object> {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue = new ArrayBlockingQueue<>(
            QueueSizes.COMPONENT_DELEGATOR_QUEUE_SIZE);
	private ComponentHandler<Perspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;

	public ComponentDelegator() {
		super("ComponentDelegator");
		ShutdownThreadsHandler.registerThread(this);
	}
	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			try {
				final SubComponent<EventHandler<Event>, Event, Object> component = this.componentDelegateQueue
						.take();
                final String targetId = JacpContextImpl.class.cast(component.getContext()).getExecutionTarget();
				this.delegateTargetChange(targetId, component);

			} catch (final InterruptedException e) {
				logger.info("queue in ComponentDelegator interrupted");
				break;
			}

		}
	}

	private void delegateTargetChange(final String target,
			final SubComponent<EventHandler<Event>, Event, Object> component) {
		// find responsible perspective
		final Perspective<EventHandler<Event>, Event, Object> responsiblePerspective = PerspectiveRegistry.findPerspectiveById(target);
		// find correct target in perspective
		if (responsiblePerspective != null) {
			this.handleTargetHit(responsiblePerspective, component);

		} // End if
		else {
			this.handleTargetMiss();
		} // End else
	}

	/**
	 * handle component delegate when target was found
	 * 
	 * @param responsiblePerspective
	 * @param component
	 */
	private void handleTargetHit(
			final Perspective<EventHandler<Event>, Event, Object> responsiblePerspective,
			final SubComponent<EventHandler<Event>, Event, Object> component) {
        activateInactiveComponent(responsiblePerspective);
		responsiblePerspective.registerComponent(component);
		responsiblePerspective.getComponentHandler().initComponent(
				new MessageImpl(component.getContext().getId(), component.getContext().getId(), FXUtil.MessageUtil.INIT, null),
				component);
	}

    private void activateInactiveComponent(
            final Perspective<EventHandler<Event>, Event, Object> responsiblePerspective) {
        if (!responsiblePerspective.getContext().isActive()) {
            // 1. init perspective (do not register component before perspective
            // is active, otherwise component will be handled once again)
            this.handleInActivePerspective(responsiblePerspective,
                    new MessageImpl(responsiblePerspective.getContext().getId(),
                            responsiblePerspective.getContext().getId(), FXUtil.MessageUtil.INIT, null));
        } // End if
    }

	private <P extends Component<EventHandler<Event>,  Object>> void handleInActivePerspective(
			final P component, final Message<Event, Object> action) {
		component.getContext().setActive(true);
        //noinspection unchecked
        Platform.runLater(() -> ComponentDelegator.this.componentHandler
                .initComponent(
                        action,
                        (Perspective<EventHandler<Event>, Event, Object>) component));
	}

	/**
	 * handle component delegate when no target found
	 */
	private void handleTargetMiss() {
		throw new UnsupportedOperationException(
				"No responsible perspective found. Handling not implemented yet.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends Component<EventHandler<Event>,  Object>> void setPerspectiveHandler(
            final ComponentHandler<P, Message<Event, Object>> handler) {
		this.componentHandler = (ComponentHandler<Perspective<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

	}

	@Override
	public BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> getComponentDelegateQueue() {
		return this.componentDelegateQueue;
	}

	@Override
	public void delegateComponent(
			final SubComponent<EventHandler<Event>, Event, Object> component) {
        try {
            this.componentDelegateQueue.put(component);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }

    }

}
