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
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.AFXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.ContextImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.*;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * Background Worker to execute components handle method in separate thread and
 * to replace or add the component result node; While the handle method is
 * executed in an own thread the postHandle method is executed in application
 * main thread.
 *
 * @author Andy Moncsek
 */
class EmbeddedFXComponentWorker extends AEmbeddedComponentWorker {

    private final Map<String, Node> targetComponents;
    private final AFXComponent component;
    private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;

    public EmbeddedFXComponentWorker(
            final Map<String, Node> targetComponents,
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
            final AFXComponent component) {
        super(component.getContext().getName());
        this.targetComponents = targetComponents;
        this.component = component;
        this.componentDelegateQueue = componentDelegateQueue;
        ShutdownThreadsHandler.registerThread(this);
    }

    @Override
    public void run() {
        try {
            this.component.lock();
            while (!Thread.interrupted()) {
                final Thread t = Thread.currentThread();
                try {
                    final Message<Event, Object> myAction = this.component
                            .getNextIncomingMessage();
                    final Node previousContainer = this.component.getRoot();
                    final ContextImpl contextImpl = ContextImpl.class.cast(this.component.getContext());
                    final String currentTargetLayout = contextImpl.getTargetLayout();
                    final String currentExecutionTarget = contextImpl.getExecutionTarget();
                    // run code
                    final Node handleReturnValue = WorkerUtil.prepareAndRunHandleMethod(
                            this.component, myAction);
                    this.publish(this.component, myAction, this.targetComponents,
                            handleReturnValue, previousContainer,
                            currentTargetLayout, currentExecutionTarget);
                } catch (final IllegalStateException e) {
                    if (e.getMessage().contains("Not on FX application thread")) {
                        t.getUncaughtExceptionHandler().uncaughtException(t, new UnsupportedOperationException(
                                "Do not reuse Node components in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                                e));
                    }
                } catch (InterruptedException e) {
                } catch (Exception e) {
                    t.getUncaughtExceptionHandler().uncaughtException(t, e);
                }
            }
            this.component.release();
        } finally {
            if (this.component.isBlocked()) this.component.release();
        }
    }


    /**
     * publish handle result in application main thread
     *
     * @throws InterruptedException
     */
    private void publish(final AFXComponent component,
                         final Message<Event, Object> action,
                         final Map<String, Node> targetComponents,
                         final Node handleReturnValue,
                         final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget)
            throws InterruptedException, ExecutionException {
        final Thread t = Thread.currentThread();
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            // check if component was set to inactive, if so remove
            try {
                final FXComponentLayout layout = ContextImpl.class.cast(component.getContext()).getComponentLayout();
                // check if was not deactivated in handle method
                if (component.getContext().isActive()) {
                    WorkerUtil.executeComponentViewPostHandle(handleReturnValue, component,
                            action);
                }
                // check if was not deactivated in post handle method
                if (component.getContext().isActive()) {
                    EmbeddedFXComponentWorker.this.publishComponentValue(
                            component, targetComponents, layout,
                            previousContainer, currentTargetLayout, currentExecutionTarget);
                    return;
                }
                shutDownComponent(component, layout, previousContainer, currentTargetLayout);
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
    private void publishComponentValue(final AFXComponent component,
                                       final Map<String, Node> targetComponents,
                                       final FXComponentLayout layout,
                                       final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget) {

        if (previousContainer != null) {
            final ContextImpl context = ContextImpl.class.cast(this.component.getContext());
            final String newExecutionTarget = context.getExecutionTarget();
            if (!currentExecutionTarget.equalsIgnoreCase(newExecutionTarget)) {
                this.shutDownComponent(component, layout, previousContainer, currentTargetLayout);
                // restore target execution
                component.getContext().setExecutionTarget(newExecutionTarget);
                this.handlePerspectiveChange(this.componentDelegateQueue,
                        component);
            } else {
                final String newTargetLayout = context.getTargetLayout();
                this.removeOldComponentValue(component, previousContainer, currentTargetLayout, newTargetLayout);
                this.checkAndHandleLayoutTargetChange(component, previousContainer,
                        currentTargetLayout, newTargetLayout, targetComponents);
            }

        }
    }

    private void shutDownComponent(final AFXComponent component, final FXComponentLayout layout, final Node previousContainer, final String currentTargetLayout) {

        final String parentId = component.getParentId();
        final Perspective<EventHandler<Event>, Event, Object> parentPerspective = PerspectiveRegistry.findPerspectiveById(parentId);
        if (parentPerspective != null) {
            // unregister component
            if(!this.removeComponentValue(previousContainer)) {
                clearTargetLayoutInPerspective(parentPerspective,currentTargetLayout);
            }
            parentPerspective.unregisterComponent(component);
        }
        TearDownHandler.shutDownFXComponent(component, layout);
    }

    private void clearTargetLayoutInPerspective(final Perspective<EventHandler<Event>, Event, Object> parentPerspective,final String currentTargetLayout) {
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
     * remove old component value from root node
     */
    private void removeOldComponentValue(final AFXComponent component,
                                         final Node previousContainer, final String currentTargetLayout, String newTargetLayout) {
        final Node root = component.getRoot();
        // avoid remove/add when root component did not changed!
        if (!currentTargetLayout.equalsIgnoreCase(newTargetLayout) || root == null || root != previousContainer) {
            // remove old view
            this.removeComponentValue(previousContainer);
        }
    }

    /**
     * add new component value to root node
     */
    private void checkAndHandleLayoutTargetChange(final AFXComponent component,
                                                  final Node previousContainer, final String currentTargetLayout, final String newTargetLayout, final Map<String, Node> targetComponents) {

        final Node root = component.getRoot();
        if (!currentTargetLayout.equals(newTargetLayout)) {
            executeLayoutTargetUpdate(component, newTargetLayout, targetComponents);
        } else if (root != null && root != previousContainer) {
            // add new view
            this.log(" //1.1.1.1.4// handle new component insert: "
                    + component.getContext().getName());
            WorkerUtil.handleViewState(root, true);
            executeLayoutTargetUpdate(component, newTargetLayout, targetComponents);
        }

    }

    /**
     * Performs target change of component or perspective
     *
     * @param component,        the component
     * @param newTargetLayout,  the new target layout id
     * @param targetComponents, the target components provided by parent perspective
     */
    private void executeLayoutTargetUpdate(final AFXComponent component,
                                           final String newTargetLayout, final Map<String, Node> targetComponents) {
        final Node validContainer = this.getValidContainerById(
                targetComponents, newTargetLayout);
        if (validContainer == null && component.getRoot() != null)
            throw new InvalidParameterException("no targetLayout for layoutID: " + newTargetLayout + " found");
        this.handleLayoutTargetChange(component,
                validContainer);
    }

    @Override
    public void cleanAfterInterrupt() {
        this.component.release();
        ShutdownThreadsHandler.unRegisterThread(this);
    }
}
