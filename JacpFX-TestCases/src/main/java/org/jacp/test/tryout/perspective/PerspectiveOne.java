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
package org.jacp.test.tryout.perspective;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacp.test.tryout.config.BasicConfig;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.OnHide;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.LayoutUtil;

import java.util.ResourceBundle;

import static javafx.scene.layout.Priority.ALWAYS;

/**
 * A simple perspective defining a split pane
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@Perspective(id = PerspectiveIds.PerspectiveOneVersion3,
        components = {ComponentIds.ComponentOneVersion3, ComponentIds.ComponentTwoVersion3},
        viewLocation = "/fxml/perspectiveOneVersion3.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US")
public class PerspectiveOne implements FXPerspective {
    @FXML
    private SplitPane mainLayout;
    @FXML
    private GridPane leftMenu;
    @FXML
    private GridPane mainContent;


    @Resource
    public Context context;

    @Override
    /**
     * handle messages to perspective, be aware... perspective always runs on FXApplication thread
     *
     */
    public void handlePerspective(final Message<Event, Object> message,
                                  final PerspectiveLayout perspectiveLayout) {

    }


    @OnShow
    /**
     * This method will be executed when the perspective gets the focus and switches to foreground
     * @param layout, the component layout contains references to the toolbar and the menu
     */
    public void onShow(final FXComponentLayout layout) {

    }

    @OnHide
    /**
     * will be executed when an active perspective looses the focus and moved to the background.
     * @param layout, the component layout contains references to the toolbar and the menu
     */
    public void onHide(final FXComponentLayout layout) {

    }

    @PostConstruct
    /**
     * @PostConstruct annotated method will be executed when component is activated.
     * @param perspectiveLayout , the perspective layout let you register target layouts
     * @param layout, the component layout contains references to the toolbar and the menu
     * @param resourceBundle
     */
    public void onStartPerspective(final PerspectiveLayout perspectiveLayout, final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {


        // let them grow
        LayoutUtil.GridPaneUtil.setFullGrow(ALWAYS, mainLayout);
        // register left menu
        perspectiveLayout.registerTargetLayoutComponent(BasicConfig.TARGET_CONTAINER_LEFT, leftMenu);
        // register main content
        perspectiveLayout.registerTargetLayoutComponent(BasicConfig.TARGET_CONTAINER_RIGHT, mainContent);
    }

    @PreDestroy
    /**
     * @PreDestroy annotated method will be executed when component is deactivated.
     * @param layout, the component layout contains references to the toolbar and the menu
     */
    public void onTearDownPerspective(final FXComponentLayout layout) {

    }

}
