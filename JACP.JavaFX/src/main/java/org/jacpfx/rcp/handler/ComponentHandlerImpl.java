/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ComponentHandlerImpl.java]
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
package org.jacpfx.rcp.handler;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.StatelessCallabackComponent;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.component.EmbeddedFXComponent;
import org.jacpfx.rcp.scheduler.StatelessComponentSchedulerImpl;
import org.jacpfx.rcp.util.HandlerThreadFactory;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;
import org.jacpfx.rcp.worker.CallbackComponentInitWorker;
import org.jacpfx.rcp.worker.FXComponentInitWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles initialization an reassignment of component in perspective.
 * 
 * @author Andy Moncsek
 * 
 */
public class ComponentHandlerImpl
		implements
        org.jacpfx.api.handler.ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final StatelessComponentSchedulerImpl scheduler;
	private volatile PerspectiveLayoutInterface<Node, Node> perspectiveLayout;
	private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
	private final ExecutorService fxInitExecutor = Executors
			.newSingleThreadExecutor(new HandlerThreadFactory("fxInitExecutor:"));
    private final ExecutorService callbackInitExecutor = Executors
            .newSingleThreadExecutor(new HandlerThreadFactory("callbackInitExecutor:"));


    public ComponentHandlerImpl(
            final Launcher<?> launcher,
            final PerspectiveLayoutInterface<Node, Node> perspectiveLayout,
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue) {
		this.perspectiveLayout = perspectiveLayout;
		this.componentDelegateQueue = componentDelegateQueue;
		this.scheduler = new StatelessComponentSchedulerImpl(launcher);
		ShutdownThreadsHandler.registerExecutor(fxInitExecutor);
		ShutdownThreadsHandler.registerExecutor(callbackInitExecutor);
	}

	@Override
	public final void initComponent(final Message<Event, Object> action,
			final SubComponent<EventHandler<Event>, Event, Object> component) {
            this.handleInit(action, component);
	}

	@Override
	public final void handleAndReplaceComponent(
			final Message<Event, Object> action,
			final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (AStatelessCallbackComponent.class.isAssignableFrom(component.getClass())) {
            this.log("RUN STATELESS COMPONENTS:::" , component.getContext().getId());
            this.runStatelessCallbackComponent(
                    ((AStatelessCallbackComponent) component), action);
            return;
        }
        // all others
        this.putMessageToQueue(action,component);
        this.log("DONE EXECUTE REPLACE:::" , component.getContext().getId());
	}


	/**
	 * Handle state less callback component. This Method is invoked when an message is triggered.
	 * 
	 * @param component, the target component
	 * @param action, the message
	 */
	private void runStatelessCallbackComponent(
			final StatelessCallabackComponent<EventHandler<Event>, Event, Object> component,
			final Message<Event, Object> action) {
		this.scheduler.incomingMessage(action, component);
	}
	
	/**
	 * Execute subComponent initialization. This methods run's on "init" message while bootstrap.
	 * 
	 * @param action, the initial message
	 * @param component, the component to init
	 */
	private void handleInit(final Message<Event, Object> action,
			final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (EmbeddedFXComponent.class.isAssignableFrom(component.getClass())) {
			this.log("COMPONENT EXECUTE INIT:::" , component.getContext().getId());
			this.fxInitExecutor.execute(new FXComponentInitWorker(
                    this.perspectiveLayout.getTargetLayoutComponents(),
                    ((EmbeddedFXComponent) component), action, this.componentDelegateQueue));
			return;
		}// if END
		if (AStatelessCallbackComponent.class.isAssignableFrom(component.getClass())) {
			this.log("SATELESS BACKGROUND COMPONENT EXECUTE INIT:::"
                    , component.getContext().getId());
            final AStatelessCallbackComponent asyncComponent = AStatelessCallbackComponent.class.cast(component);
			this.runStatelessCallbackComponent(asyncComponent, action);
            return;
        }// else if END
        if (ASubComponent.class.isAssignableFrom(component.getClass())) {
            this.log("BACKGROUND COMPONENT EXECUTE INIT:::"
                    , component.getContext().getId());
            this.callbackInitExecutor.execute(new CallbackComponentInitWorker(
                    this.componentDelegateQueue, ((ASubComponent) component), action));
        }// else if END

	}


	/**
	 * set component blocked and add message to queue
	 * 
	 * @param component, the component where the message will be placed in queue
	 * @param action, the message
	 */
	private void putMessageToQueue(final Message<Event, Object> action,
			final SubComponent<EventHandler<Event>, Event, Object> component
			) {
		component.putIncomingMessage(action);
	}

	private void log(final String message, final String name) {
		if (this.logger.isLoggable(Level.FINE)) {
			this.logger.finest(">> " + message+ ' ' +name);
		}
	}
}
