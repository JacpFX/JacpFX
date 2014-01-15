/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [DelegateDTOImpl.java]
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
package org.jacpfx.rcp.delegator;

import javafx.event.Event;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.message.DelegateDTO;

/**
 * DTO interface to transfer components to desired target
 * 
 * @author Andy Moncsek
 * 
 */
public class DelegateDTOImpl implements DelegateDTO<Event, Object> {
	private final String target;
	private final Message<Event, Object> action;
    private final boolean isPerspective;

	public DelegateDTOImpl(final String target, final Message<Event, Object> action) {
		this.target = target;
		this.action = action;
        this.isPerspective = false;
	}

    public DelegateDTOImpl(final String target, final boolean isPerspective, final Message<Event, Object> action) {
        this.target = target;
        this.action = action;
        this.isPerspective = isPerspective;
    }

	@Override
	public String getTarget() {
		return this.target;
	}

	@Override
	public Message<Event, Object> getMessage() {
		return this.action;
	}

    @Override
    public boolean isPerspective() {return isPerspective;}
}
