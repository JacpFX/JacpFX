/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [FX2Util.java]
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
package org.jacpfx.rcp.worker;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.context.JACPContextImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.util.WorkerUtil;

import java.util.concurrent.BlockingQueue;

/**
 * This class handles running stateful background components
 *
 * @author Andy Moncsek
 */
public class CallbackComponentInitWorker
        extends
        AFXComponentWorker<ASubComponent> {
    private final ASubComponent component;
    private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue;
    private final Message<Event, Object> action;

    public CallbackComponentInitWorker(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final ASubComponent component, final Message<Event, Object> action) {
        this.component = component;
        this.delegateQueue = delegateQueue;
        this.action = action;
    }

    @Override
    protected ASubComponent call()
            throws Exception {
            this.component.lock();
            checkValidComponent(this.component);
            runCallbackOnStartMethods(this.component);
            final Message<Event, Object> myAction = this.action;
            final JACPContextImpl context = JACPContextImpl.class.cast(this.component.getContext());
            context.setReturnTarget(myAction.getSourceId());
            final String currentExecutionTarget = context.getExecutionTarget();
            final Object value = this.component.getComponent().handle(myAction);
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

    private void handleComponentShutdown(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if (!component.getContext().isActive()) {
            component.setStarted(false);
            final String parentId = component.getParentId();
            final IPerspective<EventHandler<Event>, Event, Object> parentPerspctive = PerspectiveRegistry.findPerspectiveById(parentId);
            if(parentPerspctive!=null)parentPerspctive.unregisterComponent(component);
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
            final ISubComponent<EventHandler<Event>, Event, Object> comp,
            final String currentExecutionTarget) {
        final String targetNew = JACPContextImpl.class.cast(comp.getContext()).getExecutionTarget();
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
            this.log("Exception in CallbackComponent INIT Worker, Thread interrupted: "
                    + e.getMessage());
            // TODO add to error queue and restart thread if
            // messages in
            // queue
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        } finally {
            this.component.release();
        }
    }

}
