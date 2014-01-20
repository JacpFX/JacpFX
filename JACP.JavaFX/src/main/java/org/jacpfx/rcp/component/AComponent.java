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
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.context.JacpContextImpl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The most abstract component, used to define components as well as
 * perspectives
 *
 * @author Andy Moncsek
 */

public abstract class AComponent implements
        Component<EventHandler<Event>, Object> {

    private volatile AtomicBoolean started =  new AtomicBoolean(false);
    private String localeID = "";
    private String resourceBundleLocation = "";
    protected JacpContextImpl context;
    protected volatile BlockingQueue<Message<Event, Object>> globalMessageQueue;


    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isStarted() {
        return this.started.get();
    }

    @Override
    public void setStarted(boolean started) {
        this.started.set(started);
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
     */
    @Override
    public void setLocaleID(String localeID) {
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

        AComponent that = (AComponent) o;

        if (started.get() != that.started.get()) return false;
        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (globalMessageQueue != null ? !globalMessageQueue.equals(that.globalMessageQueue) : that.globalMessageQueue != null)
            return false;
        return !(localeID != null ? !localeID.equals(that.localeID) : that.localeID != null) && !(resourceBundleLocation != null ? !resourceBundleLocation.equals(that.resourceBundleLocation) : that.resourceBundleLocation != null);

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
