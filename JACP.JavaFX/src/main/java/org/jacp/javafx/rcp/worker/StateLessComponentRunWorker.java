/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [StateLessComponentRunWorker.java]
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
package org.jacp.javafx.rcp.worker;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.component.IStatelessCallabackComponent;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.TearDownHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

/**
 * CallbackComponent worker to run instances of a stateless component in a worker
 * thread.
 * 
 * @author Andy Moncsek
 * 
 */
public class StateLessComponentRunWorker
		extends
		AFXComponentWorker<ISubComponent<EventHandler<Event>, Event, Object>> {
	private final ISubComponent<EventHandler<Event>, Event, Object> component;
	private final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> parent;

	public StateLessComponentRunWorker(
			final ISubComponent<EventHandler<Event>, Event, Object> component,
			final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> parent) {
		super(component.getName() + component);
		this.component = component;
		this.parent = parent;
	}

	@Override
	protected ISubComponent<EventHandler<Event>, Event, Object> call()
			throws Exception {
			this.component.lock();
			try {
                runCallbackOnStartMethods(this.component);
				while (this.component.hasIncomingMessage()) {
					final IAction<Event, Object> myAction = this.component
							.getNextIncomingMessage();
                    final JACPContextImpl context = JACPContextImpl.class.cast(this.component.getContext());
                    context.setHandleTarget(myAction.getSourceId());
                    final Object value = this.component.getComponentHandle().handle(myAction);
                    final String targetId = context
                            .getHandleTargetAndClear();
					this.delegateReturnValue(this.component, targetId, value,
							myAction);
				}
				runCallbackPostExecution(this.component);
			} finally {
				this.component.release();
			}
		return this.component;
	}



	@Override
	protected void done() {
        ISubComponent<EventHandler<Event>, Event, Object> component = null;
		try {
			component = this.get();
			// check if component was deactivated and is still in instance list
			if (!component.isActive()
					&& parent.getInstances().contains(component)) {
				forceShutdown(component, parent);
			}
		} catch (final InterruptedException | ExecutionException e) {
			e.printStackTrace();
			// TODO add to error queue and restart thread if messages in
			// queue
		}

    }

	/**
	 * Handle shutdown of components.
	 * 
	 * @param component
	 * @param parent
	 */
	private void forceShutdown(
			final ISubComponent<EventHandler<Event>, Event, Object> component,
			final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> parent) {
		// remove first component from instance list
		if (parent.getInstances().contains(component))
			parent.getInstances().remove(component);
		// create a copy off all remaining instances
		final List<ISubComponent<EventHandler<Event>, Event, Object>> instances = new CopyOnWriteArrayList<>(
				parent.getInstances());
		// create a backup (for iteration and later to handle teardown)
		final List<ISubComponent<EventHandler<Event>, Event, Object>> instancesCopy = new CopyOnWriteArrayList<>(
				instances);
		// clear all created instances
		parent.getInstances().clear();
		// check if execution is done and remove from list (TODO: any better
		// idea?)
		while (!instances.isEmpty()) {
			for (final ISubComponent<EventHandler<Event>, Event, Object> c : instancesCopy) {
				if (!c.isBlocked() && instances.contains(c))
					instances.remove(c);
			}
		}
		// ad first (missing) component to list which is passed to handle
		// teardown
		instancesCopy.add(component);

		Platform.runLater(() -> TearDownHandler.handleAsyncTearDown(instancesCopy));
	}
}
