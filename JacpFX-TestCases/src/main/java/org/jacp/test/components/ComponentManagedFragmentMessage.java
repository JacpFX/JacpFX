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
package org.jacp.test.components;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jacp.test.dialogs.DialogManagedFragmentMessageTest;
import org.jacp.test.main.ApplicationLauncherMessagingTest;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.component.DeclarativeView;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.method.OnMessage;
import org.jacpfx.api.annotations.method.OnAsyncMessage;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.components.managedFragment.ManagedFragmentHandler;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.util.FXUtil;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@DeclarativeView(id = ComponentIds.ComponentManagedFragmentMessage, viewLocation ="/fxml/ComponentMessagingTests1Component2.fxml", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", initialTargetLayoutId = "content1")
public class ComponentManagedFragmentMessage implements FXComponent {

    private final Logger log = Logger.getLogger(ComponentManagedFragmentMessage.class
            .getName());

    String current = "content0";
    Button button1 = new Button("dialog");

    @FXML
    VBox container ;
    Label label = new Label();
    public static boolean ui = false;
    public static String[] value = new String[1];
    @Resource
    private Context context;

    public static AtomicInteger counter = new AtomicInteger(10000);
    public static CountDownLatch waitButton4 = new CountDownLatch(1);

    @Override
    /**
     * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
     */
    public Node handle(final Message<Event, Object> action) {

        return null;
    }

    @OnAsyncMessage(String.class)
    public void handleAsyncString(final Message<Event, Object> message) {
        if (message.messageBodyEquals("button10")) {
            IntStream.rangeClosed(1, 100).forEach(i -> context.send(ComponentIds.CallbackComponentMessagingTest2, "message9"));
        } else if (message.messageBodyEquals("button11")) {
            IntStream.rangeClosed(1, 100).forEach(i -> context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.CallbackComponentMessagingTest2), "message10"));
        }
    }

    @Override
    /**
     * The postHandleAction method runs always in the main application thread.
     */
    public Node postHandle(final Node arg0,
                           final Message<Event, Object> action) {

        return null;
    }

    @OnMessage(String.class)
    public void handleString(final Message<Event, Object> message) {
        if (message.messageBodyEquals(FXUtil.MessageUtil.INIT)) {


            ApplicationLauncherMessagingTest.latch.countDown();
        }
        System.out.println("Component:: "+message.getMessageBody());
    }


    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
        button1 = new Button("dialog");

        label = new Label();

        HBox group1 = new HBox();
        button1.setOnMouseClicked((event) -> {
            ManagedFragmentHandler<DialogManagedFragmentMessageTest> dialog = context.getManagedFragmentHandler(DialogManagedFragmentMessageTest.class);
            dialog.getController().init();
            context.showModalDialog(dialog.getFragmentNode());
        });

        group1.getChildren().addAll(button1);

        container.getChildren().addAll(label, group1);
        this.log.info("run on start of ComponentMessagingTest1 ... componentId:" + this.context.getId() + " parentId: "+this.context.getParentId()+"  this:"+this);
        waitButton4.countDown();
    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.log.info("run on tear down of ComponentMessagingTest1 " + this);
        waitButton4.countDown();
    }


}
