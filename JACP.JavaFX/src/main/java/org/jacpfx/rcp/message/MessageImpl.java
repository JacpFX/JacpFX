/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [FX2Action.java]
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
import org.jacpfx.api.message.Message;

/**
 * represents an message which is fired by an component, has a target and a
 * message targeting the component itself or an other component
 * 
 * @author Andy Moncsek
 */
public final class MessageImpl implements Message<Event, Object> {

	private Object message;
	private final String sourceId;
	private final Event event;
	private String target;

	public MessageImpl(final String sourceId) {
		this.sourceId = sourceId;
		this.event = null;
	}
	
	public MessageImpl(final String sourceId, final Event event) {
		this.sourceId = sourceId;
		this.event = event;
	}

	public MessageImpl(final String sourceId, final Object message) {
		this.sourceId = sourceId;
		this.setMessageBody(message);
		this.event = null;
	}

	public MessageImpl(final String sourceId, final String targetId,
                       final Object message, final Event event) {
		this.sourceId = sourceId;
		this.target = targetId;
		this.event = event;
		this.setMessageBody(message);
	}
	
	@Override
	public void setMessageBody(final Object message) {
		this.message = message;
		this.target = this.target != null ? this.target : this.sourceId;
	}

	@Override
	public void addMessage(final String targetId, final Object message) {
		this.target = targetId;
		this.message = message;
	}

	@Override
	public Object getMessageBody() {
		return this.message;
	}

	@Override
	public String getSourceId() {
		return this.sourceId;
	}

	@Override
	public Event getSourceEvent() {
		return this.event;
	}

	@Override
	public Message<Event, Object> clone() {
		return new MessageImpl(this.sourceId,this.target, this.message, this.event);
	}

	@Override
	public String getTargetId() {
		return this.target;
	}

    @Override
    public <T> boolean isMessageBodyTypeOf(final Class<T> clazz) {
        return clazz.isAssignableFrom(this.message.getClass());
    }
    @Override
    public <T> T getTypedMessageBody(final Class<T> clazz) {
        return clazz.cast(this.message);
    }
    @Override
    public boolean messageBodyEquals(Object object) {
        return object.equals(this.message);
    }

}
