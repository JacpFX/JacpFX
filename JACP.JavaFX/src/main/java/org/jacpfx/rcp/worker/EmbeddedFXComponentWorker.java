/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [EmbeddedFXComponentWorker.java]
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


import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.method.OnAsyncMessage;
import org.jacpfx.api.annotations.method.OnMessage;
import org.jacpfx.api.component.ComponentView;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.exceptions.NonUniqueComponentException;
import org.jacpfx.api.message.Message;
import org.jacpfx.concurrency.FXWorker;
import org.jacpfx.rcp.component.EmbeddedFXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.*;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Background Worker to execute component handle method in separate thread and
 * to replace or add the component result node; While the handle method is
 * executed in an own thread the postHandle method is executed in application
 * main thread.
 *
 * @author Andy Moncsek
 */
class EmbeddedFXComponentWorker extends AEmbeddedComponentWorker {

    private final Map<String, Node> targetComponents;
    private final EmbeddedFXComponent component;
    private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
    private final Map<Class, Method> syncMethodMap;
    private final Map<Class, Method> asyncMethodMap;

    public EmbeddedFXComponentWorker(
            final Map<String, Node> targetComponents,
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
            final EmbeddedFXComponent component) {
        super(component.getContext().getId());
        this.targetComponents = targetComponents;
        this.component = component;
        this.componentDelegateQueue = componentDelegateQueue;
        ShutdownThreadsHandler.registerThread(this);
        ComponentView<Node, Event, Object> handle = this.component.getComponentViewHandle();
        syncMethodMap = Stream.of(handle.getClass().getMethods()).
                filter(method -> method.isAnnotationPresent(OnMessage.class)).
                collect(Collectors.toMap(method -> method.getAnnotation(OnMessage.class).value(), p -> p));
        asyncMethodMap = Stream.of(handle.getClass().getMethods()).
                filter(method -> method.isAnnotationPresent(OnAsyncMessage.class)).
                collect(Collectors.toMap(method -> method.getAnnotation(OnAsyncMessage.class).value(), p -> p));

        // TODO check for duplicate OnMessage methods
    }


    @Override
    public final void run() {
        try {
            this.component.lock();
            while (!Thread.interrupted()) {
                handleComponentExecution(this.component, this.targetComponents);
            }
            this.component.release();
        } finally {
            if (this.component.isBlocked()) this.component.release();
        }
    }

