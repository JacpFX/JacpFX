/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXPerspective.java]
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

package org.jacpfx.rcp.perspective;

import javafx.event.Event;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.08.13
 * Time: 10:10
 * Defines a perspective to implement in JacpFX
 */
public interface FXPerspective extends Injectable {

    /**
     * Handle perspective method to initialize the perspective and the layout.
     *
     * @param message            ; the message triggering the method
     * @param perspectiveLayout ,  the layout handler defining the perspective
     */
    void handlePerspective(Message<Event, Object> message,
                                              final PerspectiveLayout perspectiveLayout);
}
