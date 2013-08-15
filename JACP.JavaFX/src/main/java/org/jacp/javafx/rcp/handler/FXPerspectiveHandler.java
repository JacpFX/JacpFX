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

import javafx.application.Platform;
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
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.scheduler.StatelessCallbackScheduler;
import org.jacp.javafx.rcp.util.HandlerThreadFactory;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;
import org.jacp.javafx.rcp.worker.FXComponentInitWorker;
import org.jacp.javafx.rcp.worker.FXComponentReplaceWorker;
import org.jacp.javafx.rcp.worker.StateComponentRunWorker;

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
public class FXPerspectiveHandler
		implements
		IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	public static int MAX_INCTANCE_COUNT;
	private final FXComponentLayout layout;
	private final StatelessCallbackScheduler scheduler;
	private final IPerspectiveLayout<Node, Node> perspectiveLayout;
	private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
	private final ExecutorService executor = Executors
			.newCachedThreadPool(new HandlerThreadFactory("FXPerspectiveHandler:"));


	public FXPerspectiveHandler(
			final Launcher<?> launcher,
			final FXComponentLayout layout,
			final IPerspectiveLayout<Node, Node> perspectiveLayout,
			final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue) {
		this.layout = layout;
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
		if (component.isBlocked() && component.isStarted()) {
			this.putMessageToQueue(component, action);
			this.log("ADD TO QUEUE:::" + component.getName());
		} else {
			this.executeComponentReplaceThread(this.perspectiveLayout,
					component, action, this.layout);

		}
		this.log("DONE EXECUTE REPLACE:::" + component.getName());
	}

	/**
	 * start component replace thread, be aware that all actions are in
	 * components message box!
	 * 
	 * @param layout
	 * @param component
	 */
	private void executeComponentReplaceThread(
			final IPerspectiveLayout<? extends Node, Node> perspectiveLayout,
			final ISubComponent<EventHandler<Event>, Event, Object> component,
			final IAction<Event, Object> action, final FXComponentLayout layout) {
		if (AStatelessCallbackComponent.class.isAssignableFrom(component.getClass())) {
			this.log("RUN STATELESS COMPONENTS:::" + component.getName());
			this.runStatelessCallbackComponent(
					((AStatelessCallbackComponent) component), action);
			return;
		}
		this.putMessageToQueue(component, action);
		if (AFXComponent.class.isAssignableFrom(component.getClass())) {
			this.log("CREATE NEW THREAD:::" + component.getName());			
			this.runFXComponent(perspectiveLayout, component, layout);
			return;
		} 		
		if (ASubComponent.class.isAssignableFrom(component.getClass())) {
			this.log("CREATE NEW THREAD:::" + component.getName());
			this.runStateComponent(action, component);
        }
		

	}

	
	
	

	/**
	 * Handle state less callback component. This Method is invoked when an action is triggered.
	 * 
	 * @param component
	 * @param action
	 */
	private void runStatelessCallbackComponent(
			final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> component,
			final IAction<Event, Object> action) {
		this.scheduler.incomingMessage(action, component);
	}
	
	
	/**
	 * Run component in background thread.
	 * 
	 * @param layout
	 * @param component
	 */
	private void runFXComponent(
			final IPerspectiveLayout<? extends Node, Node> perspectiveLayout,
			final ISubComponent<EventHandler<Event>, Event, Object> component,
			final FXComponentLayout layout) {
		this.executor.execute(new FXComponentReplaceWorker(perspectiveLayout
				.getTargetLayoutComponents(), this.componentDelegateQueue,
				((AFXComponent) component), layout));
	}

	/**
	 * Run background components thread.
	 * 
	 * @param action
	 * @param component
	 */
	private void runStateComponent(
			final IAction<Event, Object> action,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		this.executor.execute(new StateComponentRunWorker(
				this.componentDelegateQueue, component));
	}

	/**
	 * Execute subComponent initialization. This methods run's on "init" action while bootstrap.
	 * 
	 * @param action
	 * @param component
	 */
	private void handleInit(final IAction<Event, Object> action,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		if (AFXComponent.class.isAssignableFrom(component.getClass())) {
			this.log("COMPONENT EXECUTE INIT:::" + component.getName());
			this.executor.execute(new FXComponentInitWorker(
                    this.perspectiveLayout.getTargetLayoutComponents(),
                    ((AFXComponent) component), action, this.layout));
			return;
		}// if END

		if (AStatelessCallbackComponent.class.isAssignableFrom(component.getClass())) {
			this.log("SATELESS BACKGROUND COMPONENT EXECUTE INIT:::"
					+ component.getName());
			this.runStatelessCallbackComponent(
					((AStatelessCallbackComponent) component), action);
        }// else if END
        if (ASubComponent.class.isAssignableFrom(component.getClass())) {
            this.log("BACKGROUND COMPONENT EXECUTE INIT:::"
                    + component.getName());
            this.putMessageToQueue(component, action);
            this.runStateComponent(action,component);
            return;
        }// else if END

	}


	
	/**
	 * set component blocked and add message to queue
	 * 
	 * @param component
	 * @param action
	 */
	private void putMessageToQueue(
			final ISubComponent<EventHandler<Event>, Event, Object> component,
			final IAction<Event, Object> action) {
		component.putIncomingMessage(action);
	}

	private void log(final String message) {
		if (this.logger.isLoggable(Level.FINE)) {
			this.logger.fine(">> " + message);
		}
	}
}
