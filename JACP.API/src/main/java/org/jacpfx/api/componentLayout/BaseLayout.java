/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [BaseLayout.java]
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
package org.jacpfx.api.componentLayout;

import javafx.scene.Node;
import org.jacpfx.api.util.ToolbarPosition;

import java.util.Map;

/**
 * Defines a bean containing the defined tool bars and the main menu
 *
 * @param <C> defines the base component where others extend from
 * @author Andy Moncsek
 */
public interface BaseLayout<C> {

    /**
     * Gets the registered tool bar.
     *
     * @param position the position
     * @return the registered tool bar
     */
    C getRegisteredToolBar(ToolbarPosition position);


    /**
     * Gets all registered tool bars.
     *
     * @return all registered tool bars
     */
    Map<ToolbarPosition, ? extends Node> getRegisteredToolBars();

    /**
     * Returns the application menu instance.
     *
     * @return the menu instance
     */
    C getMenu();

    /**
     * Gets the glass pane.
     *
     * @return the glass pane
     */
    C getGlassPane();

}
