/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FX2PerspectiveHandler.java]
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
package org.jacp.javafx.rcp.handler;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.component.IStatelessCallabackComponent;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.componentLayout.IPerspectiveLayout;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.api.launcher.Launcher;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.component.AStatelessCallbackComponent;
import org.jacp.javafx.rcp.component.ASubComponent;
import org.jacp.javafx.rcp.scheduler.StatelessCallbackScheduler;
import org.jacp.javafx.rcp.util.HandlerThreadFactory;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;
import org.jacp.javafx.rcp.worker.CallbackComponentInitWorker;
import org.jacp.javafx.rcp.worker.FXComponentInitWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles initialization an reassignment of components in perspective.
 * 
 * @author Andy Moncsek
 * 
 */
public class FXComponentHandler
		implements
		IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final StatelessCallbackScheduler scheduler;
	private final IPerspectiveLayout<Node, Node> perspectiveLayout;
	private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
	private final ExecutorService executor = Executors
			.newCachedThreadPool(new HandlerThreadFactory("FXPerspectiveHandler:"));


	public FXComponentHandler(
            final Launcher<?> launcher,
            final IPerspectiveLayout<Node, Node> perspectiveLayout,
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue) {
		this.perspectiveLayout = perspectiveLayout;
		this.componentDelegateQueue = componentDelegateQueue;
		this.scheduler = new StatelessCallbackScheduler(launcher);
		ShutdownThreadsHandler.registerexecutor(executor);
	}

	@Override
	public final void initComponent(final IAction<Event, Object> action,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
            this.handleInit(action, component);
	}

	@Override
	public final void handleAndReplaceComponent(
			final IAction<Event, Object> action,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if (AStatelessCallbackComponent.class.isAssignableFrom(component.getClass())) {
            this.log("RUN STATELESS COMPONENTS:::" + component.getContext().getName());
            this.runStatelessCallbackComponent(
                    ((AStatelessCallbackComponent) component), action);
            return;
        }
        // all others
        this.putMessageToQueue(action,component);
        this.log("DONE EXECUTE REPLACE:::" + component.getContext().getName());
	}


	/**
	 * Handle state less callback component. This Method is invoked when an action is triggered.
	 * 
	 * @param component, the target component
	 * @param action, the action
	 */
	private void runStatelessCallbackComponent(
			final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> component,
			final IAction<Event, Object> action) {
		this.scheduler.incomingMessage(action, component);
	}
	
	/**
	 * Execute subComponent initialization. This methods run's on "init" action while bootstrap.
	 * 
	 * @param action, the initial action
	 * @param component, the component to init
	 */
	private void handleInit(final IAction<Event, Object> action,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		if (AFXComponent.class.isAssignableFrom(component.getClass())) {
			this.log("COMPONENT EXECUTE INIT:::" + component.getContext().getName());
			this.executor.execute(new FXComponentInitWorker(
                    this.perspectiveLayout.getTargetLayoutComponents(),
                    ((AFXComponent) component), action,this.componentDelegateQueue));
			return;
		}// if END
		if (AStatelessCallbackComponent.class.isAssignableFrom(component.getClass())) {
			this.log("SATELESS BACKGROUND COMPONENT EXECUTE INIT:::"
                    + component.getContext().getName());
            final AStatelessCallbackComponent asyncComponent = AStatelessCallbackComponent.class.cast(component);
            setAsyncComponentToActive(asyncComponent);
			this.runStatelessCallbackComponent(asyncComponent, action);
            return;
        }// else if END
        if (ASubComponent.class.isAssignableFrom(component.getClass())) {
            this.log("BACKGROUND COMPONENT EXECUTE INIT:::"
                    + component.getContext().getName());
            this.executor.execute(new CallbackComponentInitWorker(
                    this.componentDelegateQueue,((ASubComponent) component),action));
        }// else if END

	}

    private void setAsyncComponentToActive(AStatelessCallbackComponent component) {
              component.getContext().setActive(true);
    }


	
	/**
	 * set component blocked and add message to queue
	 * 
	 * @param component, the component where the action will be placed in queue
	 * @param action, the action
	 */
	private void putMessageToQueue(final IAction<Event, Object> action,
			final ISubComponent<EventHandler<Event>, Event, Object> component
			) {
		component.putIncomingMessage(action);
	}

	private void log(final String message) {
		if (this.logger.isLoggable(Level.FINE)) {
			this.logger.fine(">> " + message);
		}
	}
}
