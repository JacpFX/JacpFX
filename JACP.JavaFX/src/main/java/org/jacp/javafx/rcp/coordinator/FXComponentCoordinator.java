/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [FX2ComponentCoordinator.java]
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
package org.jacp.javafx.rcp.coordinator;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IDelegateDTO;
import org.jacp.api.component.IComponent;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.coordinator.IComponentCoordinator;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.javafx.rcp.util.ComponentRegistry;
import org.jacp.javafx.rcp.util.FXUtil;

import java.util.concurrent.BlockingQueue;

/**
 * observe component actions and delegates to correct component
 *
 * @author Andy Moncsek
 */
public class FXComponentCoordinator extends AFXCoordinator implements
        IComponentCoordinator<EventHandler<Event>, Event, Object> {
    private IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> componentHandler;
    private BlockingQueue<IDelegateDTO<Event, Object>> delegateQueue;
    private String parentId;

    public FXComponentCoordinator() {
        super("FXComponentCoordinator");
    }

    @Override
    public void handleMessage(final String targetId,
                              final IAction<Event, Object> action) {
        // check if local message
        final boolean local = FXUtil.isLocalMessage(targetId);
        if (!local) {
            handleGlobalMessage(targetId,action);
        } else {
            // local message
            handleLocalMessage(targetId,action);
        }

    }
    /**
     * Handles local messages like id=componentId.
     */
    private void handleLocalMessage(final String targetId,
                                     final IAction<Event, Object> action) {
        final ISubComponent<EventHandler<Event>, Event, Object> component = getTargetComponent(targetId, action);
        final String targetPerspectiveId = component.getParentId();
        if (this.parentId.equals(targetPerspectiveId)) {
            this.log(" //1.1.1// component HIT: " + action.getTargetId());
            this.handleComponentHit(targetId, action, component);
        } else {
            // search target component in an other perspective
            this.delegateMessageToCorrectPerspective(targetId, action,
                    this.delegateQueue);
        }
    }

    /**
     * Handles global messages like perspectiveId.componentId.
     */
    private void handleGlobalMessage(final String targetId,
                                     final IAction<Event, Object> action) {
        final String targetPerspectiveId = FXUtil
                .getTargetPerspectiveId(targetId);
        if (parentId.equals(targetPerspectiveId)) {
            // target component is in current perspective
            handleMessageInCurrentPerspective(targetId, action);
        } else {
            // target component is in an other perspective
            delegateMessageToCorrectPerspective(targetId, action,
                    this.delegateQueue);
        }
    }

    /**
     * Returns the target component by targetId specified in action.
     * @param targetId
     * @param action
     * @return
     */
    private ISubComponent<EventHandler<Event>, Event, Object> getTargetComponent(final String targetId,
                                                                                 final IAction<Event, Object> action) {
        final ISubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentById(targetId);

        if (component == null) throw new UnsupportedOperationException(
                "invalid component id handling not supported yet. Source: "
                        + action.getSourceId() + " target: "
                        + action.getTargetId());
        return component;
    }

    /**
     * This method  assumes that the target is located in current perspective
     * @param targetId
     * @param action
     */
    private void handleMessageInCurrentPerspective(final String targetId,
                                                   final IAction<Event, Object> action) {
        final ISubComponent<EventHandler<Event>, Event, Object> component = getTargetComponent(targetId, action);
        this.log(" //1.1// component message to: " + action.getTargetId());
        this.log(" //1.1.1// component HIT: " + action.getTargetId());
        this.handleComponentHit(targetId, action, component);
    }

    /**
     * handle method if component was found in local context
     *
     * @param targetId
     * @param action
     * @param component
     */
    private void handleComponentHit(final String targetId,
                                    final IAction<Event, Object> action,
                                    final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if (component.isActive()) {
            this.log(" //1.1.1.1// component HIT handle ACTIVE: "
                    + action.getTargetId());
            this.handleActive(component, action);
        } // End if
        else {
            this.log(" //1.1.1.1// component HIT handle IN-ACTIVE: "
                    + action.getTargetId());
            this.handleInActive(component, action);
        } // End else
    }


    @Override
    public final <P extends IComponent<EventHandler<Event>, Event, Object>> void handleActive(
            final P component, final IAction<Event, Object> action) {
        this.log(" //1.1.1.1.1// component " + action.getTargetId()
                + " delegate to perspective: " + this.parentId);
        this.componentHandler.handleAndReplaceComponent(action,
                (ISubComponent<EventHandler<Event>, Event, Object>) component);

    }

    @Override
    public final <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActive(
            final P component, final IAction<Event, Object> action) {
        component.setActive(true);
        this.componentHandler.initComponent(action,
                (ISubComponent<EventHandler<Event>, Event, Object>) component);

    }

    @SuppressWarnings("unchecked")
    @Override
    public IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> getComponentHandler() {
        return this.componentHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(
            final IComponentHandler<P, IAction<Event, Object>> handler) {
        this.componentHandler = (IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>>) handler;

    }

    @Override
    public void setMessageDelegateQueue(
            final BlockingQueue<IDelegateDTO<Event, Object>> delegateQueue) {
        this.delegateQueue = delegateQueue;

    }

    @Override
    public void setParentId(final String parentId) {
        this.parentId = parentId;

    }
}
