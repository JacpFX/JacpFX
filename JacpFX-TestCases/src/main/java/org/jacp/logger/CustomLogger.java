/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [Component.java]
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

package org.jacp.logger;

import org.jacpfx.api.message.Message;
import org.jacpfx.api.message.MessageLogger;

/**
 * Created by Andy Moncsek on 15.07.15.
 */
public class CustomLogger implements MessageLogger {
    @Override
    public void onSend(Message m) {
        System.out.println("onsend: "+m);
    }

    @Override
    public void handleActive(Message m) {
       System.out.println("handleActive: "+m);
    }

    @Override
    public void handleInactive(Message m) {
        System.out.println("handleInactive: "+m);
    }

    @Override
    public void handleInCurrentPerspective(Message m) {
        System.out.println("handleInCurrentPerspective: "+m);
    }

    @Override
    public void delegate(Message m) {
        System.out.println("delegate: "+m);
    }

    @Override
    public void receive(Message m) {
        System.out.println("receive: "+m);
    }
}
