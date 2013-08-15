/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [StatelessCallbackScheduler.java]
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
package org.jacp.javafx.rcp.scheduler;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.IStatelessCallabackComponent;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.scheduler.IStatelessComponentScheduler;
import org.jacp.javafx.rcp.component.AStatelessCallbackComponent;
import org.jacp.javafx.rcp.worker.StateLessComponentRunWorker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatelessCallbackScheduler implements
        IStatelessComponentScheduler<EventHandler<Event>, Event, Object> {

    private final Launcher<?> launcher;

    private static volatile ReadWriteLock lock = new ReentrantReadWriteLock();

    public StatelessCallbackScheduler(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    @Override
    public final void incomingMessage(
            final IAction<Event, Object> message,
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        // avoid locking of whole code block
        lock.writeLock().lock();
        try {
            // get active instance
            final ISubComponent<EventHandler<Event>, Event, Object> comp = this
                    .getActiveComponent(baseComponent);
            final List<ISubComponent<EventHandler<Event>, Event, Object>> componentInstances = baseComponent
                    .getInstances();
            if (comp != null) {
                if (componentInstances.size() < AStatelessCallbackComponent.MAX_INCTANCE_COUNT) {
                    // create new instance as buffer
                    IComponentHandle<?, EventHandler<Event>, Event, Object> handle = baseComponent.getComponentHandle();
                    componentInstances.add(this.getCloneBean(baseComponent,
                            handle.getClass()));
                } // End inner if
                // run component in thread
                this.instanceRun(baseComponent, comp, message);
            } // End if
            else {
                // check if new instances can be created
                if (componentInstances.size() < AStatelessCallbackComponent.MAX_INCTANCE_COUNT) {
                    this.createInstanceAndRun(baseComponent, message);
                } // End if
                else {
                    this.seekAndPutMessage(baseComponent, message);
                } // End else
            } // End else

        } finally {
            lock.writeLock().unlock();
        } // End synchronized
    }

    /**
     * block component, put message to component's queue and run in thread
     *
     * @param comp
     * @param message
     */
    private void instanceRun(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final ISubComponent<EventHandler<Event>, Event, Object> comp,
            final IAction<Event, Object> message) {
        comp.putIncomingMessage(message);
        baseComponent.getExecutorService().submit(new StateLessComponentRunWorker(
                comp, baseComponent));
    }

    /**
     * if max thread count is not reached and all available component instances
     * are blocked create a new one, block it an run in thread
     *
     * @param message
     */
    private void createInstanceAndRun(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final IAction<Event, Object> message) {
        IComponentHandle<?, EventHandler<Event>, Event, Object> handle = baseComponent.getComponentHandle();
        final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> comp = this
                .getCloneBean(baseComponent,
                        handle.getClass());
        baseComponent.getInstances().add(comp);
        this.instanceRun(baseComponent, comp, message);
    }

    @Override
    public final <T extends IStatelessCallabackComponent<EventHandler<Event>, Event, Object>, H extends IComponentHandle> IStatelessCallabackComponent<EventHandler<Event>, Event, Object> getCloneBean(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final Class<H> clazz) {
        return ((AStatelessCallbackComponent) baseComponent).init(this.launcher
                .getBean(clazz));
    }

    /**
     * Returns a component instance that is currently not blocked.
     *
     * @return
     */
    private ISubComponent<EventHandler<Event>, Event, Object> getActiveComponent(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        // TODO this solution is crappy and dangerous!
        for (final ISubComponent<EventHandler<Event>, Event, Object> comp : baseComponent
                .getInstances()) {
            if (!comp.isBlocked()) {
                return comp;
            } // End if
        } // End for

        return null;
    }

    /**
     * seek to first running component in instance list and add message to queue
     * of selected component
     *
     * @param message
     */
    private void seekAndPutMessage(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final IAction<Event, Object> message) {
        // if max count reached, seek through components and add
        // message to queue of oldest component
        final ISubComponent<EventHandler<Event>, Event, Object> comp = baseComponent
                .getInstances().get(this.getSeekValue(baseComponent));
        // put message to queue
        comp.putIncomingMessage(message);
    }

    private int getSeekValue(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        final AtomicInteger threadCount = baseComponent.getThreadCounter();
        final int seek = threadCount.incrementAndGet()
                % baseComponent.getInstances().size();
        threadCount.set(seek);
        return seek;
    }

}
