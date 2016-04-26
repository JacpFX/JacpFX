/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [MessageDelegatorImpl.java]
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

package org.jacpfx.rcp.delegator;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.ComponentBase;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.concurrency.FXWorker;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

/**
 * Created by Andy Moncsek on 10.12.13.
 */
public class MessageDelegatorImpl extends Thread implements
        org.jacpfx.api.delegator.MessageDelegator<EventHandler<Event>, Event, Object> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private final TransferQueue<DelegateDTO<Event, Object>> messageDelegateQueue = new LinkedTransferQueue<>();

    public MessageDelegatorImpl() {
        super("MessageDelegatorImpl");
        ShutdownThreadsHandler.registerThread(this);
    }

    @Override
    public final void run() {
        while (!Thread.interrupted()) {
            try {
                final DelegateDTO<Event, Object> dto = this.messageDelegateQueue.take();
                this.handleCall(dto.getTarget(), dto.getMessage());
            } catch (final InterruptedException e) {
                logger.info("queue in ComponentDelegator interrupted");
                break;
            } catch (final ExecutionException e) {
                logger.info("queue in ComponentDelegator interrupted");
                e.printStackTrace();
            }

        }
    }



    private void handleCall(final String targetId,
                                     final Message<Event, Object> message) throws ExecutionException, InterruptedException {
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(FXUtil.getTargetPerspectiveId(targetId));
        if(perspective==null) throw new ComponentNotFoundException("no perspective for message : "+targetId+ " found");
        checkPerspectiveAndInit(perspective);
        perspective.getMessageQueue().put(message);
    }

    private void checkPerspectiveAndInit(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) throws ExecutionException, InterruptedException {
        if(perspective.getContext().isActive()) return;
        initPerspective(perspective);
    }

    private void initPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) throws ExecutionException, InterruptedException {
        perspective.updatePositions(1,1);
        FXWorker.invokeOnFXThreadAndWait(() -> this.perspectiveHandler.initComponent(new MessageImpl(perspective.getContext().getId(), perspective
                .getContext().getId(), "init", null), perspective));
    }


    @Override
    public TransferQueue<DelegateDTO<Event, Object>> getMessageDelegateQueue() {
        return this.messageDelegateQueue;
    }

    @Override
    public <P extends ComponentBase<EventHandler<Event>, Object>> void setPerspectiveHandler(ComponentHandler<P, Message<Event, Object>> handler) {
              this.perspectiveHandler = (ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;
    }
}
