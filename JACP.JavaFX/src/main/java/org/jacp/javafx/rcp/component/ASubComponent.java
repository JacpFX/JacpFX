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
package org.jacp.javafx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.context.Context;
import org.jacp.javafx.rcp.context.JACPContextImpl;

import java.util.ResourceBundle;
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

    private volatile String executionTarget = "";

    private volatile String parentId;

    private final Semaphore lock = new Semaphore(1);

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private volatile BlockingQueue<IAction<Event, Object>> incomingMessage = new ArrayBlockingQueue<>(
            1000);

    private JACPContextImpl context;


    private IComponentHandle<?, EventHandler<Event>, Event, Object> componentHandle;

    private String localeID = "";

    private String resourceBundleLocation = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public final void initEnv(final String parentId,
                              final BlockingQueue<IAction<Event, Object>> messageQueue) {
        this.parentId = parentId;
        this.globalMessageQueue = messageQueue;
        this.context = new JACPContextImpl(this.getId(), this.getName(), this.globalMessageQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getExecutionTarget() {
        return this.executionTarget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setExecutionTarget(final String target) {
        this.executionTarget = target;

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
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IAction<Event, Object> getNextIncomingMessage() {
        if (this.hasIncomingMessage()) {
            try {
                return this.incomingMessage.take();
            } catch (final InterruptedException e) {
                logger.info("massage take failed:");
            }
        }
        return null;
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
    @Override
    public final IComponentHandle<?, EventHandler<Event>, Event, Object> getComponentHandle() {
        return componentHandle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends IComponentHandle<?, EventHandler<Event>, Event, Object>> void setComponentHandle(final X handle) {
        this.componentHandle = handle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocaleID() {
        return localeID;
    }
    /**
     * {@inheritDoc}
     *//*
    @Override
	public void setLocaleID(String localeID) {
		super.checkPolicy(this.localeID, "Do Not Set document manually");
		this.localeID = localeID;
	}*/

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceBundleLocation() {
        return resourceBundleLocation;
    }
    /**
     * {@inheritDoc}
     *//*
    @Override
	public final void setResourceBundleLocation(String resourceBundleLocation) {
		super.checkPolicy(this.resourceBundleLocation, "Do Not Set document manually");
		this.resourceBundleLocation = resourceBundleLocation;
	}*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResourceBundle(final ResourceBundle resourceBundle) {
        initResourceToContext(resourceBundle);
    }

    protected void initResourceToContext(final ResourceBundle resourceBundle) {
        Context context = this.getContext();
        JACPContextImpl jContext = JACPContextImpl.class.cast(context);
        jContext.setResourceBundle(resourceBundle);
    }
}
