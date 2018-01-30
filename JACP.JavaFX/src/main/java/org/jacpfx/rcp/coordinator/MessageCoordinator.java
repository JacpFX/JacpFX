/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [MessageCoordinator.java]
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

package org.jacpfx.rcp.coordinator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.ComponentBase;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.coordinator.Coordinator;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.delegator.DelegateDTOImpl;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.*;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * The Message Coordinator checks the message target and delegates the message to component/perspective for correct handling.
 * Created by Andy Moncsek on 09.12.13.
 */
public class MessageCoordinator extends Thread implements
        Coordinator<EventHandler<Event>, Event, Object> {
    private ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private final TransferQueue<DelegateDTO<Event, Object>> delegateQueue;
    private final TransferQueue<Message<Event, Object>> messages = new LinkedTransferQueue<>();
    private final String parentId;
    private final Launcher<?> launcher;
    private static final String seperator = ".";

    public MessageCoordinator(final String parentId,
                              final Launcher<?> launcher) {
        this(parentId, launcher, null, null);
    }

    public MessageCoordinator(final String parentId,
                              final Launcher<?> launcher, final TransferQueue<DelegateDTO<Event, Object>> delegateQueue, final ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler) {
        super("MessageCoordinator");
        ShutdownThreadsHandler.registerThread(this);
        this.parentId = parentId;
        this.launcher = launcher;
        this.delegateQueue = delegateQueue;
        this.perspectiveHandler = perspectiveHandler;
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                final Message<Event, Object> message = messages.take();
                handleMessage(message.getTargetId(), message);
                Thread.yield();
            } catch (InterruptedException e) {
                // this can happen on application shutdown
                break;
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }


    @Override
    public TransferQueue<Message<Event, Object>> getMessageQueue() {
        return this.messages;
    }

    @Override
    public void handleMessage(final String targetId, final Message<Event, Object> message) {
        final MessageCoordinatorExecutionResult result = executeMessageHandling(targetId, message);
        final Message<Event, Object> messageObject = result.getMessage();
        switch (result.getState()) {
            case HANDLE_ACTIVE:
                MessageLoggerService.getInstance().handleActive(message);
                handleActive(result.getTargetComponent(), messageObject);
                break;
            case HANDLE_INACTIVE:
                MessageLoggerService.getInstance().handleInactive(message);
                handleInActive(result.getTargetComponent(), result.getParentPerspective(), messageObject);
                break;
            case HANDLE_CURRENT_PERSPECTIVE:
                handleCurrentPerspective(result.getTargetId(), message);
                break;
            case DELEGATE:
                MessageLoggerService.getInstance().delegate(message);
                delegateMessageToCorrectPerspective(result.getDto());
                break;
            default:
                throw new ComponentNotFoundException("no valid component found for id: " + targetId + " found");
        }
    }

    private void handleCurrentPerspective(final String targetId, final Message<Event, Object> message) {
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        Platform.runLater(() -> this.perspectiveHandler
                .handleAndReplaceComponent(
                        message, perspective) // End runnable
        ); // End runlater
    }

    private void handleActive(final SubComponent<EventHandler<Event>, Event, Object> component, Message<Event, Object> message) {
        this.componentHandler.handleAndReplaceComponent(message, component);
    }

    private void handleInActive(final SubComponent<EventHandler<Event>, Event, Object> component, final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective, Message<Event, Object> message) {
        ComponentUtil.activateComponent(component);
        parentPerspective.addComponent(component);
        this.componentHandler.initComponent(message, component);
    }

    private void delegateMessageToCorrectPerspective(final DelegateDTOImpl dto) {
        final Thread t = Thread.currentThread();
        try {
            this.delegateQueue.transfer(dto);
        } catch (InterruptedException e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }
    }

    private MessageCoordinatorExecutionResult executeMessageHandling(final String targetId, final Message<Event, Object> message) {
        if (!FXUtil.isLocalMessage(targetId)) {
            // this must be a component message
            return handleGlobalComponentMessage(targetId, message);
        } else {
            // unclear if it is a component- or a perspective-message
            return findCorrectTargetAndProceed(targetId, message);
        }
    }

    private MessageCoordinatorExecutionResult findCorrectTargetAndProceed(final String targetId, final Message<Event, Object> message) {
        // 1. test if perspective itself
        if (parentId.equalsIgnoreCase(targetId)) {
            // this is a message to current perspective
            return new MessageCoordinatorExecutionResult(targetId, message, MessageCoordinatorExecutionResult.State.HANDLE_CURRENT_PERSPECTIVE);

        }
        // 2. check if it is an active component in registry, active component must have active perspective
        final SubComponent<EventHandler<Event>, Event, Object> targetComponent = ComponentRegistry.findComponentByQualifiedId(FXUtil.getQualifiedComponentId(parentId, targetId));
        if (null != targetComponent) {
            // found an active component
            return new MessageCoordinatorExecutionResult(targetComponent, message, MessageCoordinatorExecutionResult.State.HANDLE_ACTIVE);
        }
        // 3. check if it is a perspective, all perspective (even inactive ones are registerd)
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        if (null != perspective) {
            // this is a perspective
            // delegate message to perspective, mark in dto that it is a perspective
            return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(targetId, true, message), MessageCoordinatorExecutionResult.State.DELEGATE);

        }
        // 4. check if it is an inactive component in perspective
        boolean exists = PerspectiveRegistry.perspectiveContainsComponentIdInAnnotation(this.parentId, targetId);
        if (exists) {
            // create global id
            final String globalTarget = this.parentId +seperator+targetId;
            return createComponentInstanceAndRegister(globalTarget, message);
        }
        return new MessageCoordinatorExecutionResult(MessageCoordinatorExecutionResult.State.ERROR);
    }

    /**
     * Handles messages with fully qualified value like "parentId.componentId"
     *
     * @param targetId the fully qualified id
     * @param message  the message
     * @return a MessageCoordinatorExecutionResult with values "HANDLE_ACTIVE" , "HANDLE_INACTIVE", "DELEGATE", or "ERROR"
     */
    private MessageCoordinatorExecutionResult handleGlobalComponentMessage(final String targetId, final Message<Event, Object> message) {
        final String parentMessageId = FXUtil.getParentFromId(targetId);
        if (parentId.equalsIgnoreCase(parentMessageId)) {
            // this is a message to local component in current perspective
            return getTargetComponentInCurrentPerspective(targetId, message);
        } else {
            // this must be a message in different perspective
            return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(targetId, message), MessageCoordinatorExecutionResult.State.DELEGATE);
        }
    }


    /**
     * Returns the target component by targetId specified in message.
     *
     * @param targetId the fully qualified id
     * @param message  the message
     * @return a MessageCoordinatorExecutionResult  with value "HANDLE_ACTIVE"  or "HANDLE_INACTIVE"
     */
    private MessageCoordinatorExecutionResult getTargetComponentInCurrentPerspective(final String targetId,
                                                                                     final Message<Event, Object> message) {
        final SubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentByQualifiedId(targetId);
        if (null != component) {
            // component is active
            return new MessageCoordinatorExecutionResult(component, message, MessageCoordinatorExecutionResult.State.HANDLE_ACTIVE);
        } else {
            // start inactive component
            return createComponentInstanceAndRegister(targetId, message);
        }
    }

    /**
     * Creates a new component instance and registers it
     *
     * @param targetId the fully qualified id
     * @param message  the message
     * @return a MessageCoordinatorExecutionResult  with value "HANDLE_INACTIVE"
     */
    private MessageCoordinatorExecutionResult createComponentInstanceAndRegister(final String targetId, final Message<Event, Object> message) {
        final SubComponent<EventHandler<Event>, Event, Object> component = PerspectiveUtil.getInstance(this.launcher).createSubcomponentById(targetId);
        if (null == component) throw new ComponentNotFoundException(
                "invalid component id. Source: "
                        + message.getSourceId() + " target: "
                        + message.getTargetId());

        return findParentPerspectiveAndRegisterComponent(component, message, targetId);
    }

    private static MessageCoordinatorExecutionResult findParentPerspectiveAndRegisterComponent(final SubComponent<EventHandler<Event>, Event, Object> component, final Message<Event, Object> message, final String targetId) {
        final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective = PerspectiveRegistry.findPerspectiveById(FXUtil.getTargetPerspectiveId(targetId));
        if (null == parentPerspective)
            throw new ComponentNotFoundException("no valid perspective for component " + targetId + " found");
        parentPerspective.registerComponent(component);
        return new MessageCoordinatorExecutionResult(component, parentPerspective, message, MessageCoordinatorExecutionResult.State.HANDLE_INACTIVE);
    }


    @Override
    public <P extends ComponentBase<EventHandler<Event>, Object>> void setComponentHandler(ComponentHandler<P, Message<Event, Object>> handler) {
        this.componentHandler = (ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

    }

    @Override
    public <P extends ComponentBase<EventHandler<Event>, Object>> void setPerspectiveHandler(ComponentHandler<P, Message<Event, Object>> handler) {
        this.perspectiveHandler = (ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;
    }


    /**
     * first builder interface for the parent id
     */
    @FunctionalInterface
    public interface ParentIdBuilder {
        /**
         * add the parent id value
         *
         * @param parentId define the parent id of the coordinator
         * @return the next builder LauncherBuilder
         */
        LauncherBuilder parentId(final String parentId);
    }

    /**
     * second builder interface for the launcher
     */
    @FunctionalInterface
    public interface LauncherBuilder {
        /**
         * add the launcher reference
         *
         * @param launcher the launcher instance
         * @return the next builder DelegateQueueBuilder
         */
        DelegateQueueBuilder launcher(final Launcher<?> launcher);
    }

    /**
     * third builder interface for the delegateQueue
     */
    @FunctionalInterface
    public interface DelegateQueueBuilder {
        /**
         * add the delegate queue reference
         *
         * @param delegateQueue the delegate message queue reference
         * @return the next builder HandlerBuilder
         */
        HandlerBuilder delegateQueue(final TransferQueue<DelegateDTO<Event, Object>> delegateQueue);
    }

    /**
     * forth builder interface for the handler
     */
    @FunctionalInterface
    public interface HandlerBuilder {
        /**
         * the last step which creates the Coordinator instance
         *
         * @param perspectiveHandler the perspective handler reference
         * @return the MessageCoordinator instance
         */
        MessageCoordinator handler(final ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler);
    }

    /**
     * Build the MessageCoordinator object
     * @return the initial builder interface ParentIdBuilder
     */
    public static ParentIdBuilder build() {
        return parentId -> launcher -> delegateQueue -> handler -> new MessageCoordinator(parentId, launcher, delegateQueue, handler);
    }
}
