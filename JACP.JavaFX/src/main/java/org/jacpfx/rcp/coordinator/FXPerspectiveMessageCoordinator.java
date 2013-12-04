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
package org.jacpfx.rcp.coordinator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.coordinator.IPerspectiveCoordinator;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observe perspectives and delegates message to correct component
 * 
 * @author Andy Moncsek
 */
public class FXPerspectiveMessageCoordinator extends AFXCoordinator implements
		IPerspectiveCoordinator<EventHandler<Event>, Event, Object> {

	private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;

	public FXPerspectiveMessageCoordinator() {
		super("FXPerspectiveCoordinator");
	}

	@Override
	public void handleMessage(final String target,
			final Message<Event, Object> action) {
		final IPerspective<EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(target);
		if (perspective != null) {
			this.handleComponentHit(target, action, perspective);
		} // End if
		else {
			// TODO implement missing perspective handling!!
			throw new UnsupportedOperationException(
					"No responsible perspective found. Handling not implemented yet. target: "
							+ target + " available perspectives: " + PerspectiveRegistry.getAllPerspectives());
		} // End else
	}

	/**
	 * handle message target hit
	 * 
	 * @param target
	 * @param action
	 */
	private void handleComponentHit(final String target,
			final Message<Event, Object> action,
			final IPerspective<EventHandler<Event>, Event, Object> perspective) {
		if (perspective.getContext().isActive()) {
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
			final Message<Event, Object> action,
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
			final P component, final Message<Event, Object> action) {
		Platform.runLater(() -> FXPerspectiveMessageCoordinator.this.componentHandler
                .handleAndReplaceComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component) // End runnable
		); // End runlater
	}

	@Override
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActive(
			final P component, final Message<Event, Object> action) {
		component.getContext().setActive(true);
		Platform.runLater(() -> FXPerspectiveMessageCoordinator.this.componentHandler
                .initComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component));
	}

	@SuppressWarnings("unchecked")
	@Override
	public IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> getComponentHandler() {
		return this.componentHandler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(
			final IComponentHandler<P, Message<Event, Object>> handler) {
		this.componentHandler = (IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

	}

}
