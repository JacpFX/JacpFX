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
package org.jacp.javafx.rcp.worker;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.component.ASubComponent;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.WorkerUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

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
    private final IAction<Event, Object> action;

    public CallbackComponentInitWorker(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final ASubComponent component, final IAction<Event, Object> action) {
        this.component = component;
        this.delegateQueue = delegateQueue;
        this.action = action;
    }

    // TODO check behavior when component set to active==alse and other messages are in pipe
    @Override
    protected ASubComponent call()
            throws Exception {

        try {
            this.component.lock();
            runCallbackOnStartMethods(this.component);
            final IAction<Event, Object> myAction = this.action;
            final JACPContextImpl context = JACPContextImpl.class.cast(this.component.getContext());
            context.setReturnTarget(myAction.getSourceId());
            final String currentExecutionTarget = context.getExecutionTarget();
            final Object value = this.component.getComponentHandle().handle(myAction);
            final String targetId = context
                    .getReturnTargetAndClear();
            WorkerUtil.delegateReturnValue(this.component, targetId, value,
                    myAction);
            this.checkAndHandleTargetChange(this.component,
                    currentExecutionTarget);
            EmbeddedCallbackComponentWorker worker = new EmbeddedCallbackComponentWorker( this.delegateQueue,this.component);
            this.component.setWorker(worker);
            worker.start();
            runPostExecution(this.component);
            WorkerUtil.runCallbackOnTeardownMethods(this.component);
        } finally {
            this.component.release();
        }
        return this.component;
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
        try {
            this.get();
        } catch (final InterruptedException | ExecutionException e) {
            // FIXME: Handle Exceptions the right way
            e.printStackTrace();
        }
    }

}
