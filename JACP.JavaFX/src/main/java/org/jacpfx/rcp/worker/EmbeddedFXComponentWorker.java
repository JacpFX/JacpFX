/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [FX2ComponentReplaceWorker.java]
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
package org.jacpfx.rcp.worker;


import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.exceptions.NonUniqueComponentException;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.EmbeddedFXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.*;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

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

    public EmbeddedFXComponentWorker(
            final Map<String, Node> targetComponents,
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
            final EmbeddedFXComponent component) {
        super(component.getContext().getName());
        this.targetComponents = targetComponents;
        this.component = component;
        this.componentDelegateQueue = componentDelegateQueue;
        ShutdownThreadsHandler.registerThread(this);
    }

    @SuppressWarnings({"FeatureEnvy", "Annotation"})
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
            final Message<Event, Object> myAction = component
                    .getNextIncomingMessage();
            final Node previousContainer = component.getRoot();
            final InternalContext contextImpl = InternalContext.class.cast(component.getContext());
            final String currentTargetLayout = contextImpl.getTargetLayout();
            final String currentExecutionTarget = contextImpl.getExecutionTarget();
            // run code
            final Node handleReturnValue = WorkerUtil.prepareAndRunHandleMethod(
                    component, myAction);
            this.publish(component, myAction, targetComponents,
                    handleReturnValue, previousContainer,
                    currentTargetLayout, currentExecutionTarget);
        } catch (final IllegalStateException e) {
            if (e.getMessage().contains("Not on FX application thread")) {
                t.getUncaughtExceptionHandler().uncaughtException(t, new UnsupportedOperationException(
                        "Do not reuse Node component in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                        e));
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }

    }


    /**
     * publish handle result in application main thread
     *
     * @throws InterruptedException
     */
    private void publish(final EmbeddedFXComponent component,
                         final Message<Event, Object> action,
                         final Map<String, Node> targetComponents,
                         final Node handleReturnValue,
                         final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget)
            throws InterruptedException, ExecutionException {
        final Thread t = Thread.currentThread();
        Thread.yield();
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            // check if component was set to inactive, if so remove
            try {
                final JacpContext context = component.getContext();

                // check if was not deactivated in handle method
                if (context.isActive()) {
                    WorkerUtil.executeComponentViewPostHandle(handleReturnValue, component,
                            action);
                }
                // check if was not deactivated in post handle method
                if (context.isActive()) {
                    EmbeddedFXComponentWorker.this.publishComponentValue(
                            component, targetComponents,
                            previousContainer, currentTargetLayout, currentExecutionTarget);
                } else {
                    shutDownComponent(component, previousContainer, currentTargetLayout);
                }

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
    @SuppressWarnings("FeatureEnvy")
    private void publishComponentValue(final EmbeddedFXComponent component,
                                       final Map<String, Node> targetComponents,
                                       final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget) {

        if (previousContainer != null) {
            final String id = component.getContext().getId();
            final InternalContext context = InternalContext.class.cast(component.getContext());
            final String newExecutionTarget = context.getExecutionTarget();
            if (!currentExecutionTarget.equalsIgnoreCase(newExecutionTarget)) {
                if (ComponentRegistry.findComponentByQualifiedId(newExecutionTarget, id) != null)
                    throw new NonUniqueComponentException("perspective " + newExecutionTarget + " already contains a component with id: " + id);
                this.shutDownComponent(component, previousContainer, currentTargetLayout);
                // restore target execution
                final JacpContext contextTemp = component.getContext();
                contextTemp.setExecutionTarget(newExecutionTarget);
                // handle target outside current perspective
                WorkerUtil.changeComponentTarget(this.componentDelegateQueue, component);
            } else {
                final String newTargetLayout = context.getTargetLayout();
                this.checkAndHandleLayoutTargetChange(component, previousContainer,
                        currentTargetLayout, newTargetLayout, targetComponents);
            }

        }
    }

    private void shutDownComponent(final EmbeddedFXComponent component, final Node previousContainer, final String currentTargetLayout) {

        final Context context = Context.class.cast(component.getContext());
        final String parentId = context.getParentId();
        if(parentId==null) return;
        final FXComponentLayout layout = context.getComponentLayout();
        final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective = PerspectiveRegistry.findPerspectiveById(parentId);
        if (parentPerspective != null) {
            // unregister component
            if (!this.removeComponentValue(previousContainer)) {
                clearTargetLayoutInPerspective(parentPerspective, currentTargetLayout);
            }
            parentPerspective.unregisterComponent(component);
        }
        TearDownHandler.shutDownFXComponent(component, parentId, layout);
    }

    private static void clearTargetLayoutInPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective, final String currentTargetLayout) {
        final PerspectiveLayout playout = PerspectiveUtil.getPerspectiveLayoutFromPerspective(parentPerspective);
        if (playout != null && currentTargetLayout != null) {
            final Node container = playout.getTargetLayoutComponents().get(currentTargetLayout);
            if (container != null) {
                final ObservableList<Node> children = FXUtil.getChildren(container);
                children.clear();
            }

        }
    }


    /**
     * add new component value to root node
     */
    private void checkAndHandleLayoutTargetChange(final EmbeddedFXComponent component,
                                                  final Node previousContainer, final String currentTargetLayout, final String newTargetLayout, final Map<String, Node> targetComponents) {

        final Node root = component.getRoot();
        if (!currentTargetLayout.equals(newTargetLayout)) {
            removeComponentValue(previousContainer);
            executeLayoutTargetUpdate(component, newTargetLayout, targetComponents);
        } else if (root != null && !root.equals(previousContainer)) {
            // add new view
            this.log(" //1.1.1.1.4// handle new component insert: "
                    + component.getContext().getName());
            removeComponentValue(previousContainer);
            WorkerUtil.handleViewState(root, true);
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
