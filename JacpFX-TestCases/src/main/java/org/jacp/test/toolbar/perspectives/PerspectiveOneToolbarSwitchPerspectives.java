/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [PerspectiveOne.java]
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
package org.jacp.test.toolbar.perspectives;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jacp.test.main.ApplicationLauncherHandleToolBarButtonsBetweenPerspectives;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacp.test.toolbar.base.HandleToolbarBase;
import org.jacp.test.toolbar.components.ComponentHandleToolBarBetweenPerspectives1;
import org.jacp.test.toolbar.components.ComponentHandleToolBarBetweenPerspectives2;
import org.jacp.test.util.MessageConstants;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.FXUtil.MessageUtil;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static org.jacpfx.rcp.util.LayoutUtil.GridPaneUtil;

/**
 * A simple perspective defining a split pane
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 * @author <a href="mailto:pete.ahcp@gmail.com">Patrick Symmangk</a>
 */

@Perspective(id = PerspectiveOneToolbarSwitchPerspectives.ID, name = "contactPerspective",
        components = {ComponentHandleToolBarBetweenPerspectives1.ID, ComponentHandleToolBarBetweenPerspectives2.ID},
        viewLocation = "/fxml/perspectiveOne.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US")
public class PerspectiveOneToolbarSwitchPerspectives extends HandleToolbarBase implements FXPerspective {

    // =================== CONSTANTS ===================
    public static final String ID = PerspectiveIds.PerspectiveOneToolbarSwitchPerspectives;
    public static CountDownLatch start = new CountDownLatch(1);
    @Resource
    static Context context;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;

    public static void switchPerspective() {
        context.send(PerspectiveTwoToolbarSwitchPerspectives.ID, MessageConstants.SWITCH_MESSAGE);
    }

    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        String currentAction = (String) action.getMessageBody();
        switch (currentAction) {
            case MessageUtil.INIT:
                //perspectiveLayout.registerRootComponent(createRoot());
                GridPaneUtil.setFullGrow(Priority.ALWAYS, perspectiveLayout.getRootComponent());

                // register left panel
                perspectiveLayout.registerTargetLayoutComponent("content0", this.content1);
                perspectiveLayout.registerTargetLayoutComponent("content1", this.content2);
                perspectiveLayout.registerTargetLayoutComponent("content2", this.content3);
                ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.latch.countDown();

                break;
        }


    }

    @OnShow
    public void onShow(final FXComponentLayout layout) {
        switchLatch.countDown();
    }

    @PostConstruct
    /**
     * @OnStart annotated method will be executed when component is activated.
     * @param layout
     * @param resourceBundle
     */
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        logger.info("..:: ADD BUTTONS FOR PERSPECTIVE " + this.getClass().getName() + "::..");
        final JACPToolBar toolbar = layout.getRegisteredToolBar(ToolbarPosition.NORTH);

        final Button globalP1 = new Button("[" + ID + "]" + " GLOBAL");
        final Button p1 = new Button("[" + ID + "]" + " SWITCH");
        p1.setOnAction((event) -> switchPerspective());

        toolbar.add(p1);
        toolbar.addOnEnd("globP2", globalP1);
        start.countDown();
    }

    @PreDestroy
    /**
     * @OnTearDown annotated method will be executed when component is deactivated.
     * @param arg0
     */
    public void onTearDownPerspective(final FXComponentLayout arg0) {
        // remove toolbars and menu entries when close perspective
    }

}
