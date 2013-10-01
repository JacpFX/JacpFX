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
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Resource;
import org.jacp.api.annotations.lifecycle.OnShow;
import org.jacp.api.annotations.lifecycle.PostConstruct;
import org.jacp.api.annotations.lifecycle.PreDestroy;
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.componentLayout.PerspectiveLayout;
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.perspective.FXPerspective;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;
import org.jacp.test.main.ApplicationLauncher;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple perspective defining a split pane
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@Perspective(id = "id10", name = "contactPerspective",
        components ={} ,
       // viewLocation = "/fxml/perspectiveOne.fxml",
        resourceBundleLocation = "bundles.languageBundle" ,
        localeID="en_US")
public class PerspectiveMessagingTestP1 implements FXPerspective {
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;
    @Resource
    private static JACPContext context;
    private AtomicInteger counter = new AtomicInteger(1000);
    public static CountDownLatch wait = new CountDownLatch(1);

    @Override
    public void handlePerspective(final IAction<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
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
            ApplicationLauncher.latch.countDown();
        } else {
            if(counter.get()>1){
                System.out.println("Perspective id09: "+counter.decrementAndGet());
                context.getActionListener("id10","message").performAction(null);
            }else{
                wait.countDown();

            }

        }

    }


    public static void fireMessage() {
        context.getActionListener("id10","message").performAction(null);
    }

    private Node createRoot() {
        BorderPane pane = new BorderPane();
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(Double.valueOf(0.2506265664160401));
        splitPane.getStyleClass().add("hsplitpane");

        content1 = new HBox();
        HBox paneRight = new HBox();
        splitPane.getItems().addAll(content1,paneRight);
        SplitPane contentSplitPane = new SplitPane();
        contentSplitPane.setPrefWidth(800);
        contentSplitPane.setDividerPositions(Double.valueOf(0.5));
        contentSplitPane.setOrientation(Orientation.VERTICAL);

        content2 = new HBox();
        content3 = new HBox();
        contentSplitPane.getItems().addAll(content2,content3);
        paneRight.getChildren().add(contentSplitPane);
        pane.setCenter(splitPane);

        return pane;
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

}
