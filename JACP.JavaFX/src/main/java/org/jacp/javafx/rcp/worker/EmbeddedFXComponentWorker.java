/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [FX2ComponentReplaceWorker.java]
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


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.lifecycle.PreDestroy;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;
import org.jacp.javafx.rcp.util.WorkerUtil;

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
public class EmbeddedFXComponentWorker extends AEmbeddedComponentWorker {

    private final Map<String, Node> targetComponents;
    private final AFXComponent component;
    private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;

    public EmbeddedFXComponentWorker(
            final Map<String, Node> targetComponents,
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
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

            while (!Thread.interrupted()) {
                this.component.lock();
                final IAction<Event, Object> myAction = this.component
                        .getNextIncomingMessage();
              //  System.err.println("WORKER: "+this);
                this.log(" //1.1.1.1.1// handle replace component BEGIN: "
                        + this.component.getContext().getName());

                final Node previousContainer = this.component.getRoot();
                final String currentTargetLayout = JACPContextImpl.class.cast(this.component.getContext()).getTargetLayout();
                final String currentExecutionTarget = JACPContextImpl.class.cast(this.component.getContext()).getExecutionTarget();
                // run code
                this.log(" //1.1.1.1.2// handle component: "
                        + this.component.getContext().getName());
                final Node handleReturnValue = WorkerUtil.prepareAndRunHandleMethod(
                        this.component, myAction);
                this.log(" //1.1.1.1.3// publish component: "
                        + this.component.getContext().getName());
                this.publish(this.component, myAction, this.targetComponents,
                        handleReturnValue, previousContainer,
                        currentTargetLayout, currentExecutionTarget);
                this.component.release();
            }
        } catch (final IllegalStateException e) {
            if (e.getMessage().contains("Not on FX application thread")) {
                throw new UnsupportedOperationException(
                        "Do not reuse Node components in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                        e);
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if(this.component.isBlocked())this.component.release();
        }
    }


    /**
     * publish handle result in application main thread
     *
     * @throws InterruptedException
     */
    private void publish(final AFXComponent component,
                         final IAction<Event, Object> action,
                         final Map<String, Node> targetComponents,
                         final Node handleReturnValue,
                         final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget)
            throws InterruptedException, ExecutionException {
        //final ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            setCacheHints(true, CacheHint.SPEED, component);
            // check if component was set to inactive, if so remove
            try {
                final FXComponentLayout layout = JACPContextImpl.class.cast(component.getContext()).getComponentLayout();
                if (component.getContext().isActive()) {
                    WorkerUtil.executeComponentViewPostHandle(handleReturnValue, component,
                            action);
                    EmbeddedFXComponentWorker.this.publishComponentValue(
                            component, targetComponents, layout,
                            previousContainer, currentTargetLayout, currentExecutionTarget);
                } else {
                    // unregister component
                    EmbeddedFXComponentWorker.this.removeComponentValue(
                            previousContainer);
                    // run teardown
                    FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                            component.getComponentHandle(), layout);
                }
            } catch (Exception e) {
                e.printStackTrace(); // TODO pass exception
            }
        });
    }


    private void removeComponentValue(final Node previousContainer) {
        if (previousContainer != null) {
            final Node parent = previousContainer.getParent();
            if (parent != null) {
                this.handleOldComponentRemove(parent, previousContainer);
            }
        }

    }


    /**
     * run in thread
     *
     * @param component, the component
     * @param targetComponents, all layoutTargets of the parent perspective
     * @param previousContainer, the previous container where the component.root was in
     * @param currentTargetLayout, the previous targetLayout
     * @param currentExecutionTarget, the current executionTarget
     */
    private void publishComponentValue(final AFXComponent component,
                                       final Map<String, Node> targetComponents,
                                       final FXComponentLayout layout,
                                       final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget) {

        if (previousContainer != null) {
            // check again if component was set to inactive (in postHandle), if
            // so remove
            if (component.getContext().isActive()) {
                final String newExecutionTarget = JACPContextImpl.class.cast(this.component.getContext()).getExecutionTarget();
                if (!currentExecutionTarget.equalsIgnoreCase(newExecutionTarget)) {
                    this.removeComponentValue(previousContainer);
                    this.handlePerspectiveChange(this.componentDelegateQueue,
                            component, layout);
                } else {
                    final String newTargetLayout = JACPContextImpl.class.cast(this.component.getContext()).getTargetLayout();
                    this.removeOldComponentValue(component, previousContainer,
                            currentTargetLayout, newTargetLayout);
                    this.checkAndHandleLayoutTargetChange(component, previousContainer,
                            currentTargetLayout, newTargetLayout, targetComponents);
                }

            } else {
                // unregister component
                this.removeComponentValue(previousContainer);
                // run teardown
                FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                        component.getComponentHandle(), layout);
            }

        }
    }

    /**
     * remove old component value from root node
     */
    private void removeOldComponentValue(final AFXComponent component,
                                         final Node previousContainer, final String currentTargetLayout, final String newTargetLayout) {
        final Node root = component.getRoot();
        // avoid remove/add when root component did not changed!
        if (!currentTargetLayout.equals(newTargetLayout)
                || root == null || root != previousContainer) {
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
     * @param component, the component
     * @param newTargetLayout, the new target layout id
     * @param targetComponents, the target components provided by parent perspective
     */
    private void executeLayoutTargetUpdate(final AFXComponent component,
                                           final String newTargetLayout, final Map<String, Node> targetComponents) {
        final Node validContainer = this.getValidContainerById(
                targetComponents, newTargetLayout);
        if(validContainer==null && component.getRoot()!=null) throw new InvalidParameterException("no targetLayout for layoutID: "+newTargetLayout+" found");
        if (validContainer != null) {
            this.handleLayoutTargetChange(component,
                    validContainer);
        } else {
            throw new IllegalArgumentException("no targetLayout " + newTargetLayout + " found");
        }
    }

  /*  @Override
    protected final void done() {
        AFXComponent component = null;
        try {
            component = this.get();
        } catch (final InterruptedException e) {
            logger.info("execution interrupted for component: " + this.component.getContext().getName());
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof InterruptedException) {
                logger.info("execution interrupted for component: " + this.component.getContext().getName());
            } else {
                e.printStackTrace();
            }

            // TODO add to error queue and restart thread if
            // messages in
            // queue
        } catch (final Exception e) {
            e.printStackTrace();
            // TODO add to error queue and restart thread if
            // messages in
            // queue
        } finally {
            if (component != null) setCacheHints(true, CacheHint.DEFAULT, component);
        }

    }
*/
}
