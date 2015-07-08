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
package org.jacp.test.workbench;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.workbench.FXWorkbench;

/**
 * A simple JacpFX workbench. Define basic UI settings like size, menus and
 * toolbars here.
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */
@org.jacpfx.api.annotations.workbench.Workbench(id = WorkbenchIds.WorkbenchOnShowPerspective, name = "workbench", perspectives = {
        PerspectiveIds.PerspectiveOnShowTest4,
        PerspectiveIds.PerspectiveOnShowTest3,
        PerspectiveIds.PerspectiveOnShowTest2,
        PerspectiveIds.PerspectiveOnShowTest1})
public class WorkbenchOnShowPerspective implements FXWorkbench {
    private Stage stage;
    @Resource
    Context context;

    @Override
    public void handleInitialLayout(final Message<Event, Object> action,
                                    final WorkbenchLayout<Node> layout, final Stage stage) {
        layout.setWorkbenchXYSize(1024, 600);
        layout.setStyle(StageStyle.DECORATED);
        layout.setMenuEnabled(true);
        layout.registerToolBar(ToolbarPosition.SOUTH);
        this.stage = stage;

    }

    @Override
    public void postHandle(final FXComponentLayout layout) {

    }


}
