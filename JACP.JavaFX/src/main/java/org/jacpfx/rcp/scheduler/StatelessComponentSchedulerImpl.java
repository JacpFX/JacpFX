/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [StatelessComponentSchedulerImpl.java]
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
package org.jacpfx.rcp.scheduler;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.StatelessCallabackComponent;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.scheduler.StatelessComponentScheduler;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.worker.StateLessComponentRunWorker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatelessComponentSchedulerImpl implements
        StatelessComponentScheduler<EventHandler<Event>, Event, Object> {

    private final Launcher<?> launcher;

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();


    public StatelessComponentSchedulerImpl(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    @Override
/**
 * {@inheritDoc }
 */
    public final void incomingMessage(
            final Message<Event, Object> message,
            final StatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        // TODO avoid locking of whole code block

        // get active instance
        final SubComponent<EventHandler<Event>, Event, Object> comp = getActiveComponent(baseComponent);
        final List<SubComponent<EventHandler<Event>, Event, Object>> componentInstances = baseComponent
                .getInstances();
        final int currentSize = componentInstances.size();
        if (comp != null) {
            if (currentSize < AStatelessCallbackComponent.MAX_INCTANCE_COUNT) {
                // create new instance as buffer
                ComponentHandle<?, Event, Object> handle = baseComponent.getComponent();
                componentInstances.add(getCloneBean(baseComponent,handle.getClass()));
            } // End inner if
            // run component in thread
            instanceRun(baseComponent, comp, message);
        } // End if
        else {
            // check if new instances can be created
            if (currentSize < AStatelessCallbackComponent.MAX_INCTANCE_COUNT) {
                createInstanceAndRun(baseComponent, message);
            } // End if
            else {
                seekAndPutMessage(baseComponent, message);
            } // End else
        } // End else

    }

    /**
     * block component, put message to component's queue and run in thread
     *
     * @param baseComponent, the parent component
     * @param comp,          the child component
     * @param message,       the message
     */
    private void instanceRun(
            final StatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final SubComponent<EventHandler<Event>, Event, Object> comp,
            final Message<Event, Object> message) {
        comp.putIncomingMessage(message);
        // TODO switch to embedded worker!!
        baseComponent.getExecutorService().submit(new StateLessComponentRunWorker(
                comp, baseComponent));
    }

    /**
     * if max thread count is not reached and all available component instances
     * are blocked create a new one, block it an run in thread
     *
     * @param baseComponent, the parent component
     * @param message,       the current message
     */
    private void createInstanceAndRun(
            final StatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final Message<Event, Object> message) {
        ComponentHandle<?, Event, Object> handle = baseComponent.getComponent();
        lock.writeLock().lock();
        try {
            final StatelessCallabackComponent<EventHandler<Event>, Event, Object> comp = this
                    .getCloneBean(baseComponent,
                            handle.getClass());
            baseComponent.getInstances().add(comp);
            instanceRun(baseComponent, comp, message);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * {@inheritDoc }
     */
    public final <T extends StatelessCallabackComponent<EventHandler<Event>, Event, Object>, H extends ComponentHandle> StatelessCallabackComponent<EventHandler<Event>, Event, Object> getCloneBean(
            final T baseComponent,
            final Class<H> clazz) {
        final AStatelessCallbackComponent component = AStatelessCallbackComponent.class.cast(baseComponent);
        final Context context = (Context) component.getContext();
        return component.init(launcher.getBean(context.getParentId().concat(FXUtil.PATTERN_GLOBAL).concat(context.getId())), context);
    }

    /**
     * Returns a component instance that is currently not blocked.
     *
     * @param baseComponent, the parent component
     * @return an available subcomponent
     */
    private SubComponent<EventHandler<Event>, Event, Object> getActiveComponent(
            final StatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        int size = baseComponent
                .getInstances().size();
        final AtomicInteger threadCount = baseComponent.getThreadCounter();
        if (size == 0) return null;
        final SubComponent<EventHandler<Event>, Event, Object> comp = baseComponent
                .getInstances().get(getSeekValue(threadCount, size));

        return !comp.isBlocked() ? comp : null;
    }

    /**
     * seek to first running component in instance list and add message to queue
     * of selected component
     *
     * @param baseComponent, the root component
     * @param message,       the current message
     */
    private void seekAndPutMessage(
            final StatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final Message<Event, Object> message) {
        // if max count reached, seek through component and add
        // message to queue of oldest component
        final int currentSize = baseComponent.getInstances().size();
        final AtomicInteger threadCount = baseComponent.getThreadCounter();
        final SubComponent<EventHandler<Event>, Event, Object> comp = baseComponent
                .getInstances().get(getSeekValue(threadCount, currentSize));
        // put message to queue
        comp.putIncomingMessage(message);
    }

    private int getSeekValue(
            final AtomicInteger threadCount, final int currentSize) {
        final int seek = threadCount.incrementAndGet()
                % currentSize;
        threadCount.lazySet(seek);
        return seek;
    }

}
