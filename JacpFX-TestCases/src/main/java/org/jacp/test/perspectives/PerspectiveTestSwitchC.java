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

package org.jacp.test.perspectives;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jacp.test.main.ApplicationLauncher;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.OnHide;
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
import org.jacpfx.rcp.util.FXUtil;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 27.08.13
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
@Perspective(id = PerspectiveIds.PerspectiveTestSwitchC,
        components = {},
        viewLocation = "/fxml/perspectiveOne.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US", active = false)
public class PerspectiveTestSwitchC implements FXPerspective {
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;
    @Resource
    Context context;

    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        if (action.messageBodyEquals(FXUtil.MessageUtil.INIT)) {

            GridPane.setVgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);
            GridPane.setHgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);

            // register left panel
            perspectiveLayout.registerTargetLayoutComponent("content0",
                    this.content1);
            perspectiveLayout.registerTargetLayoutComponent("content1",
                    this.content2);
            perspectiveLayout.registerTargetLayoutComponent("content2",
                    this.content3);
            ApplicationLauncher.latch.countDown();

        }

    }

    @OnHide
    public void onHide(final FXComponentLayout layout) {
        System.out.println(" on onHide PerspectiveTestSwitchC");
    }

    @OnShow
    public void onShow(final FXComponentLayout layout) {
        System.out.println(" on onShow PerspectiveTestSwitchC");
    }

    @PostConstruct
    /**
     * @OnStart annotated method will be executed when component is activated.
     * @param layout
     * @param resourceBundle
     */
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        System.out.println(" on start PerspectiveTestSwitchC");
        final JACPToolBar toolbar = layout.getRegisteredToolBar(ToolbarPosition.NORTH);
        final Button p1 = new Button("Perspective A");
        p1.setOnMouseClicked((event)->context.send(PerspectiveIds.PerspectiveTestSwitchA, "show"));
        toolbar.addAllOnEnd(p1);
    }

    private void setVisibility(List<Region> nodes, boolean visibility) {
        nodes.forEach(n -> n.setVisible(visibility));
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
