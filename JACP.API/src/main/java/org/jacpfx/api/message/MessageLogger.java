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

package org.jacpfx.api.message;

/**
 * SPI for Message tracking/persisting in JacpFX applications
 * Created by Andy Moncsek on 13.07.15.
 */
public interface MessageLogger {

    /**
     * Log message when send by any compinent/perspective/workbench
     * @param m
     */
    void onSend(Message m);

    void handleActive(Message m);

    void handleInactive(Message m);

    void handleInCurrentPerspective(Message m);

    void delegate(Message m);

    void receive(Message m);
}
