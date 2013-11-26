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
package org.jacpfx.rcp.delegator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.action.IAction;
import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.delegator.IComponentDelegator;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.rcp.action.FXAction;
import org.jacpfx.rcp.context.JACPContextImpl;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;

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
                final String targetId = JACPContextImpl.class.cast(component.getContext()).getExecutionTarget();
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
        activateInactiveComponent(responsiblePerspective);
		responsiblePerspective.registerComponent(component);
		responsiblePerspective.getComponentHandler().initComponent(
				new FXAction(component.getContext().getId(), component.getContext().getId(), FXUtil.MessageUtil.INIT, null),
				component);
	}

    private void activateInactiveComponent(
            final IPerspective<EventHandler<Event>, Event, Object> responsiblePerspective) {
        if (!responsiblePerspective.getContext().isActive()) {
            // 1. init perspective (do not register component before perspective
            // is active, otherwise component will be handled once again)
            this.handleInActivePerspective(responsiblePerspective,
                    new FXAction(responsiblePerspective.getContext().getId(),
                            responsiblePerspective.getContext().getId(), FXUtil.MessageUtil.INIT, null));
        } // End if
    }

	private <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActivePerspective(
			final P component, final IAction<Event, Object> action) {
		component.getContext().setActive(true);
        // TODO remove runLater and ensure in workbench handler that correct thread is used... for perspecive handler this is not nessesary
        //noinspection unchecked
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
        try {
            this.componentDelegateQueue.put(component);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }

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
