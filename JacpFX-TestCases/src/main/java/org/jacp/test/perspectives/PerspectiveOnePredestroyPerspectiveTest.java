/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [PerspectiveOne.java]
 * AHCP Project (http://jacp.googlecode.com)
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
import org.jacpfx.api.action.IAction;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.OnHide;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.JACPContext;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.FXUtil.MessageUtil;
import org.jacp.test.main.ApplicationLauncher;
import org.jacp.test.main.ApplicationPredestroyPerspectiveTest;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

/**
 * A simple perspective defining a split pane
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@Perspective(id = "id17", name = "contactPerspective",
        components = {"id016","id017","id018","id019"},
        // viewLocation = "/fxml/perspectiveOne.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US")
public class PerspectiveOnePredestroyPerspectiveTest implements FXPerspective {
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;
    @Resource
    private static JACPContext context;

    public static CountDownLatch latch = new CountDownLatch(1);
    public static CountDownLatch startLatch = new CountDownLatch(1);

    public static CountDownLatch showLatch = new CountDownLatch(1);

    public static CountDownLatch hideLatch = new CountDownLatch(1);

    @Override
    public void handlePerspective(final IAction<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        System.out.println("Perspective 17: "+action.getMessage());
        if (action.isMessage(MessageUtil.INIT)) {

            perspectiveLayout.registerRootComponent(createRoot());
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
            ApplicationPredestroyPerspectiveTest.latch.countDown();
        }
        else if (action.isMessage("stop")) {
            System.err.println("STOP MESSAGE P17");
            context.setActive(false);
        }
        else if (action.isMessage("SHOW")) {
            System.err.println("SHOW MESSAGE P17");

        }

    }

    public static void fireBurst(final int count) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                getContext().getActionListener("id17.id016", "message").performAction(null);
            }
        });
        t.setDaemon(true);
        t.start();

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                getContext().getActionListener("id17.id017", "message").performAction(null);
            }
        });
        t2.setDaemon(true);
        t2.start();

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                getContext().getActionListener("id17.id018", "message").performAction(null);
            }
        });
        t3.setDaemon(true);
        t3.start();

        Thread t4 = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                getContext().getActionListener("id17.id019", "message").performAction(null);
            }
        });
        t4.setDaemon(true);
        t4.start();
    }

    public static synchronized JACPContext getContext() {
        return context;
    }

    public static void stop() {
        context.getActionListener("stop").performAction(null);
    }
    private Node createRoot() {
        BorderPane pane = new BorderPane();
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(Double.valueOf(0.2506265664160401));
        splitPane.getStyleClass().add("hsplitpane");

        content1 = new HBox();
        HBox paneRight = new HBox();
        splitPane.getItems().addAll(content1, paneRight);
        SplitPane contentSplitPane = new SplitPane();
        contentSplitPane.setPrefWidth(800);
        contentSplitPane.setDividerPositions(Double.valueOf(0.5));
        contentSplitPane.setOrientation(Orientation.VERTICAL);

        content2 = new HBox();
        content3 = new HBox();
        contentSplitPane.getItems().addAll(content2, content3);
        paneRight.getChildren().add(contentSplitPane);
        pane.setCenter(splitPane);

        return pane;
    }

    @OnShow
    public void onShow(final FXComponentLayout layout) {
        System.out.println("on show p 17");
        showLatch.countDown();
    }

    @OnHide
    public void onHide(){
              hideLatch.countDown();
    }


    @PostConstruct
    /**
     * @OnStart annotated method will be executed when component is activated.
     * @param layout
     * @param resourceBundle
     */
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        System.out.println("on postconstruct p 17");
        startLatch.countDown();
    }

    @PreDestroy
    /**
     * @OnTearDown annotated method will be executed when component is deactivated.
     * @param arg0
     */
    public void onTearDownPerspective(final FXComponentLayout arg0) {
        // remove toolbars and menu entries when close perspective
        System.out.println("on predestroy p 17");
        latch.countDown();
    }

}
