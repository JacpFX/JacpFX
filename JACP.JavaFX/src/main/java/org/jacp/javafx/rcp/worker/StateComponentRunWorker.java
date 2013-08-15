/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FX2Util.java]
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

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.context.JACPContextImpl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * This class handles running stateful background components
 * 
 * @author Andy Moncsek
 * 
 */
public class StateComponentRunWorker
		extends
		AFXComponentWorker<ISubComponent<EventHandler<Event>, Event, Object>> {
	private final ISubComponent<EventHandler<Event>, Event, Object> component;
	private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue;

	public StateComponentRunWorker(
			final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		super(component.getName());
		this.component = component;
		this.delegateQueue = delegateQueue;
	}

	@Override
	protected ISubComponent<EventHandler<Event>, Event, Object> call()
			throws Exception {
		synchronized (this.component) {
			this.component.lock();
			try {
				runCallbackOnStartMethods(this.component);
				while (this.component.hasIncomingMessage()) {
					final IAction<Event, Object> myAction = this.component
							.getNextIncomingMessage();
                    final JACPContextImpl context = JACPContextImpl.class.cast(this.component.getContext());
                    context.setHandleTarget(myAction.getSourceId());
                      // TODO move execution target to Context!
					final String targetCurrent = this.component
							.getExecutionTarget();
					final Object value = this.component.getComponentHandle().handle(myAction);
					final String targetId = context
							.getHandleTargetAndClear();
					this.delegateReturnValue(this.component, targetId, value,
							myAction);
					this.checkAndHandleTargetChange(this.component,
							targetCurrent);
				}
				runCallbackPostExecution(this.component);
				runCallbackOnTeardownMethods(this.component);
			} finally {
				this.component.release();
			}
		}
		return this.component;
	}
	
	

	/**
	 * check if target has changed
	 * 
	 * @param comp
	 * @param currentTaget
	 */
	private void checkAndHandleTargetChange(
			final ISubComponent<EventHandler<Event>, Event, Object> comp,
			final String currentTaget) {
		final String targetNew = comp.getExecutionTarget();
		if (!targetNew.equals(currentTaget)) {
			if (!component.isActive())
				throw new UnsupportedOperationException(
						"CallbackComponent may be moved or set to inactive but not both");
			this.changeComponentTarget(this.delegateQueue, comp);
		}
	}

	@Override
	protected final void done() {
		synchronized (this.component) {
			try {
				this.get();
			} catch (final InterruptedException | ExecutionException e) {
				// FIXME: Handle Exceptions the right way
				e.printStackTrace();
			}
        }
	}

}
