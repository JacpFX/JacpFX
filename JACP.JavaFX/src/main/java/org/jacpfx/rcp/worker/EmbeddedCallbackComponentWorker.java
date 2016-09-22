/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [EmbeddedCallbackComponentWorker.java]
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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.exceptions.NonUniqueComponentException;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.MessageLoggerService;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.util.WorkerUtil;

import java.util.concurrent.BlockingQueue;

/**
 * This class handles running stateful background component
 *
 * @author Andy Moncsek
 */
class EmbeddedCallbackComponentWorker
        extends
        AEmbeddedComponentWorker {
    private final SubComponent<EventHandler<Event>, Event, Object> component;
    private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> delegateQueue;

    public EmbeddedCallbackComponentWorker(
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        super(component.getContext().getName());
        this.component = component;
        this.delegateQueue = delegateQueue;
        ShutdownThreadsHandler.registerThread(this);
    }

    // TODO check behavior when component set to active==false and other messages are in pipe
    @Override
    public void run() {
        final Thread t = Thread.currentThread();
        try {
            boolean wasExecuted = false;
            while (!Thread.interrupted()) {
                try {
                    final Message<Event, Object> myAction = this.component
                            .getNextIncomingMessage();
                    this.component.lock();
                    MessageLoggerService.getInstance().receive(myAction);
                    checkValidComponent(this.component);
                    wasExecuted = true;
                    final InternalContext context = InternalContext.class.cast(this.component.getContext());
                    context.updateReturnTarget(myAction.getSourceId());
                    final String currentExecutionTarget = context.getExecutionTarget();
                    final Object value = this.component.getComponent().handle(myAction);
                    final String targetId = context
                            .getReturnTargetAndClear();
                    WorkerUtil.delegateReturnValue(this.component, targetId, value,
                            myAction);
                    this.checkAndHandleTargetChange(this.component,
                            currentExecutionTarget);
                    this.component.release();
                    if (!component.getContext().isActive()) break;
                } catch (InterruptedException e) {
                    if(!t.isInterrupted())t.interrupt();
                } catch (final IllegalStateException e) {
                    if (e.getMessage().contains("Not on FX application thread")) {
                        t.getUncaughtExceptionHandler().uncaughtException(t, new UnsupportedOperationException(
                                "Do not reuse Node component in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                                e));
                    } else {
                        t.getUncaughtExceptionHandler().uncaughtException(t, e);
                    }
                } catch (Exception e) {
                    t.getUncaughtExceptionHandler().uncaughtException(t, e);
                }
            }
            if (wasExecuted) handleComponentShutdown(this.component);
        } finally {
            if (this.component.isBlocked()) this.component.release();
        }

    }

    private void handleComponentShutdown(final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (!component.isBlocked()) component.lock();
        try {
            final JacpContext<EventHandler<Event>, Object> context = component.getContext();
            final String parentId = context.getParentId();
            if(parentId!=null) {
                final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspctive = PerspectiveRegistry.findPerspectiveById(parentId);
                if (parentPerspctive != null) parentPerspctive.unregisterComponent(component);
            }
            TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(component));
        } finally {
            component.release();
        }
    }

    /**
     * check if target has changed
     *
     * @param comp,                   the component
     * @param currentExecutionTarget, the current execution target... which was valid before execution
     */
    private void checkAndHandleTargetChange(
            final SubComponent<EventHandler<Event>, Event, Object> comp,
            final String currentExecutionTarget) {
        final InternalContext context = InternalContext.class.cast(comp.getContext());
        final String newExecutionTarget = context.getExecutionTarget();
        if (!newExecutionTarget.equals(currentExecutionTarget)) {
            if (ComponentRegistry.findComponentByQualifiedId(newExecutionTarget, comp.getContext().getId())!=null)
                throw new NonUniqueComponentException("perspective " + newExecutionTarget + " already contains a component with id: " + comp.getContext().getId());
            if (!comp.getContext().isActive())
                throw new UnsupportedOperationException(
                        "CallbackComponent may be moved or set to inactive but not both");
            WorkerUtil.changeComponentTarget(this.delegateQueue, comp);
        }
    }

    @Override
    public void cleanAfterInterrupt() {
        this.component.release();
        ShutdownThreadsHandler.unRegisterThread(this);
    }

}
