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

package org.jacpfx.rcp.components.workbench;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jacpfx.api.componentLayout.WorkbenchLayout;

/**
 * Created by Andy Moncsek on 12.02.15.
 * The WorkbenchDecorator defines the basic Layout of a workbench window with the root naodes, menu and toolBars positions.
 */
public interface WorkbenchDecorator {

    /**
     *
     * Initialize the decorator
     * @param stage
     */
    void initBasicLayout(Stage stage);

    /**
     * retirn the Root node, this node is the parent for all perspectives
     * @return  the root node
     */
    Pane getRoot();

    /**
     * Returns the glassPane which is needed to show dialogs
     * @return the glassPane
     */
    Pane getGlassPane();

    /**
     *
     * set the current workbenchLayout
     * @param workbenchLayout the {@link org.jacpfx.api.componentLayout.WorkbenchLayout}
     */
    void setWorkbenchLayout(WorkbenchLayout<Node> workbenchLayout);
}
