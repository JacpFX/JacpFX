/*
 * Copyright (c) 2013, Andy Moncsek, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of Andy Moncsek, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jacp.test.components;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jacp.test.main.ApplicationLauncherMoveComponentsBetweenComponents;
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
import java.util.logging.Logger;

/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@View(id = "id0024", name = "SimpleView", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", initialTargetLayoutId = "content0")
public class ComponentMoveComponentsBetweenPerspectives2 implements FXComponent {

    private final Logger log = Logger.getLogger(ComponentMoveComponentsBetweenPerspectives2.class
            .getName());

    String current = "content0";
    Button button = new Button("move to next target");
    VBox container = new VBox();
    Label label = new Label();
    public static CountDownLatch stopLatch = new CountDownLatch(1);
    public static CountDownLatch startLatch = new CountDownLatch(1);

    public static String currentId = "id20";

    @Resource
    private static Context context;

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
        if (action.messageBodyEquals("switch")) {
            if(currentId.equals("id20")) {
              currentId="id21";

            } else {
                currentId="id20";
            }
            context.setExecutionTarget(currentId);
         //   System.out.println("::::"+"3");
            return null;
        } else if(action.messageBodyEquals(FXUtil.MessageUtil.INIT)) {
            button.setOnMouseClicked(context.getEventHandler("switch"));
            button.setStyle("-fx-background-color: red");
            label.setText(" current Tagret: " + currentId);
            container.getChildren().addAll(button, label);
            ApplicationLauncherMoveComponentsBetweenComponents.latch.countDown();
       //     System.out.println("::::"+"2");
            return container;
        }
        return null;

    }

    public static void switchTarget() {
            context.send("switch");
    }

    public static void showPerspective(String id) {
        context.send(id,"show");
    }


    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
        this.log.info("run on start of id0024 "+this+" execution target: "+currentId);
        button = new Button("move to next target");
        container = new VBox();
        label = new Label();
        startLatch.countDown();
      //  System.out.println("::::"+"1");

    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.log.info("run on tear down of id0024 "+this);
        stopLatch.countDown();
     //   System.out.println("::::"+"4");
    }


}
