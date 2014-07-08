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
package org.jacp.test.toolbar.components;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.main.ApplicationLauncherHandleToolBarButtonsBetweenPerspectives;
import org.jacp.test.toolbar.base.HandleToolbarBase;
import org.jacp.test.toolbar.perspectives.PerspectiveOneToolbarSwitchPerspectives;
import org.jacp.test.util.MessageConstants;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.component.View;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.util.FXUtil;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import static org.jacp.test.util.MessageConstants.SWITCH_MESSAGE;


/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@View(id = ComponentHandleToolBarBetweenPerspectives2.ID,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US",
        initialTargetLayoutId = "content0")
public class ComponentHandleToolBarBetweenPerspectives2 extends HandleToolbarBase implements FXComponent {

    public static final String ID = ComponentIds.ComponentHandleToolBarBetweenPerspectives2;
    @Resource
    protected static Context context;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    Button button = new Button("move to next target");
    VBox container = new VBox();
    Label label = new Label();

    public static void
    switchTarget() {
        context.send(SWITCH_MESSAGE);
    }

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

        String currentAction = (String) action.getMessageBody();

        switch (currentAction) {
            case FXUtil.MessageUtil.INIT:
                button.setOnMouseClicked(context.getEventHandler(SWITCH_MESSAGE));
                button.setStyle("-fx-background-color: green");
                label.setText(" current Tagret: " + currentId);
                container.getChildren().addAll(button, label);
                ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.latch.countDown();
                return container;
            case SWITCH_MESSAGE:
                this.switchCurrentId();
                context.setExecutionTarget(currentId);
                break;
            default:
                break;
        }


        return null;
    }

    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout fxComponentLayout,
                                 final ResourceBundle resourceBundle) {
        this.logger.info("run on start of " + ComponentHandleToolBarBetweenPerspectives2.ID + " " + this + " execution target: " + currentId);
        button = new Button("move to next target");
        container = new VBox();
        label = new Label();

        logger.info("..:: ADD BUTTONS FOR COMPONENT " + this.getClass().getName() + "::..");
        //    BUTTON TO SWITCH TO ANOTHER PERSPECTIVE (id_pt_01)
        final JACPToolBar toolbar = fxComponentLayout.getRegisteredToolBar(ToolbarPosition.NORTH);
        final Button p1 = new Button("[" + ID + "]" + " COMP");
        final Button p2 = new Button("[" + ID + "]" + " CLEAR");
        p1.setOnMouseClicked((event) -> context.send(PerspectiveOneToolbarSwitchPerspectives.ID, MessageConstants.SHOW_MESSAGE));
        toolbar.addAll(p1, p2);

        startLatch.countDown();

    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.logger.info("run on tear down of " + ComponentHandleToolBarBetweenPerspectives2.ID + " " + this);
        stopLatch.countDown();
    }


}
