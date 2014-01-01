/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [TearDownHandler.java]
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
package org.jacpfx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.IStatelessCallabackComponent;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.workbench.IBase;
import org.jacpfx.rcp.component.AFXComponent;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.component.CallbackComponent;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.worker.AComponentWorker;
import org.jacpfx.rcp.worker.TearDownWorker;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles TearDown annotations on all components when application is closed.
 * 
 * @author Andy Moncsek
 * 
 */
public class TearDownHandler {
	private static IBase<EventHandler<Event>, Event, Object> rootWorkbench;
	private static final ExecutorService executor = Executors
			.newCachedThreadPool(new HandlerThreadFactory(
					"PerspectiveHandler:"));

	/**
	 * Register the parent workbench, from here all perspectives and component
	 * should be reachable.
	 * 
	 * @param rootWorkbench, the root workbench
	 */
	public static void registerBase(
			IBase<EventHandler<Event>, Event, Object> rootWorkbench) {
		TearDownHandler.rootWorkbench = rootWorkbench;
	}

	/**
	 * perform global teardown on all components. This method will cause all @TearDown
	 * annotated methods to be executed.
	 */
	public static void handleGlobalTearDown() {
		if (rootWorkbench == null)
			throw new UnsupportedOperationException("can't teardown workbench");
		final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = rootWorkbench
				.getPerspectives();
        if(perspectives==null) return;
		for (final IPerspective<EventHandler<Event>, Event, Object> perspective : perspectives) {
			// TODO ... teardown perspective itself
			final List<ISubComponent<EventHandler<Event>, Event, Object>> subcomponents = perspective
					.getSubcomponents();
            if(subcomponents!=null) {
                final List<ISubComponent<EventHandler<Event>, Event, Object>> handleAsync = new ArrayList<>();
                // TODO FIXME for teardow all parameters in PreDestroyed should be passed -- see init process
                for (final ISubComponent<EventHandler<Event>, Event, Object> component : subcomponents) {
                    if (CallbackComponent.class.isAssignableFrom(component.getClass())) {
                        handleAsync
                                .add(component);
                    }
                    else {
                        // run teardown in app thread
                        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                                component.getComponent());
                    }

                }
                if (!handleAsync.isEmpty())
                    handleAsyncTearDown(handleAsync);

            }

            FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                    perspective.getPerspective());

		}
		executor.shutdown();
	}

	/**
	 * executes all methods in ICallbackComponent, annotated with @OnTeardown
	 * outside application thread.
	 * 
	 * @param components, all components that should execute an async teardown
	 */
	@SafeVarargs
    public static void handleAsyncTearDown(
            ISubComponent<EventHandler<Event>, Event, Object>... components) {
		final List<ISubComponent<EventHandler<Event>, Event, Object>> handleAsync = new ArrayList<>();
        Collections.addAll(handleAsync, components);
		handleAsyncTearDown(handleAsync);
	}

	/**
	 * executes all methods in ICallbackComponent, annotated with @OnTeardown
	 * outside application thread.
	 * 
	 * @param components, all components that should execute an async teardown
	 */
	private static void handleAsyncTearDown(
            final List<ISubComponent<EventHandler<Event>, Event, Object>> components) {
		try {
			final Set<Future<Boolean>> set = new HashSet<>();
			for (final ISubComponent<EventHandler<Event>, Event, Object> component : components) {
				if(component instanceof IStatelessCallabackComponent){
					final IStatelessCallabackComponent<EventHandler<Event>, Event, Object>tmp = (IStatelessCallabackComponent<EventHandler<Event>, Event, Object>) component;
					final List<ISubComponent<EventHandler<Event>, Event, Object>> instances = tmp.getInstances();
					for(final ISubComponent<EventHandler<Event>, Event, Object> instance : instances) {
						set.add(executor.submit(new TearDownWorker(instance)));
					}
				}
				set.add(executor.submit(new TearDownWorker(component)));
			}
            awaitTermination(set);
		} catch (RejectedExecutionException e) {
			log("component teardown was not executed");
		}

	}

    public static void shutDownFXComponent(final AFXComponent component, final Object ...params) {
        // run teardown
        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                component.getComponent(), params);
        component.interruptWorker();
        component.initEnv(null, null);
        ComponentRegistry.removeComponent(component);
    }

    private static void awaitTermination(Set<Future<Boolean>> set) {
        // await termination
        for (final Future<Boolean> future : set) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log("error while handle TearDown");
                e.printStackTrace();
            }
        }
    }

    public static void shutDownAsyncComponent(final ASubComponent component, final Object ...params) {
        if(component instanceof IStatelessCallabackComponent){
            final Set<Future<Boolean>> set = new HashSet<>();
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object>tmp = (IStatelessCallabackComponent<EventHandler<Event>, Event, Object>) component;
            final List<ISubComponent<EventHandler<Event>, Event, Object>> instances = tmp.getInstances();
            for(final ISubComponent<EventHandler<Event>, Event, Object> instance : instances) {
                if(instance.isStarted())
                    set.add(executor.submit(new TearDownWorker(instance)));
            }
            awaitTermination(set);
            tmp.getExecutorService().shutdownNow();
            instances.clear();
            ComponentRegistry.removeComponent(component);
        } else {
            try {
                executor.submit(new TearDownWorker(component)).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            component.interruptWorker();
            component.initEnv(null, null);
            ComponentRegistry.removeComponent(component);
        }
    }

    public static void executePredestroy(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                component.getComponent());
    }

	private static void log(final String message) {
		if (Logger.getLogger(AComponentWorker.class.getName()).isLoggable(
				Level.FINE)) {
			Logger.getLogger(AComponentWorker.class.getName()).fine(
					">> " + message);
		}
	}
}
