/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXWorkbench.java]
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

package org.jacpfx.rcp.workbench;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 30.08.13
 * Time: 10:00
 * This interface represents a JACP Workbench which should be implemented to create a workbench.
 */
public interface FXWorkbench extends Injectable {

    /**
     * Handle menu and bar entries created
     * @param layout, the component layout
     */
    void postHandle(final FXComponentLayout layout);

    /**
     * JavaFX2 specific initialization method to create a workbench instance
     *
     * @param action, the initial event
     * @param layout, the workbench layout
     * @param stage, the JavaFX stage
     */
    void handleInitialLayout(
            final Message<Event, Object> action,
            final WorkbenchLayout<Node> layout, final Stage stage);

}
