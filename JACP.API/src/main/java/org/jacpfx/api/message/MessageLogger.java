/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [MessageLogger.java]
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
     * @param m, the message
     */
    void onSend(final Message m);
    /**
     * Log message when found an active component
     * @param m, the message
     */
    void handleActive(final Message m);
    /**
     * Log message when found an inactive component
     * @param m, the message
     */
    void handleInactive(final Message m);
    /**
     * Log message when handle component in current perspective
     * @param m, the message
     */
    void handleInCurrentPerspective(final Message m);
    /**
     * Log message when message was delegate to an other perspective
     * @param m, the message
     */
    void delegate(final Message m);
    /**
     * Log message when the component is handled
     * @param m, the message
     */
    void receive(final Message m);
}
