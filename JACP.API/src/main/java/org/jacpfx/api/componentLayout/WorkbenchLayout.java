/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [WorkbenchLayout.java]
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
package org.jacpfx.api.componentLayout;

import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.api.util.Tupel;

/**
 * Defines the base layout of a workbench and the application.
 *
 * @param <C> defines the base component where others extend from
 * @author Andy Moncsek
 */
public interface WorkbenchLayout<C> extends BaseLayout<C> {

    /**
     * Check if menus are enabled.
     *
     * @return if menu is enable/disable
     */
    boolean isMenuEnabled();

    /**
     * Set menus to enabled state.
     *
     * @param enabled, true if menu is enabled
     */
    void setMenuEnabled(boolean enabled);

    /**
     * Set the size of the workbench.
     *
     * @param x, the initial X size of workbench
     * @param y, the initial Y size of the workbench
     */
    void setWorkbenchXYSize(int x, int y);

    /**
     * Returns a tuple defining the workbench size.
     *
     * @return the tuple containing the workbench size
     */
    Tupel<Integer, Integer> getWorkbenchSize();

    /**
     * Register multiple toolbars for the workbench
     * <p>
     * All toolbars are added with the same priority, thus the priority is given
     * by the order of registration.
     *
     * @param positions - NORTH, WEST, EAST, SOUTH
     */
    void registerToolBars(ToolbarPosition... positions);

    /**
     * Register a toolbar for the workbench
     * <p>
     * All toolbars are added with the same priority, thus the priority is given
     * by the order of registration.
     *
     * @param position - NORTH, WEST, EAST, SOUTH
     */
    void registerToolBar(final ToolbarPosition position);

    /**
     * Set the workbench style.
     *
     * @param style , the style of workbench
     * @param <S>,  the style is type of Enum
     */
    @SuppressWarnings("rawtypes")
    <S extends Enum> void setStyle(S style);

    /**
     * Returns the workbench style.
     *
     * @param <S>, the style is type of Enum
     * @return style
     */
    @SuppressWarnings("rawtypes")
    <S extends Enum> S getStyle();
}
