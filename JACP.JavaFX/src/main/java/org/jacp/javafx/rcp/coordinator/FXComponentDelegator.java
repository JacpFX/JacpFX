/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FX2ComponentDelegator.java]
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
package org.jacp.javafx.rcp.coordinator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.component.IComponent;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.coordinator.IComponentDelegator;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * The component delegator handles a component target change, find the correct
 * perspective an add component to correct perspective
 * 
 * @author Andy Moncsek
 * 
 */
public class FXComponentDelegator extends Thread implements
		IComponentDelegator<EventHandler<Event>, Event, Object> {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue = new ArrayBlockingQueue<>(
			100);
	private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> componentHandler;
	private final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new CopyOnWriteArrayList<>();

	public FXComponentDelegator() {
		super("FXComponentDelegator");
		ShutdownThreadsHandler.registerThread(this);
	}
	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			try {
				final ISubComponent<EventHandler<Event>, Event, Object> component = this.componentDelegateQueue
						.take();
				final String targetId = component.getExecutionTarget();

				this.delegateTargetChange(targetId, component);

			} catch (final InterruptedException e) {
				logger.info("queue in FXComponentDelegator interrupted");
				break;
			}

		}
	}

	private void delegateTargetChange(final String target,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		// find responsible perspective
		final IPerspective<EventHandler<Event>, Event, Object> responsiblePerspective = FXUtil
				.getObserveableById(FXUtil.getTargetPerspectiveId(target),
						this.perspectives);
		// find correct target in perspective
		if (responsiblePerspective != null) {
			final String parentId = component.getParentId();
			// unregister component from previous parent
			if (!parentId.equals(responsiblePerspective.getId())) {
				final IPerspective<EventHandler<Event>, Event, Object> currentParent = FXUtil
						.getObserveableById(
								FXUtil.getTargetPerspectiveId(parentId),
								this.perspectives);
				currentParent.unregisterComponent(component);
			}
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
			final IPerspective<EventHandler<Event>, Event, Object> responsiblePerspective,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		if (!responsiblePerspective.isActive()) {
			// 1. init perspective (do not register component before perspective
			// is active, otherwise component will be handled once again)
			this.handleInActivePerspective(responsiblePerspective,
					new FXAction(responsiblePerspective.getId(),
							responsiblePerspective.getId(), FXUtil.MessageUtil.INIT, null));
		} // End if
		responsiblePerspective.registerComponent(component);
		responsiblePerspective.getComponentHandler().initComponent(
				new FXAction(component.getId(), component.getId(), FXUtil.MessageUtil.INIT, null),
				component);
	}

	private <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActivePerspective(
			final P component, final IAction<Event, Object> action) {
		component.setActive(true);
		Platform.runLater(() -> FXComponentDelegator.this.componentHandler
                .initComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component));
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
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(
			final IComponentHandler<P, IAction<Event, Object>> handler) {
		this.componentHandler = (IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>>) handler;

	}

	@Override
	public BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> getComponentDelegateQueue() {
		return this.componentDelegateQueue;
	}

	@Override
	public void delegateComponent(
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
        this.componentDelegateQueue.add(component);

	}

	@Override
	public void addPerspective(
			final IPerspective<EventHandler<Event>, Event, Object> perspective) {
		this.perspectives.add(perspective);

	}

	@Override
	public void removePerspective(
			final IPerspective<EventHandler<Event>, Event, Object> perspective) {
		this.perspectives.remove(perspective);

	}

}
