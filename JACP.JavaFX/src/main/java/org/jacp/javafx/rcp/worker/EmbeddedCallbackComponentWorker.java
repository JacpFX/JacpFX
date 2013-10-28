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
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;
import org.jacp.javafx.rcp.util.WorkerUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * This class handles running stateful background components
 *
 * @author Andy Moncsek
 */
class EmbeddedCallbackComponentWorker
        extends
        AEmbeddedComponentWorker {
    private final ISubComponent<EventHandler<Event>, Event, Object> component;
    private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue;

    public EmbeddedCallbackComponentWorker(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        super(component.getContext().getName());
        this.component = component;
        this.delegateQueue = delegateQueue;
        ShutdownThreadsHandler.registerThread(this);
    }

    // TODO check behavior when component set to active==false and other messages are in pipe
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                this.component.lock();
                final IAction<Event, Object> myAction = this.component
                        .getNextIncomingMessage();
                final JACPContextImpl context = JACPContextImpl.class.cast(this.component.getContext());

                String id = myAction.getSourceId();
                context.setReturnTarget(id);
                final String currentExecutionTarget = context.getExecutionTarget();
                final Object value = this.component.getComponentHandle().handle(myAction);
                final String targetId = context
                        .getReturnTargetAndClear();
                WorkerUtil.delegateReturnValue(this.component, targetId, value,
                        myAction);
                this.checkAndHandleTargetChange(this.component,
                        currentExecutionTarget);
                this.component.release();

            }
            WorkerUtil.runCallbackOnTeardownMethods(this.component);
        } catch (InterruptedException e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (final IllegalStateException e) {
            if (e.getMessage().contains("Not on FX application thread")) {
                throw new UnsupportedOperationException(
                        "Do not reuse Node components in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                        e);
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (this.component.isBlocked()) this.component.release();
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
    public void cleanAfterInterrupt() {
        this.component.release();
        ShutdownThreadsHandler.unRegisterThread(this);
    }

}
