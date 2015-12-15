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
package org.jacp.test.tryout.component;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.tryout.config.BasicConfig;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.component.DeclarativeView;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.method.OnMessage;
import org.jacpfx.api.annotations.method.OnMessageAsync;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.util.FXUtil;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@DeclarativeView(id = ComponentIds.ComponentTwoVersion3,
        viewLocation = "/fxml/ComponentTwoVersion3.fxml",
        active = true,
        resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", initialTargetLayoutId = BasicConfig.TARGET_CONTAINER_RIGHT)
public class ComponentTwo implements FXComponent {

    @Resource
    private Context context;
    @FXML
    private TextArea name;

    private final Logger log = Logger.getLogger(ComponentTwo.class
            .getName());



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
                           final Message<Event, Object> message) {
        if (!message.messageBodyEquals(FXUtil.MessageUtil.INIT)) {

            name.setText(message.getTypedMessageBody(String.class));
        }
        return null;
    }

    @OnMessageAsync(String.class)
    public Node handleAsyncString(final Message<Event, Object> message) {
        System.out.println("ASYNC MESSAGE::: "+message.getTypedMessageBody(String.class)+" thread:"+Thread.currentThread());
        return new Label("hello from async");
    }

    @OnMessage(String.class)
    public void handleString(final Message<Event, Object> message,Label test) {
        if(test!=null) {
            System.out.println("MESSAGE::: "+message.getTypedMessageBody(String.class)+"  :: "+test.getText()+" thread:"+Thread.currentThread());
        } else {
            System.out.println("MESSAGE::: "+message.getTypedMessageBody(String.class)+" thread:"+Thread.currentThread());
        }
        System.out.println("MESSAGE---1::: "+message.getTypedMessageBody(String.class)+" thread:"+Thread.currentThread());
        name.setText(message.getTypedMessageBody(String.class));
    }




    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {

    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.log.info("run on tear down of ComponentMessagingTest1 " + this);

    }


}
