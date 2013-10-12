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
package org.jacp.javafx.rcp.worker;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;
import org.jacp.api.annotations.lifecycle.PostConstruct;
import org.jacp.api.annotations.lifecycle.PreDestroy;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.IUIComponent;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.component.AComponent;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;
import org.jacp.javafx.rcp.util.ThrowableWrapper;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
     * @param id, a target id
     * @return returns a target node by id
     */
    Node getValidContainerById(
            final Map<String, Node> targetComponents, final String id) {
        return targetComponents.get(id);
    }

    /**
     * find valid target and add type specific new component. Handles Container,
     * ScrollPanes, Menus and Bar Entries from user
     *
     * @param validContainer, a valid container where components root will be added
     * @param component, the component
     */
    void addComponentByType(
            final Node validContainer,
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component) {
        this.handleAdd(validContainer, component.getRoot());
        this.handleViewState(validContainer, true);

    }

    /**
     * enables component an add to container
     *
     * @param validContainer , a valid container where components root will be added
     * @param IUIComponent , the component
     */
    private void handleAdd(final Node validContainer, final Node IUIComponent) {
        if (validContainer != null && IUIComponent != null) {
            this.handleViewState(IUIComponent, true);
            final ObservableList<Node> children = FXUtil
                    .getChildren(validContainer);
            children.add(IUIComponent);
        }

    }


    /**
     * set visibility and enable/disable
     *
     * @param IUIComponent, a Node where to set the state
     * @param state, the boolean value of the state
     */
    void handleViewState(final Node IUIComponent,
                               final boolean state) {
        IUIComponent.setVisible(state);
        IUIComponent.setDisable(!state);
        IUIComponent.setManaged(state);
    }

    /**
     * delegate components handle return value to specified target
     *
     * @param comp, the component
     * @param targetId, the message target id
     * @param value, the message value
     * @param action, the action
     */
    void delegateReturnValue(
            final ISubComponent<EventHandler<Event>, Event, Object> comp,
            final String targetId, final Object value,
            final IAction<Event, Object> action) {
        if (value != null && targetId != null
                && !action.isMessage("init")) {
            final IActionListener<EventHandler<Event>, Event, Object> listener = comp.getContext()
                    .getActionListener(null);
            listener.notifyComponents(new FXAction(comp.getContext().getId(), targetId,
                    value, null));
        }
    }


    /**
     * Move component to new target in perspective.
     *
     * @param delegateQueue, the component delegate queue
     * @param component, the component
     */
    void changeComponentTarget(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final String targetId = JACPContextImpl.class.cast(component.getContext()).getExecutionTarget();
        final String parentIdOld = component.getParentId();
        final String parentId = FXUtil.getTargetParentId(targetId);
        if (!parentIdOld.equals(parentId)) {
            // delegate to perspective observer
            delegateQueue.add(component);

        }
    }

    /**
     * Runs the handle method of a componentView.
     *
     * @param component, the component
     * @param action, the current action
     * @return a returned node from component execution
     */
    Node prepareAndRunHandleMethod(
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final IAction<Event, Object> action) throws Exception {
        return component.getComponentViewHandle().handle(action);

    }



    /**
     * checks if component started, if so run PostConstruct annotations
     *
     * @param component, the component
     */
    void runCallbackOnStartMethods(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if (!component.isStarted()) {
            initLocalization(component);
            handleContextInjection(component);
            FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponentHandle());
        }

    }

    /**
     * Set Resource Bundle
     *
     * @param component, the component
     */
    private void initLocalization(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final String bundleLocation = component.getResourceBundleLocation();
        if (bundleLocation.equals(""))
            return;
        final String localeID = component.getLocaleID();
        JACPContextImpl.class.cast(component.getContext()).setResourceBundle(ResourceBundle.getBundle(bundleLocation,
                FXUtil.getCorrectLocale(localeID)));

    }

    private void handleContextInjection(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IComponentHandle<?, EventHandler<Event>, Event, Object> handler = component.getComponentHandle();
        FXUtil.performResourceInjection(handler, component.getContext());
    }


    /**
     * Check if component was not started yet an activate it.
     *
     * @param component, the component
     */
    void runPostExecution(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if (!component.isStarted())
            FXUtil.setPrivateMemberValue(AComponent.class, component,
                    FXUtil.ACOMPONENT_STARTED, true);
    }

    void log(final String message) {
        if (Logger.getLogger(AFXComponentWorker.class.getName()).isLoggable(
                Level.FINE)) {
            Logger.getLogger(AFXComponentWorker.class.getName()).fine(
                    ">> " + message);
        }
    }



}
