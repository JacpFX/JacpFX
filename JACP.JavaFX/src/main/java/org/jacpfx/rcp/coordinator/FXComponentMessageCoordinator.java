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
package org.jacpfx.rcp.coordinator;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.coordinator.ICoordinator;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.PerspectiveUtil;

import java.util.concurrent.BlockingQueue;

/**
 * observe component actions and delegates to correct component
 *
 * @author Andy Moncsek
 */
public class FXComponentMessageCoordinator extends AFXCoordinator implements
        ICoordinator<EventHandler<Event>, Event, Object> {
    private IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private final BlockingQueue<IDelegateDTO<Event, Object>> delegateQueue;
    private final String parentId;
    private final Launcher<?> launcher;

    public FXComponentMessageCoordinator(final BlockingQueue<IDelegateDTO<Event, Object>> delegateQueue,
                                         final String parentId,
                                         final Launcher<?> launcher) {
        super("FXComponentCoordinator");
        this.delegateQueue = delegateQueue;
        this.parentId = parentId;
        this.launcher = launcher;
    }

    @Override
    public void handleMessage(final String targetId,
                              final Message<Event, Object> action) {
        // check if local message
        if (!FXUtil.isLocalMessage(targetId)) {
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
                                     final Message<Event, Object> action) {
        final ISubComponent<EventHandler<Event>, Event, Object> component = getTargetComponent(targetId, action);
        final String targetPerspectiveId = component.getParentId();
        if (this.parentId.equals(targetPerspectiveId)) {
            this.log(" //1.1.1// component HIT: " + action.getTargetId());
            this.handleComponentHit(action, component);
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
                                     final Message<Event, Object> action) {
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
     * Returns the target component by targetId specified in message.
     * @param targetId
     * @param action
     * @return
     */
    private ISubComponent<EventHandler<Event>, Event, Object> getTargetComponent(final String targetId,
                                                                                 final Message<Event, Object> action) {
        ISubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentById(targetId);
        if(component != null){
            // component is active
            return component;
        }else{
            // start inactive component
            component = PerspectiveUtil.getInstance(this.launcher).createSubcomponentById(targetId);
        }
        if (component == null) throw new ComponentNotFoundException(
                "invalid component id. Source: "
                        + action.getSourceId() + " target: "
                        + action.getTargetId());
        findParentPerspectiveAndRegisterComponent(component);
        return component;
    }

    private void findParentPerspectiveAndRegisterComponent(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IPerspective<EventHandler<Event>, Event, Object> currentPerspective = PerspectiveRegistry.findPerspectiveById(parentId);
        currentPerspective.registerComponent(component);
    }

    /**
     * This method  assumes that the target is located in current perspective
     * @param targetId
     * @param action
     */
    private void handleMessageInCurrentPerspective(final String targetId,
                                                   final Message<Event, Object> action) {
        final ISubComponent<EventHandler<Event>, Event, Object> component = getTargetComponent(targetId, action);
        this.log(" //1.1// component message to: " + action.getTargetId());
        this.log(" //1.1.1// component HIT: " + action.getTargetId());
        this.handleComponentHit(action, component);
    }

    /**
     * handle method if component was found in local context
     *
     * @param action
     * @param component
     */
    private void handleComponentHit(final Message<Event, Object> action,
                                    final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if (component.getContext().isActive() && component.isStarted()) {
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
            final P component, final Message<Event, Object> action) {
        this.log(" //1.1.1.1.1// component " + action.getTargetId()
                + " delegate to perspective: " + this.parentId);
        //noinspection unchecked
        this.componentHandler.handleAndReplaceComponent(action,
                (ISubComponent<EventHandler<Event>, Event, Object>) component);

    }

    @Override
    public final <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActive(
            final P component, final Message<Event, Object> action) {
        this.componentHandler.initComponent(action,
                (ISubComponent<EventHandler<Event>, Event, Object>) component);

    }

    @SuppressWarnings("unchecked")
    @Override
    public IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> getComponentHandler() {
        return this.componentHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(
            final IComponentHandler<P, Message<Event, Object>> handler) {
        this.componentHandler = (IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

    }

}
