/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [AStatelessCallbackComponent.java]
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
package org.jacp.javafx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.IStatelessCallabackComponent;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.HandlerThreadFactory;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * represents a abstract stateless background component
 * 
 * @author Andy Moncsek
 * 
 */

public abstract class AStatelessCallbackComponent extends ASubComponent
		implements
		IStatelessCallabackComponent<EventHandler<Event>, Event, Object>{
	public static int MAX_INCTANCE_COUNT;



	private final AtomicInteger threadCount = new AtomicInteger(0);

	private final List<ISubComponent<EventHandler<Event>, Event, Object>> componentInstances = new CopyOnWriteArrayList<>();

	private volatile ExecutorService executor = Executors
			.newCachedThreadPool(new HandlerThreadFactory("AStatelessCallbackComponent:"));
	static {
		final Runtime runtime = Runtime.getRuntime();
		final int nrOfProcessors = runtime.availableProcessors();
		AStatelessCallbackComponent.MAX_INCTANCE_COUNT = nrOfProcessors + 1;
	}
	
	public AStatelessCallbackComponent() {
		ShutdownThreadsHandler.registerexecutor(executor);
	}






	/**
	 * init cloned instance with values of blueprint
	 * 
	 * @param handler
	 * @return
	 */
	public final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> init(
			final IComponentHandle<Object,EventHandler<Event>, Event, Object> handler) {

        final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> comp = new EmbeddedStatelessCallbackComponent(handler);
		FXUtil.setPrivateMemberValue(AComponent.class, comp,
				FXUtil.ACOMPONENT_ID, this.getId());
		FXUtil.setPrivateMemberValue(AComponent.class, comp,
				FXUtil.ACOMPONENT_ACTIVE, this.isActive());
		FXUtil.setPrivateMemberValue(AComponent.class, comp,
				FXUtil.ACOMPONENT_NAME, this.getName());
		FXUtil.setPrivateMemberValue(ASubComponent.class, comp,
				FXUtil.ACOMPONENT_EXTARGET, this.getExecutionTarget());
		comp.initEnv(this.getParentId(), this.globalMessageQueue);
		return comp;
	}

	@Override
	public final List<ISubComponent<EventHandler<Event>, Event, Object>> getInstances() {
		return this.componentInstances;
	}

	@Override
	public final AtomicInteger getThreadCounter() {
		return this.threadCount;
	}

	@Override
	public final ExecutorService getExecutorService() {
		return this.executor;
	}

}
