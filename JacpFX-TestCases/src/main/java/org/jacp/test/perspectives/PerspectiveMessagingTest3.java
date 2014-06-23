/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2014
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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.main.ApplicationLauncher;
import org.jacp.test.main.ApplicationLauncherMessagingTest;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.FXUtil.MessageUtil;

import java.util.ResourceBundle;

/**
 * A simple perspective defining a split pane
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@Perspective(id = PerspectiveIds.PerspectiveMessagingTest3, name = "contactPerspective",
        components = {
                ComponentIds.ComponentMessagingTests1,ComponentIds.ComponentMessagingTests2 ,ComponentIds.CallbackComponentMessagingTest1_1
                },
        viewLocation = "/fxml/perspectiveOne.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US",
        active = false)
public class PerspectiveMessagingTest3 implements FXPerspective {
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;

    @Resource
    private static Context context;

    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        if (action.messageBodyEquals(MessageUtil.INIT)) {

            //perspectiveLayout.registerRootComponent(createRoot());
            GridPane.setVgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);
            GridPane.setHgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);
            content1.setStyle("-fx-background-color: red");
            content2.setStyle("-fx-background-color: red");
            content3.setStyle("-fx-background-color: red");
            // register left panel
            perspectiveLayout.registerTargetLayoutComponent("content0",
                    this.content1);
            perspectiveLayout.registerTargetLayoutComponent("content1",
                    this.content2);
            perspectiveLayout.registerTargetLayoutComponent("content2",
                    this.content3);
            ApplicationLauncherMessagingTest.latch.countDown();
        }

    }


    @OnShow
    public void onShow(final FXComponentLayout layout) {

    }

    @PostConstruct
    /**
     * @OnStart annotated method will be executed when component is activated.
     * @param layout
     * @param resourceBundle
     */
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {

    }

    @PreDestroy
    /**
     * @OnTearDown annotated method will be executed when component is deactivated.
     * @param arg0
     */
    public void onTearDownPerspective(final FXComponentLayout arg0) {
        // remove toolbars and menu entries when close perspective

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonOne() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button1");
    }

    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonTwo() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button2");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonThree() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button3");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonFour() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button4");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonFive() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button5");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonSix() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button6");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonSeven() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button7");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonEight() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button8");

    }
    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonNine() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button9");

    }

    /**
     * sends a message ComponentMessagingTest1 and executes Button1
     */
    public static void ClickButtonTen() {
        context.send((PerspectiveIds.PerspectiveMessagingTest3).concat(".").concat(ComponentIds.ComponentMessagingTests1),"button10");

    }

}
