/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ASubComponent.java]
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
package org.jacpfx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.ComponentBase;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.worker.AEmbeddedComponentWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * the ASubComponent is the basic component for all components
 *
 * @author Andy Moncsek
 */
public abstract class ASubComponent implements
        SubComponent<EventHandler<Event>, Event, Object> {


    private final Semaphore lock = new Semaphore(1);
    private final Logger componentLogger = Logger.getLogger(this.getClass().getName());
    private final BlockingQueue<Message<Event, Object>> incomingMessage = new LinkedBlockingQueue<>();
    private volatile ComponentHandle<?, Event, Object> component;
    private volatile AEmbeddedComponentWorker workerRef;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private String localeID = "";
    private String resourceBundleLocation = "";
    private Context context;
    protected TransferQueue<Message<Event, Object>> globalMessageQueue;


    /**
     * {@inheritDoc}
     */
    @Override
    public final void initEnv(final String parentId,
                              final TransferQueue<Message<Event, Object>> messageQueue) {
        this.globalMessageQueue = messageQueue;
        this.context = new JacpContextImpl(parentId, this.globalMessageQueue);
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
            this.componentLogger.info("massage put failed:");
            //TODO handle exception global
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Message<Event, Object> getNextIncomingMessage() throws InterruptedException {
        return this.incomingMessage.take();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isBlocked() {
        return this.lock.availablePermits() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void lock() {
        try {
            this.lock.acquire();
        } catch (final InterruptedException e) {
            this.componentLogger.info("lock interrupted.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void release() {
        this.lock.release();
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
        return this.component;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <X extends ComponentHandle<?, Event, Object>> void setComponent(final X handle) {
        this.component = handle;
    }

    public final void initWorker(final AEmbeddedComponentWorker worker) {
        workerRef = worker;
        workerRef.start();
    }

    public final void interruptWorker() {
        if (workerRef == null) return;
        if (workerRef.isAlive()) {
            workerRef.interrupt();
        }
        workerRef.cleanAfterInterrupt();
        workerRef = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isStarted() {
        return this.started.get();
    }

    @Override
    public final void setStarted(final boolean started) {
        this.started.set(started);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLocaleID() {
        return this.localeID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setLocaleID(final String localeID) {
        this.localeID = localeID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getResourceBundleLocation() {
        return this.resourceBundleLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setResourceBundleLocation(final String resourceBundleLocation) {
        this.resourceBundleLocation = resourceBundleLocation;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final int compareTo(final ComponentBase o) {
        return this.getContext().getId().compareTo(o.getContext().getId());
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final ASubComponent that = (ASubComponent) o;

        if (this.started.get() != that.started.get()) return false;
        if (this.context != null ? !this.context.equals(that.context) : that.context != null) return false;
        return !(this.globalMessageQueue != null ? !this.globalMessageQueue.equals(that.globalMessageQueue) : that.globalMessageQueue != null) && !(this.localeID != null ? !this.localeID.equals(that.localeID) : that.localeID != null) && !(this.resourceBundleLocation != null ? !this.resourceBundleLocation.equals(that.resourceBundleLocation) : that.resourceBundleLocation != null);

    }

    @Override
    public final int hashCode() {
        int result = (this.started.get() ? 1 : 0);
        result = 31 * result + (this.localeID != null ? this.localeID.hashCode() : 0);
        result = 31 * result + (this.resourceBundleLocation != null ? this.resourceBundleLocation.hashCode() : 0);
        result = 31 * result + (this.context != null ? this.context.hashCode() : 0);
        result = 31 * result + (this.globalMessageQueue != null ? this.globalMessageQueue.hashCode() : 0);
        return result;
    }

}
