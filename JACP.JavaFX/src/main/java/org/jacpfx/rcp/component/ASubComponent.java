/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [AFXSubComponent.java]
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
package org.jacpfx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.Component;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.QueueSizes;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.worker.AEmbeddedComponentWorker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * the ASubComponent is the basic component for all components
 *
 * @author Andy Moncsek
 */
public abstract class ASubComponent  implements
        SubComponent<EventHandler<Event>, Event, Object> {

    private volatile String parentId;

    private final Semaphore lock = new Semaphore(1);

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final BlockingQueue<Message<Event, Object>> incomingMessage = new ArrayBlockingQueue<>(
            QueueSizes.COMPONENT_QUEUE_SIZE);


    private volatile ComponentHandle<?, Event, Object> component;


    private volatile AtomicReference<AEmbeddedComponentWorker> workerRef = new AtomicReference<>();

    private volatile AtomicBoolean started =  new AtomicBoolean(false);
    private String localeID = "";
    private String resourceBundleLocation = "";
    protected JacpContextImpl context;
    protected volatile BlockingQueue<Message<Event, Object>> globalMessageQueue;


    /**
     * {@inheritDoc}
     */
    @Override
    public final void initEnv(final String parentId,
                              final BlockingQueue<Message<Event, Object>> messageQueue) {
        this.parentId = parentId;
        this.globalMessageQueue = messageQueue;
        this.context = new JacpContextImpl(this.globalMessageQueue);
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
    public final void putIncomingMessage(final Message<Event, Object> action) {
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
    public final Message<Event, Object> getNextIncomingMessage()throws InterruptedException{
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
    public final JacpContext getContext() {
        return this.context;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final ComponentHandle<?, Event, Object> getComponent() {
        return component;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends ComponentHandle<?, Event, Object>> void setComponent(final X handle) {
        this.component = handle;
    }

    public void initWorker(AEmbeddedComponentWorker worker) {
        this.workerRef.set(worker);
        worker.start();
    }

    public void interruptWorker() {
        final AEmbeddedComponentWorker worker = workerRef.get();
        if(worker==null)return;
        if(worker.isAlive()) {
            worker.interrupt();
        }
        worker.cleanAfterInterrupt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isStarted() {
        return this.started.get();
    }

    @Override
    public final void setStarted(boolean started) {
        this.started.set(started);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLocaleID() {
        return localeID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setLocaleID(String localeID) {
        this.localeID = localeID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getResourceBundleLocation() {
        return resourceBundleLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setResourceBundleLocation(String resourceBundleLocation) {
        this.resourceBundleLocation = resourceBundleLocation;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int compareTo(Component o) {
        return this.getContext().getId().compareTo(o.getContext().getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASubComponent that = (ASubComponent) o;

        if (started.get() != that.started.get()) return false;
        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        return !(globalMessageQueue != null ? !globalMessageQueue.equals(that.globalMessageQueue) : that.globalMessageQueue != null) && !(localeID != null ? !localeID.equals(that.localeID) : that.localeID != null) && !(resourceBundleLocation != null ? !resourceBundleLocation.equals(that.resourceBundleLocation) : that.resourceBundleLocation != null);

    }

    @Override
    public int hashCode() {
        int result = (started.get() ? 1 : 0);
        result = 31 * result + (localeID != null ? localeID.hashCode() : 0);
        result = 31 * result + (resourceBundleLocation != null ? resourceBundleLocation.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (globalMessageQueue != null ? globalMessageQueue.hashCode() : 0);
        return result;
    }

}