    private void handleComponentExecution(final EmbeddedFXComponent component, final Map<String, Node> targetComponents) {
        final Thread t = Thread.currentThread();
        try {
            final Message<Event, Object> message = component.getNextIncomingMessage();
            MessageLoggerService.getInstance().receive(message);
            final Node previousContainer = component.getRoot();
            final InternalContext contextImpl = InternalContext.class.cast(component.getContext());
            final String currentTargetLayout = contextImpl.getTargetLayout();
            final String currentExecutionTarget = contextImpl.getExecutionTarget();
            final ComponentView<Node, Event, Object> componentViewHandle = component.getComponentViewHandle();
            final Class<?> messageType = message.getMessageBody().getClass();

            final Object value = handleAsyncMessage(message, componentViewHandle, messageType);

            handleSyncMessage(component, targetComponents, message, previousContainer, currentTargetLayout, currentExecutionTarget, messageType, value);

        } catch (final IllegalStateException e) {
            if (e.getMessage().contains("Not on FX application thread")) {
                t.getUncaughtExceptionHandler().uncaughtException(t, new UnsupportedOperationException(
                        "Do not reuse Node component in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                        e));
            }
        } catch (InterruptedException e) {
            if(!t.isInterrupted())t.interrupt();
        } catch (Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }

    }

    private void handleSyncMessage(EmbeddedFXComponent component, Map<String, Node> targetComponents, Message<Event, Object> message, Node previousContainer, String currentTargetLayout, String currentExecutionTarget, Class<?> messageType, Object value) throws InterruptedException, ExecutionException {
        final Method syncMethod = syncMethodMap.get(messageType);
        if (syncMethod != null) {
            publish(component, message, syncMethod, targetComponents,
                    value, previousContainer,
                    currentTargetLayout, currentExecutionTarget);
        }
    }

    private Object handleAsyncMessage(Message<Event, Object> message, Object componentHandle, Class<?> messageType) {
        Object value = null;
        final Method asyncMethod = asyncMethodMap.get(messageType);
        if (asyncMethod != null) {
            value = FXUtil.invokeMethod(OnAsyncMessage.class, asyncMethod, componentHandle, message);
        }
        return value;
    }

    /**
     * publish handle result in application main thread
     *
     * @throws InterruptedException
     */
    private void publish(final EmbeddedFXComponent component,
                         final Message<Event, Object> message,
                         final Method method,
                         final Map<String, Node> targetComponents,
                         final Object handleReturnValue,
                         final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget)
            throws InterruptedException, ExecutionException {
        final Thread t = Thread.currentThread();
        FXWorker.invokeOnFXThreadAndWait(() -> {
            // check if component was set to inactive, if so remove
            try {
                WorkerUtil.executeTypedComponentViewPostHandle(handleReturnValue, component,
                        message, method);

                EmbeddedFXComponentWorker.this.publishComponentValue(
                        component, targetComponents,
                        previousContainer, currentTargetLayout, currentExecutionTarget);


            } catch (Exception e) {
                t.getUncaughtExceptionHandler().uncaughtException(t, e);
            }
        });
    }


    private boolean removeComponentValue(final Node previousContainer) {
        if (previousContainer != null) {
            final Node parent = previousContainer.getParent();
            if (parent != null) {
                this.handleOldComponentRemove(parent, previousContainer);
                return true;

            }
        }
        return false;
    }


    /**
     * run in thread
     *
     * @param component,              the component
     * @param targetComponents,       all layoutTargets of the parent perspective
     * @param previousContainer,      the previous container where the component.root was in
     * @param currentTargetLayout,    the previous targetLayout
     * @param currentExecutionTarget, the current executionTarget
     */
    private void publishComponentValue(final EmbeddedFXComponent component,
                                       final Map<String, Node> targetComponents,
                                       final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget) {

        if (previousContainer != null) {
            final String id = component.getContext().getId();
            final InternalContext context = InternalContext.class.cast(component.getContext());
            final String newExecutionTarget = context.getExecutionTarget();
            if (!currentExecutionTarget.equalsIgnoreCase(newExecutionTarget)) {
                handleTargetChange(component, previousContainer, currentTargetLayout, id, newExecutionTarget);
            } else {
                final String newTargetLayout = context.getTargetLayout();
                this.checkAndHandleLayoutTargetChange(component, previousContainer,
                        currentTargetLayout, newTargetLayout, targetComponents);
            }

        }
    }

    private void handleTargetChange(EmbeddedFXComponent component, Node previousContainer, String currentTargetLayout, String id, String newExecutionTarget) {
        if (ComponentRegistry.findComponentByQualifiedId(newExecutionTarget, id) != null)
            throw new NonUniqueComponentException("perspective " + newExecutionTarget + " already contains a component with id: " + id);
        this.shutDownComponent(component, previousContainer, currentTargetLayout);
        // restore target execution
        final JacpContext contextTemp = component.getContext();
        contextTemp.setExecutionTarget(newExecutionTarget);
        // handle target outside current perspective
        WorkerUtil.changeComponentTarget(this.componentDelegateQueue, component);
    }

    private void shutDownComponent(final EmbeddedFXComponent component, final Node previousContainer, final String currentTargetLayout) {

        final Context context = Context.class.cast(component.getContext());
        final String parentId = context.getParentId();
        if (parentId == null) return;
        final FXComponentLayout layout = context.getComponentLayout();
        final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective = PerspectiveRegistry.findPerspectiveById(parentId);
        if (parentPerspective != null) {
            // unregister component
            if (!this.removeComponentValue(previousContainer)) {
                clearTargetLayoutInPerspective(parentPerspective, currentTargetLayout);
            }
        }
        TearDownHandler.shutDownFXComponent(component, parentId, layout);
    }

    private static void clearTargetLayoutInPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective, final String currentTargetLayout) {
        final PerspectiveLayout perspectiveLayout = PerspectiveUtil.getPerspectiveLayoutFromPerspective(parentPerspective);
        if (perspectiveLayout != null && currentTargetLayout != null) {
            final Node container = perspectiveLayout.getTargetLayoutComponents().get(currentTargetLayout);
            if (container != null) {
                FXUtil.getChildren(container).ifPresent(ObservableList::clear);
            }

        }
    }


    /**
     * add new component value to root node
     */
    private void checkAndHandleLayoutTargetChange(final EmbeddedFXComponent component,
                                                  final Node previousContainer, final String currentTargetLayout, final String newTargetLayout, final Map<String, Node> targetComponents) {

        if (!currentTargetLayout.equals(newTargetLayout)) {
            removeComponentValue(previousContainer);
            executeLayoutTargetUpdate(component, newTargetLayout, targetComponents);
        }

    }

    /**
     * Performs target change of component or perspective
     *
     * @param component,        the component
     * @param newTargetLayout,  the new target layout id
     * @param targetComponents, the target component provided by parent perspective
     */
    private void executeLayoutTargetUpdate(final EmbeddedFXComponent component,
                                           final String newTargetLayout, final Map<String, Node> targetComponents) {
        final Node validContainer = this.getValidContainerById(
                targetComponents, newTargetLayout);
        if (validContainer == null && component.getRoot() != null)
            throw new InvalidParameterException("no targetLayout for layoutID: " + newTargetLayout + " found");
        //Handle target change inside perspective.
        WorkerUtil.addComponentByType(validContainer, component);
    }

    @SuppressWarnings("Annotation")
    @Override
    public final void cleanAfterInterrupt() {
        this.component.release();
        ShutdownThreadsHandler.unRegisterThread(this);
    }
}
