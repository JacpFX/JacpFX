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
package org.jacp.test.components;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jacp.test.main.ApplicationLauncherMessagingTest;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.component.View;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
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

@View(id = ComponentIds.ComponentMessagingTests1, name = "SimpleView", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", initialTargetLayoutId = "content1")
public class ComponentMessagingTest1 implements FXComponent {

    private final Logger log = Logger.getLogger(ComponentMessagingTest1.class
            .getName());

    String current = "content0";
    Button button1 = new Button("localMessage");
    Button button2 = new Button("message without parent");
    Button button3 = new Button("message fullqualified");
    Button button4 = new Button("message without parent to inactive");
    Button button5 = new Button("message fullqualified to inactive in p1");
    Button button7 = new Button("message fullqualified to inactive in p3");
    Button button6 = new Button("message fullqualified to inactive in other perspective (p2)");
    Button button8 = new Button("message without parent to local callback (c1)");
    Button button9 = new Button("message fullqualified to local callback (c1)");
    Button button10 = new Button("deactivate c1");
    Button button11 = new Button("message to asyncCallbac");
    Button button12 = new Button("fullqualified message to asyncCallbac");
    Button button13 = new Button("stop asyncCallbac");
    Button button14 = new Button("move component");
    VBox container = new VBox();
    Label label = new Label();
    public static boolean ui = false;
    public static String[] value = new String[1];
    @Resource
    private Context context;

    public static AtomicInteger counter = new AtomicInteger(10000);
    public static CountDownLatch waitButton1 = new CountDownLatch(1);
    public static CountDownLatch waitButton2 = new CountDownLatch(1);
    public static CountDownLatch waitButton3 = new CountDownLatch(1);
    public static CountDownLatch waitButton4 = new CountDownLatch(1);

    @Override
    /**
     * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
     */
    public Node handle(final Message<Event, Object> action) {
        if (action.messageBodyEquals("button10")) {
            IntStream.rangeClosed(1, 100).forEach(i -> context.send(ComponentIds.CallbackComponentMessagingTest2, "message9"));
        } else if (action.messageBodyEquals("button11")) {
            IntStream.rangeClosed(1, 100).forEach(i -> context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.CallbackComponentMessagingTest2), "message10"));
        }
        return null;
    }

    @Override
    /**
     * The postHandleAction method runs always in the main application thread.
     */
    public Node postHandle(final Node arg0,
                           final Message<Event, Object> action) {
        if (action.messageBodyEquals(FXUtil.MessageUtil.INIT)) {


            ApplicationLauncherMessagingTest.latch.countDown();
        } else if (action.messageBodyEquals("button1")) {
            label.setText(action.getMessageBody().toString());
            context.send("message1Local");
        } else if (action.messageBodyEquals("message1Local")) {
            label.setText(action.getMessageBody().toString());
            waitButton1.countDown();
        } else if (action.messageBodyEquals("button2")) {
            label.setText(action.getMessageBody().toString());
            context.send(ComponentIds.ComponentMessagingTests1, "message1");
        } else if (action.messageBodyEquals("message1")) {
            label.setText(action.getMessageBody().toString());
            waitButton2.countDown();
        } else if (action.messageBodyEquals("button3")) {
            label.setText(action.getMessageBody().toString());
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.ComponentMessagingTests1), "message2");
        } else if (action.messageBodyEquals("message2")) {
            label.setText(action.getMessageBody().toString());
            waitButton3.countDown();
        } else if (action.messageBodyEquals("button4")) {
            label.setText(action.getMessageBody().toString());
            context.send(ComponentIds.ComponentMessagingTests2, "message3");
        } else if (action.messageBodyEquals("button5")) {
            label.setText(action.getMessageBody().toString());
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.ComponentMessagingTests2), "message4");
        } else if (action.messageBodyEquals("button6")) {
            label.setText(action.getMessageBody().toString());
            context.send(PerspectiveIds.PerspectiveMessagingTest2.concat(".").concat(ComponentIds.ComponentMessagingTests3), "message5");
        } else if (action.messageBodyEquals("button7")) {
            label.setText(action.getMessageBody().toString());
            context.send(PerspectiveIds.PerspectiveMessagingTest3.concat(".").concat(ComponentIds.ComponentMessagingTests2), "message6");
        } else if (action.messageBodyEquals("button8")) {
            label.setText(action.getMessageBody().toString());
            context.send(ComponentIds.CallbackComponentMessagingTest1_1, "message7");
        } else if (action.messageBodyEquals("button9")) {
            label.setText(action.getMessageBody().toString());
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.CallbackComponentMessagingTest1_1), "message8");
        } else if (action.messageBodyEquals("button10")) {
            label.setText(action.getMessageBody().toString());
        } else if (action.messageBodyEquals("button11")) {
            label.setText(action.getMessageBody().toString());
        }else if (action.messageBodyEquals("button14")) {
            if(context.getParentId().equals(PerspectiveIds.PerspectiveMessagingTest)) {
                context.setExecutionTarget(PerspectiveIds.PerspectiveMessagingTest2);
            }else {
                context.setExecutionTarget(PerspectiveIds.PerspectiveMessagingTest);
            }
        } else {
            label.setText(action.getMessageBody().toString());

        }

        value[0] = action.getMessageBody().toString();
        return container;
    }


    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
        button1 = new Button("localMessage");
        button2 = new Button("message without parent");
        button3 = new Button("message fullqualified");
        button4 = new Button("message without parent to inactive");
        button5 = new Button("message fullqualified to inactive");
        button6 = new Button("message fullqualified to inactive component in other perspective (p2)");
        button7 = new Button("message fullqualified to inactive in p3");
        button8 = new Button("message without parent to local callback (c1)");
        button9 = new Button("message fullqualified to local callback (c1)");
        button10 = new Button("deactivate c1");
        button11 = new Button("message to asyncCallbac");
        button12 = new Button("fullqualified message to asyncCallbac");
        button13 = new Button("stop asyncCallbac");
        button14 = new Button("move component");
        container = new VBox();
        label = new Label();

        HBox group1 = new HBox();
        button1.setOnMouseClicked((event) -> {
            context.send("message1Local");
        });
        button2.setOnMouseClicked((event) -> {
            context.send(ComponentIds.ComponentMessagingTests1, "message1");
        });
        button3.setOnMouseClicked((event) -> {
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.ComponentMessagingTests1), "message2");
        });
        group1.getChildren().addAll(button1, button2, button3);
        HBox group2 = new HBox();
        button4.setOnMouseClicked((event) -> {
            context.send(ComponentIds.ComponentMessagingTests2, "message3");
        });
        button5.setOnMouseClicked((event) -> {
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.ComponentMessagingTests2), "message4");
        });
        group2.getChildren().addAll(button4, button5);

        button6.setOnMouseClicked((event) -> {
            context.send(PerspectiveIds.PerspectiveMessagingTest2.concat(".").concat(ComponentIds.ComponentMessagingTests3), "message5");
        });
        button7.setOnMouseClicked((event) -> {
            context.send(PerspectiveIds.PerspectiveMessagingTest3.concat(".").concat(ComponentIds.ComponentMessagingTests2), "message6");
        });

        HBox group3 = new HBox();
        button8.setOnMouseClicked((event) -> {
            context.send(ComponentIds.CallbackComponentMessagingTest1_1, "message7");
        });
        button9.setOnMouseClicked((event) -> {
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.CallbackComponentMessagingTest1_1), "message8");
        });
        button10.setOnMouseClicked((event) -> {
            context.send(ComponentIds.CallbackComponentMessagingTest1_1, "stop");
        });
        group3.getChildren().addAll(button8, button9, button10);

        HBox group4 = new HBox();
        button11.setOnMouseClicked((event) -> {
            context.send("button10");
        });
        button12.setOnMouseClicked((event) -> {
            context.send("button11");
        });
        button13.setOnMouseClicked((event) -> {
            context.send(PerspectiveIds.PerspectiveMessagingTest.concat(".").concat(ComponentIds.CallbackComponentMessagingTest2), "stop");
        });
        group4.getChildren().addAll(button11, button12, button13);

        button14.setOnMouseClicked((event) -> {
            context.send("button14");
        });
        container.getChildren().addAll(label, group1, group2, button6, button7, group3, group4,button14);
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
