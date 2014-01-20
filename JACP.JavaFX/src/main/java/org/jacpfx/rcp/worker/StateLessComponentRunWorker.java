/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [StateLessComponentRunWorker.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
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
package org.jacpfx.rcp.worker;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.StatelessCallabackComponent;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.util.WorkerUtil;

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
        AComponentWorker<SubComponent<EventHandler<Event>, Event, Object>> {
	private final SubComponent<EventHandler<Event>, Event, Object> component;
	private final StatelessCallabackComponent<EventHandler<Event>, Event, Object> parent;

	public StateLessComponentRunWorker(
			final SubComponent<EventHandler<Event>, Event, Object> component,
			final StatelessCallabackComponent<EventHandler<Event>, Event, Object> parent) {
		this.component = component;
		this.parent = parent;
	}

	@Override
	protected SubComponent<EventHandler<Event>, Event, Object> call()
			throws Exception {
			try {
                this.component.lock();
                runCallbackOnStartMethods(this.component);
				while (this.component.hasIncomingMessage()) {
					final Message<Event, Object> myAction = this.component
							.getNextIncomingMessage();
                    final JacpContextImpl context = JacpContextImpl.class.cast(this.component.getContext());
                    context.setReturnTarget(myAction.getSourceId());
                    final Object value = this.component.getComponent().handle(myAction);
                    final String targetId = context
                            .getReturnTargetAndClear();
					WorkerUtil.delegateReturnValue(this.component, targetId, value,
                            myAction);
				}
			} finally {
				this.component.release();
			}
		return this.component;
	}



	@Override
	protected void done() {
		try {
			final SubComponent<EventHandler<Event>, Event, Object> component = this.get();
			// check if component was deactivated and is still in instance list
			if (!component.getContext().isActive()) {
                try{
                    this.component.lock();
                    if(parent.getInstances().contains(component))forceShutdown(component, parent);
                } finally {
                    this.component.release();
                }

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
	 * @param component, the component to shutdown
	 * @param parent, the parent component
	 */
	private void forceShutdown(
			final SubComponent<EventHandler<Event>, Event, Object> component,
			final StatelessCallabackComponent<EventHandler<Event>, Event, Object> parent) {
        final String parentId = parent.getParentId();
        final Perspective<EventHandler<Event>, Event, Object> parentPerspctive = PerspectiveRegistry.findPerspectiveById(parentId);
        if(parentPerspctive!=null)parentPerspctive.unregisterComponent(parent);
        TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(parent));
	}
}
