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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Resource;
import org.jacp.api.annotations.component.Component;
import org.jacp.api.annotations.component.View;
import org.jacp.api.annotations.lifecycle.PostConstruct;
import org.jacp.api.annotations.lifecycle.PreDestroy;
import org.jacp.javafx.rcp.component.FXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.test.main.ApplicationLauncher;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */
@Component(id = "id003", name = "SimpleView", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
@View(initialTargetLayoutId = "content1")
public class TestTwoView implements FXComponent {

    private final Logger log = Logger.getLogger(TestTwoView.class
            .getName());
    @FXML
    private TextField message;

    @FXML
    private VBox chatField;

    String current = "";
    Button button = new Button("move");
    VBox container = new VBox();
    Label label = new Label();

    @Resource private JACPContext context;

    @Override
    /**
     * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
     */
    public Node handle(final IAction<Event, Object> action) {

        return null;
    }

    @Override
    /**
     * The postHandleAction method runs always in the main application thread.
     */
    public Node postHandle(final Node arg0,
                                 final IAction<Event, Object> action) {
        current = context.getParentId();
        if (!action.isMessage(FXUtil.MessageUtil.INIT)) {

            current =   context.getParentId().equals("id02")?"id03":"id02";
            context.setExecutionTarget(current);
            label.setText(" current Perspective: "+current);

        } else {
            button.setOnMouseClicked(new EventHandler<MouseEvent>(){

                @Override
                public void handle(MouseEvent event) {


                    context.getActionListener("update").performAction(null);
                }
            });
            button.setStyle("-fx-background-color: red");
            label.setText(" current Perspective: "+current);
            container.getChildren().addAll(button,label);
            ApplicationLauncher.latch.countDown();
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

        System.out.println("START TWO");
        container = new VBox();
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
