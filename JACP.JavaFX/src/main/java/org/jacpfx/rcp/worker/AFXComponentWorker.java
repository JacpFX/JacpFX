/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [AFX2ComponentWorker.java]
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
package org.jacpfx.rcp.worker;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.component.IComponentHandle;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.exceptions.InvalidComponentMatch;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.context.JACPContextImpl;
import org.jacpfx.rcp.util.FXUtil;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * handles component methods in own thread;
 *
 * @author Andy Moncsek
 */
public abstract class AFXComponentWorker<T> extends Task<T> {


    public AFXComponentWorker() {
    }

    /**
     * find valid target component in perspective
     *
     * @param targetComponents, the target components provided by the parent perspective
     * @param id,               a target id
     * @return returns a target node by id
     */
    Node getValidContainerById(
            final Map<String, Node> targetComponents, final String id) {
        return targetComponents.get(id);
    }

    /**
     * checks if component started, if so run PostConstruct annotations
     *
     * @param component, the component
     */
    void runCallbackOnStartMethods(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
       // if (!component.isStarted()) {
            component.setStarted(true);
            component.getContext().setActive(true);
            initLocalization(component);
            handleContextInjection(component);
            FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponent());
     //   }

    }

    /**
     * Checks if component is in correct state.
     * @param component
     */
    void checkValidComponent(final ASubComponent component) {
        final IComponentHandle<?, Event, Object> handle = component.getComponent();
        if (handle == null) throw new InvalidComponentMatch("Component is not initialized correctly");
        if (component == null || component.getContext() == null || component.getContext().getId() == null)
            throw new InvalidComponentMatch("Component is in invalid state while initialisation:" + handle.getClass());
    }

    /**
     * Set Resource Bundle
     *
     * @param component, the component
     */
    private void initLocalization(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final String bundleLocation = component.getResourceBundleLocation();
        if (bundleLocation.isEmpty())
            return;
        final String localeID = component.getLocaleID();
        JACPContextImpl.class.cast(component.getContext()).setResourceBundle(ResourceBundle.getBundle(bundleLocation,
                FXUtil.getCorrectLocale(localeID)));

    }

    private void handleContextInjection(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IComponentHandle<?, Event, Object> handler = component.getComponent();
        FXUtil.performResourceInjection(handler, component.getContext());
    }


    void log(final String message) {
        if (Logger.getLogger(AFXComponentWorker.class.getName()).isLoggable(
                Level.FINE)) {
            Logger.getLogger(AFXComponentWorker.class.getName()).fine(
                    ">> " + message);
        }
    }


}
