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
import javafx.scene.layout.VBox;
import org.jacp.test.main.ApplicationLauncherMessagingTest;
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

/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@View(id = ComponentIds.ComponentMessagingTests3,  active = false, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", initialTargetLayoutId = "content2")
public class ComponentMessagingTest3 implements FXComponent {

    private final Logger log = Logger.getLogger(ComponentMessagingTest3.class
            .getName());

    String current = "content0";
    Button button1 = new Button("deactivate");
    VBox container = new VBox();
    Label label = new Label();
    public static boolean ui = false;

    @Resource
    private Context context;

    public static AtomicInteger counter = new AtomicInteger(10000);
    public static CountDownLatch wait = new CountDownLatch(1);
    public static String[] value =new String[1];
    public static CountDownLatch waitButton1 = new CountDownLatch(1);
    @Override
    /**
     * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
     */
    public Node handle(final Message<Event, Object> action) {

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
        }else if(action.messageBodyEquals("message5")) {
            label.setText(action.getMessageBody().toString());
            value[0]= action.getMessageBody().toString();
            waitButton1.countDown();
        }else if(action.messageBodyEquals("deactivate")) {
                  context.setActive(false);
            value[0]= action.getMessageBody().toString();
        }
        else {
            label.setText(action.getMessageBody().toString());

        }


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
         button1 = new Button("deactivate");
         container = new VBox();
         label = new Label();
        button1.setOnMouseClicked((event)->{
            context.send("deactivate");
        });

        container.getChildren().addAll(label,button1);

    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.log.info("run on tear down of ComponentRight ");

    }


}
