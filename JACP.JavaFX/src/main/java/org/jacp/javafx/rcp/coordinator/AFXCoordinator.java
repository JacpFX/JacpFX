/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [AFX2Coordinator.java]
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
package org.jacp.javafx.rcp.coordinator;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IDelegateDTO;
import org.jacp.api.coordinator.ICoordinator;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * Observer handles messages and notifies correct components, the observer is
 * running in an own thread so that message handling can be done in background
 * 
 * @author Andy Moncsek
 */
public abstract class AFXCoordinator extends Thread implements
		ICoordinator<EventHandler<Event>, Event, Object> {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private volatile BlockingQueue<IAction<Event, Object>> messages = new ArrayBlockingQueue<>(
			100000);

	AFXCoordinator(String name) {
		super(name);
		ShutdownThreadsHandler.registerThread(this);
	}

	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			this.log(" observer thread size" + this.messages.size());

			IAction<Event, Object> action;
			try {
				action = this.messages.take();
			} catch (final InterruptedException e) {
				logger.info("queue in AFXCoordinator interrupted");
				break;
			}
			this.log(" handle message to: " + action.getTargetId());
			try {
				this.handleMessage(action.getTargetId(), action);
			} catch (UnsupportedOperationException e) {
				logger.info("UnsupportedOperationException in AFXCoordinator");
				e.printStackTrace();
			}
			this.log(" observer thread DONE");
		}
	}

	/**
	 * add message to delegate queue
	 * 
	 * @param target
	 * @param action
	 * @param queue
	 */
	final void delegateMessageToCorrectPerspective(
            final String target, final IAction<Event, Object> action,
            final BlockingQueue<IDelegateDTO<Event, Object>> queue) {
		queue.add(new DelegateDTO(target, action));
	}

	@Override
	public BlockingQueue<IAction<Event, Object>> getMessageQueue() {
		return this.messages;
	}

	void log(final String message) {
		this.logger.fine(message);
	}
}
