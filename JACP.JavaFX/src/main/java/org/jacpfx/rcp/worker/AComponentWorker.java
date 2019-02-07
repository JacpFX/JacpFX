/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AComponentWorker.java]
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
package org.jacpfx.rcp.worker;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.exceptions.InvalidComponentMatch;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.util.ComponentUtil;
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
public abstract class AComponentWorker<T> extends Task<T> {


    public AComponentWorker() {
    }

    /**
     * find valid target component in perspective
     *
     * @param targetComponents, the target component provided by the parent perspective
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
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        ComponentUtil.activateComponent(component);
        initLocalization(component);
        handleContextInjection(component);
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponent());
    }

    /**
     * Checks if component is in correct state.
     *
     * @param component
     */
    void checkValidComponent(final ASubComponent component) {
        if (component == null || component.getContext() == null || component.getContext().getId() == null)
            throw new InvalidComponentMatch("Component is in invalid state while initialisation:");
        final ComponentHandle<?, Event, Object> handle = component.getComponent();
        if (handle == null) throw new InvalidComponentMatch("Component is not initialized correctly");

    }

    /**
     * Set Resource Bundle
     *
     * @param component, the component
     */
    private void initLocalization(final SubComponent<EventHandler<Event>, Event, Object> component) {
        final String bundleLocation = component.getResourceBundleLocation();
        if (bundleLocation.isEmpty())
            return;
        final String localeID = component.getLocaleID();
        InternalContext.class.cast(component.getContext()).setResourceBundle(ResourceBundle.getBundle(bundleLocation,
                FXUtil.getCorrectLocale(localeID)));

    }

    private void handleContextInjection(final SubComponent<EventHandler<Event>, Event, Object> component) {
        final ComponentHandle<?, Event, Object> handler = component.getComponent();
        FXUtil.performResourceInjection(handler, component.getContext());
    }


    void log(final String message) {
        if (Logger.getLogger(AComponentWorker.class.getName()).isLoggable(
                Level.FINE)) {
            Logger.getLogger(AComponentWorker.class.getName()).finest(
                    ">> " + message);
        }
    }


}
