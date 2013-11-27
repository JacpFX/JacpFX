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
import org.jacpfx.api.message.Message;
import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.delegator.IMessageDelegator;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * The message delegate handles messages from one perspective to an other.
 * 
 * @author Andy Moncsek
 * 
 */
public class FXMessageDelegator extends Thread implements
		IMessageDelegator<EventHandler<Event>, Event, Object> {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
	private final BlockingQueue<IDelegateDTO<Event, Object>> messageDelegateQueue = new ArrayBlockingQueue<>(
			10000);
	private final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new CopyOnWriteArrayList<>();

	public FXMessageDelegator() {
		super("FXMessageDelegator");
		ShutdownThreadsHandler.registerThread(this);
	}

	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			try {
				final IDelegateDTO<Event, Object> dto = this.messageDelegateQueue.take();
				final String targetId = dto.getTarget();
				final Message<Event, Object> action = dto.getMessage();
				this.delegateMessage(targetId, action);
			} catch (final InterruptedException e) {
				logger.info("queue in FXComponentDelegator interrupted");
				break;
			} catch (final ExecutionException e) {
                logger.info("queue in FXComponentDelegator interrupted");
                e.printStackTrace();
            }

		}
	}

	private void delegateMessage(final String target,
			final Message<Event, Object> action) throws ExecutionException, InterruptedException {
		// Find local Target; if target is perspective handle target or
		// delegate
		// message to responsible component observer
		if (FXUtil.isLocalMessage(target)) {
			this.handleMessage(target, action);
		} // End if
		else {
			this.callComponentDelegate(target, action);
		} // End else
	}

	void handleMessage(final String target,
                       final Message<Event, Object> action) throws ExecutionException, InterruptedException {
		final IPerspective<EventHandler<Event>, Event, Object> perspective = FXUtil
				.getObserveableById(FXUtil.getTargetPerspectiveId(target),
						this.perspectives);
		if (perspective != null) {
			this.handleComponentHit(target, action, perspective);
		} // End if
		else {
			// try to find correct perspective by searching in all perspectives;
			// this can only be a subcomponent
            final CompletableFuture<IPerspective<EventHandler<Event>, Event, Object>> perspectiveFuture =
                    CompletableFuture.supplyAsync(()-> FXUtil.getTargetComponentId(target))
                            .thenApplyAsync(id -> FXUtil
                                    .findRootByObserveableId(id, this.perspectives));
			final IPerspective<EventHandler<Event>, Event, Object> perspectiveTemp = perspectiveFuture.get();
			if (perspectiveTemp != null) {
				final String tempTargetId = perspectiveTemp.getContext().getId().concat(".")
						.concat(FXUtil.getTargetComponentId(target));
				this.callComponentDelegate(tempTargetId, action);
			} else {
				throw new UnsupportedOperationException(
						"No responsible perspective found. Handling not implemented yet. target: "
								+ target + " perspectives: "
								+ this.perspectives);
			}
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
			this.handleInActivePerspective(perspective, action);
		} // End else
	}

	/**
	 * handle message to active perspective; check if target is perspective or
	 * component
	 * 
	 * @param target
	 * @param action
	 * @param perspective
	 */
	private void handleMessageToActivePerspective(final String target,
			final Message<Event, Object> action,
			final IPerspective<EventHandler<Event>, Event, Object> perspective) {
		// if perspective already active handle perspective and replace
		// with newly created layout component in workbench
		this.log(" //1.1.1.1// perspective HIT handle ACTIVE: "
				+ action.getTargetId());
		if (FXUtil.isLocalMessage(target)) {
			// message is addressing perspective
			this.handleActive(perspective, action);
		} // End if
		else {
			// delegate to addressed component
			perspective.getComponentsMessageQueue().add(action);
		} // End else
	}

	<P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActivePerspective(
            final P component, final Message<Event, Object> action) {
		component.getContext().setActive(true);
		Platform.runLater(() -> FXMessageDelegator.this.componentHandler
                .initComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component));
	}

	/**
	 * Handle an active perspective
	 * 
	 * @param component
	 * @param action
	 */
	private <P extends IComponent<EventHandler<Event>, Event, Object>> void handleActive(
			final P component, final Message<Event, Object> action) {
		FXMessageDelegator.this.componentHandler
                .handleAndReplaceComponent(
                        action,
                        (IPerspective<EventHandler<Event>, Event, Object>) component);

	}

	/**
	 * delegate to responsible componentObserver in correct perspective
	 * 
	 * @param target
	 * @param action
	 */
	private void callComponentDelegate(final String target,
			final Message<Event, Object> action) {
		final IPerspective<EventHandler<Event>, Event, Object> perspective = FXUtil
				.getObserveableById(FXUtil.getTargetPerspectiveId(target),
						this.perspectives);
		if (perspective != null) {
			if (!perspective.getContext().isActive()) {
				this.handleInActivePerspective(perspective, action);
			} // End inner if
			else {
                try {
                    perspective.getComponentsMessageQueue().put(action);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //TODO handle exception global
                }
            } // End else

		} // End if

	}

    @Override
	public void delegateMessage(final IDelegateDTO<Event, Object> messageDTO) {
        try {
            this.messageDelegateQueue.put(messageDTO);
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

	void log(final String message) {
		this.logger.fine(message);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(
			final IComponentHandler<P, Message<Event, Object>> handler) {
		this.componentHandler = (IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

	}

	@Override
	public BlockingQueue<IDelegateDTO<Event, Object>> getMessageDelegateQueue() {
		return this.messageDelegateQueue;
	}

}
