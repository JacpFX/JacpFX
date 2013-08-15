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
import org.jacp.api.coordinator.IPerspectiveCoordinator;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.javafx.rcp.util.FXUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observe perspectives and delegates message to correct component
 * 
 * @author Andy Moncsek
 */
public class FXPerspectiveCoordinator extends AFXCoordinator implements
		IPerspectiveCoordinator<EventHandler<Event>, Event, Object> {

	private final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new CopyOnWriteArrayList<>();
	private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> componentHandler;

	public FXPerspectiveCoordinator() {
		super("FXPerspectiveCoordinator");
	}

	@Override
	public void handleMessage(final String target,
			final IAction<Event, Object> action) {
		final IPerspective<EventHandler<Event>, Event, Object> perspective = FXUtil
				.getObserveableById(FXUtil.getTargetPerspectiveId(target),
						this.perspectives);
		if (perspective != null) {
			this.handleComponentHit(target, action, perspective);
		} // End if
		else {
			// TODO implement missing perspective handling!!
			throw new UnsupportedOperationException(
					"No responsible perspective found. Handling not implemented yet. target: "
							+ target + " perspectives: " + this.perspectives);
		} // End else
	}

	/**
	 * handle message target hit
	 * 
	 * @param target
	 * @param action
	 */
	private void handleComponentHit(final String target,
			final IAction<Event, Object> action,
			final IPerspective<EventHandler<Event>, Event, Object> perspective) {
		if (perspective.isActive()) {
			this.handleMessageToActivePerspective(target, action, perspective);
		} // End if
		else {
			// perspective was not active and will be initialized
			this.log(" //1.1.1.1// perspective HIT handle IN-ACTIVE: "
					+ action.getTargetId());
			this.handleInActive(perspective, action);
		} // End else
	}

	/**
	 * handle message to active perspective; check if target is perspective or
	 * component
	 * 
	 * @param targetId
	 * @param action
	 * @param perspective
	 */
	private void handleMessageToActivePerspective(final String targetId,
			final IAction<Event, Object> action,
			final IPerspective<EventHandler<Event>, Event, Object> perspective) {
		// if perspective already active handle perspective and replace
		// with newly created layout component in workbench
		this.log(" //1.1.1.1// perspective HIT handle ACTIVE: "
				+ action.getTargetId());
		if (FXUtil.isLocalMessage(targetId)) {
			// message is addressing perspective
			this.handleActive(perspective, action);
		} // End if
		else {
			// delegate to addressed component
			this.delegateMessageToCorrectPerspective(targetId, action,
					perspective.getMessageDelegateQueue());

		} // End else
	}

	@Override
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void handleActive(
			final P component, final IAction<Event, Object> action) {
		Platform.runLater(() -> FXPerspectiveCoordinator.this.componentHandler
                .handleAndReplaceComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component) // End runnable
		); // End runlater
	}

	@Override
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActive(
			final P component, final IAction<Event, Object> action) {
		component.setActive(true);
		Platform.runLater(() -> FXPerspectiveCoordinator.this.componentHandler
                .initComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component));
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

	@SuppressWarnings("unchecked")
	@Override
	public IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> getComponentHandler() {
		return this.componentHandler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(
			final IComponentHandler<P, IAction<Event, Object>> handler) {
		this.componentHandler = (IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>>) handler;

	}

}
