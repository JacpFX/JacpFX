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
import org.jacp.api.component.IComponent;
import org.jacp.javafx.rcp.context.JACPContextImpl;

import java.util.concurrent.BlockingQueue;

/**
 * The most abstract component, used to define components as well as
 * perspectives
 *
 * @author Andy Moncsek
 */

public abstract class AComponent implements
        IComponent<EventHandler<Event>, Event, Object> {

    protected volatile boolean started = false;
    private String localeID = "";
    private String resourceBundleLocation = "";
    protected JACPContextImpl context;
    protected volatile BlockingQueue<IAction<Event, Object>> globalMessageQueue;


    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isStarted() {
        return this.started;
    }

    @Override
    public void setStarted(boolean started) {
        this.started = started;
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


    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(String o) {
        return this.getContext().getId().compareTo(o);
    }
}
