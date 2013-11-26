/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [AFXSubComponent.java]
 * AHCP Project (http://jacp.googlecode.com/)
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
package org.jacpfx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.action.IAction;
import org.jacpfx.api.component.IComponentHandle;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.context.Context;
import org.jacpfx.rcp.context.JACPContextImpl;
import org.jacpfx.rcp.worker.AEmbeddedComponentWorker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * the AFXSubComponent is the basic component for all components
 *
 * @author Andy Moncsek
 */
public abstract class ASubComponent extends AComponent implements
        ISubComponent<EventHandler<Event>, Event, Object> {

    private volatile String parentId;

    private final Semaphore lock = new Semaphore(1);

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final BlockingQueue<IAction<Event, Object>> incomingMessage = new ArrayBlockingQueue<>(
            100000);


    private volatile IComponentHandle<?, Event, Object> component;


    private volatile AEmbeddedComponentWorker worker;


    /**
     * {@inheritDoc}
     */
    @Override
    public final void initEnv(final String parentId,
                              final BlockingQueue<IAction<Event, Object>> messageQueue) {
        this.parentId = parentId;
        this.globalMessageQueue = messageQueue;
        this.context = new JACPContextImpl(this.globalMessageQueue);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasIncomingMessage() {
        return !this.incomingMessage.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void putIncomingMessage(final IAction<Event, Object> action) {
        try {
            this.incomingMessage.put(action);
        } catch (final InterruptedException e) {
            logger.info("massage put failed:");
            //TODO handle exception global
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IAction<Event, Object> getNextIncomingMessage()throws InterruptedException{
        return this.incomingMessage.take();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isBlocked() {
        return lock.availablePermits() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void lock() {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            logger.info("lock interrupted.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void release() {
        lock.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getParentId() {
        return this.parentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Context<EventHandler<Event>, Event, Object> getContext() {
        return this.context;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final IComponentHandle<?, Event, Object> getComponent() {
        return component;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends IComponentHandle<?, Event, Object>> void setComponent(final X handle) {
        this.component = handle;
    }

    public synchronized void initWorker(AEmbeddedComponentWorker worker) {
        this.worker = worker;
        this.worker.start();
    }

    public synchronized void interruptWorker() {
        if(worker==null)return;
        if(worker.isAlive()) {
            worker.interrupt();
        }
        worker.cleanAfterInterrupt();
    }

}
