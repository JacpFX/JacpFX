/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ActionListenerImpl.java]
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
package org.jacpfx.rcp.message;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.message.ActionListener;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.util.MessageLoggerService;

import java.util.Objects;
import java.util.concurrent.TransferQueue;

/**
 * This class represents the JACP FX2 Event listener... this class can be
 * assigned to component, it reacts on actions and notifies other component in
 * JACP
 * 
 * @author Andy Moncsek
 */
public class ActionListenerImpl<T extends Event> implements EventHandler<T>,
        ActionListener<Event, Object> {
	private final Message<Event, Object> action;
	private final TransferQueue<Message<Event, Object>> globalMessageQueue;

	public ActionListenerImpl(final Message<Event, Object> action,
                              final TransferQueue<Message<Event, Object>> globalMessageQueue) {
		this.action = action;
		this.globalMessageQueue = globalMessageQueue;
	}

	@Override
	public void notifyComponents(final Message<Event, Object> action) {
        Objects.requireNonNull(action,"message cannot be null");
        try {
			logAndPutMessage(action);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }
    }

	private void logAndPutMessage(Message<Event, Object> m ) throws InterruptedException {
		MessageLoggerService.getInstance().onSend(m);
		this.globalMessageQueue.transfer(m);
	}


	@Override
	public void handle(final Event t) {
		this.notifyComponents(new MessageImpl(action.getSourceId(), action.getTargetId(), action.getMessageBody(), t));
	}



}
