/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FX2ActionListener.java]
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
package org.jacp.javafx.rcp.action;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;

import java.util.concurrent.BlockingQueue;

/**
 * This class represents the JACP FX2 Event listener... this class can be
 * assigned to components, it reacts on actions and notifies other components in
 * JACP
 * 
 * @author Andy Moncsek
 */
public class FXActionListener implements EventHandler<Event>,
		IActionListener<EventHandler<Event>, Event, Object> {
	private final IAction<Event, Object> action;
	private final BlockingQueue<IAction<Event, Object>> globalMessageQueue;

	public FXActionListener(final IAction<Event, Object> action,
			final BlockingQueue<IAction<Event, Object>> globalMessageQueue) {
		this.action = action;
		this.globalMessageQueue = globalMessageQueue;
	}

	@Override
	public void notifyComponents(final IAction<Event, Object> action) {
		this.globalMessageQueue.add(action);
	}

	@Override
	public IAction<Event, Object> getAction() {
		return this.action;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventHandler<Event> getListener() {
		return this;
	}

	@Override
	public void handle(final Event t) {
		this.notifyComponents(new FXAction(action.getSourceId(), action.getTargetId(), action.getMessage(), t));
	}

	@Override
	public void performAction(final Event arg0) {
		this.handle(arg0);
	}

}
