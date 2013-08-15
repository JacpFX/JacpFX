/*
 * Copyright (C) 2010 - 2012.
 * AHCP Project (http://code.google.com/p/jacp)
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
 */
package org.jacp.demo.callbacks;

import javafx.event.Event;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Component;
import org.jacp.api.annotations.Resource;
import org.jacp.api.annotations.Stateless;
import org.jacp.demo.constants.GlobalConstants;
import org.jacp.demo.entity.ContactDTO;
import org.jacp.javafx.rcp.component.AStatelessCallbackComponent;
import org.jacp.javafx.rcp.component.CallbackComponent;
import org.jacp.javafx.rcp.context.JACPContext;

/**
 * The CreatorCallback creates contact data with random numbers
 * 
 * @author Andy Moncsek
 * 
 */
@Component(id = GlobalConstants.CallbackConstants.CALLBACK_CREATOR, name = "creatorCallback", active = false)
@Stateless
public class CreatorCallback implements CallbackComponent {
    @Resource
    private JACPContext context;
    @Override
    public Object handle(final IAction<Event, Object> action) {
        if (action.isMessageType(ContactDTO.class)) {
            // return all values to defined target
            context.setHandleTarget(GlobalConstants.cascade(GlobalConstants.PerspectiveConstants.DEMO_PERSPECTIVE, GlobalConstants.ComponentConstants.COMPONENT_TABLE_VIEW));
            waitAmount(100);
            System.out.println("THIS THREAD: "+Thread.currentThread()+" this:"+this);
            return ContentGenerator.createEntries((action.getTypedMessage(ContactDTO.class)));
        }
        return null;
    }

    /**
     * for demo purposes
     */
    private void waitAmount(final int amount) {
        try {
            Thread.sleep(amount);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

}
