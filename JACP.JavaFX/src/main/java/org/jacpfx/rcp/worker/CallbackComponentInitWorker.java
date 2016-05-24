/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [CallbackComponentInitWorker.java]
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
import org.jacpfx.api.annotations.method.OnAsyncMessage;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.util.WorkerUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles running stateful background component
 *
 * @author Andy Moncsek
 */
public class CallbackComponentInitWorker
        extends
        AComponentWorker<ASubComponent> {
    private final ASubComponent component;
    private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> delegateQueue;
    private final Message<Event, Object> action;
    private final Map<Class, Method> asyncMethodMap;
    public CallbackComponentInitWorker(
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final ASubComponent component, final Message<Event, Object> action) {
        this.component = component;
        this.delegateQueue = delegateQueue;
        this.action = action;
        final ComponentHandle<?, Event, Object> handle = component.getComponent();
        asyncMethodMap = Stream.of(handle.getClass().getMethods()).
                filter(method -> method.isAnnotationPresent(OnAsyncMessage.class)).
                collect(Collectors.toMap(method -> method.getAnnotation(OnAsyncMessage.class).value(), p -> p));
    }

    @Override
    protected ASubComponent call()
            throws Exception {
            this.component.lock();
            checkValidComponent(this.component);
            runCallbackOnStartMethods(this.component);
            final Message<Event, Object> myAction = this.action;
            final InternalContext context = InternalContext.class.cast(this.component.getContext());
            context.updateReturnTarget(myAction.getSourceId());
            final String currentExecutionTarget = context.getExecutionTarget();
            final Object value = handleAsyncMessage(myAction,component.getComponent(),myAction.getMessageBody().getClass());//this.component.getComponent().handle(myAction);
            final String targetId = context
                    .getReturnTargetAndClear();
            WorkerUtil.delegateReturnValue(this.component, targetId, value,
                    myAction);
            this.checkAndHandleTargetChange(this.component,
                    currentExecutionTarget);
            this.component.initWorker(new EmbeddedCallbackComponentWorker( this.delegateQueue,this.component));
            handleComponentShutdown(this.component);
        return this.component;
    }

    private Object handleAsyncMessage(Message<Event, Object> message, Object componentHandle, Class<?> messageType) {
        Object value = null;
        final Method asyncMethod = asyncMethodMap.get(messageType);
        if (asyncMethod != null) {
            value = FXUtil.invokeMethod(OnAsyncMessage.class, asyncMethod, componentHandle, message);
        }
        return value;
    }

    private void handleComponentShutdown(final SubComponent<EventHandler<Event>, Event, Object> component) {
        final JacpContext<EventHandler<Event>, Object> context = component.getContext();
        if (!context.isActive()) {
            component.setStarted(false);
            TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(component));
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
        final String targetNew = InternalContext.class.cast(comp.getContext()).getExecutionTarget();
        if (!targetNew.equals(currentExecutionTarget)) {
            if (!component.getContext().isActive())
                throw new UnsupportedOperationException(
                        "CallbackComponent may be moved or set to inactive but not both");
            WorkerUtil.changeComponentTarget(this.delegateQueue, comp);
        }
    }

    @Override
    protected final void done() {
        final Thread t = Thread.currentThread();
        try {
            this.get();
        } catch (final Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        } finally {
            this.component.release();
        }
    }

}
