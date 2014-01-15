/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [FX2ActionListener.java]
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
package org.jacpfx.rcp.message;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.message.ActionListener;
import org.jacpfx.api.message.Message;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * This class represents the JACP FX2 Event listener... this class can be
 * assigned to components, it reacts on actions and notifies other components in
 * JACP
 * 
 * @author Andy Moncsek
 */
public class ActionListenerImpl<T extends Event> implements EventHandler<T>,
        ActionListener<Event, Object> {
	private final Message<Event, Object> action;
	private final BlockingQueue<Message<Event, Object>> globalMessageQueue;

	public ActionListenerImpl(final Message<Event, Object> action,
                              final BlockingQueue<Message<Event, Object>> globalMessageQueue) {
		this.action = action;
		this.globalMessageQueue = globalMessageQueue;
	}

	@Override
	public void notifyComponents(final Message<Event, Object> action) {
        Objects.requireNonNull(action,"message cannot be null");
        try {
            this.globalMessageQueue.put(action);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }
    }


	@Override
	public void handle(final Event t) {
		this.notifyComponents(new MessageImpl(action.getSourceId(), action.getTargetId(), action.getMessageBody(), t));
	}



}
