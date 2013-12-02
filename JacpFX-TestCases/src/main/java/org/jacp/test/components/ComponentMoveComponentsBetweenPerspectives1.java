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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jacp.test.main.ApplicationLauncherMoveComponentsBetweenComponents;
import org.jacp.test.main.ApplicationShutdownAndRestartComponentsTest;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.component.Component;
import org.jacpfx.api.annotations.component.Stateless;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.component.CallbackComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.context.JACPContext;
import org.jacpfx.rcp.util.FXUtil;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * A simple JacpFX FXML UI component
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */

@Component(id = "id0023", name = "SimpleView", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
public class ComponentMoveComponentsBetweenPerspectives1 implements CallbackComponent {

    private final Logger log = Logger.getLogger(ComponentMoveComponentsBetweenPerspectives1.class
            .getName());


    public static boolean ui = false;

    @Resource
    private static JACPContext context;

    public static CountDownLatch wait = new CountDownLatch(1);
    public static CountDownLatch latch = new CountDownLatch(AStatelessCallbackComponent.MAX_INCTANCE_COUNT);
    public static CountDownLatch countdownlatch = new CountDownLatch(1);
    public static CountDownLatch stopLatch = new CountDownLatch(1);
    public static CountDownLatch startLatch = new CountDownLatch(1);

    public static String currentId = "id20";

    @Override
    /**
     * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
     */
    public Object handle(final Message<Event, Object> action) {
        //System.err.println("Message id11 : "+message+"  :: "+this);
        if (action.messageBodyEquals(FXUtil.MessageUtil.INIT)) {
            ApplicationLauncherMoveComponentsBetweenComponents.latch.countDown();
        } else if (action.messageBodyEquals("switch")) {
            if(currentId.equals("id20")) {
                currentId="id21";

            } else {
                currentId="id20";
            }
            context.setExecutionTarget(currentId);
            return null;
        }else {
            countdownlatch.countDown();

            return null;
        }

        return "message";
    }

    public static void switchTarget() {
        context.send("switch");
    }


    public static synchronized JACPContext getContext() {
        return context;
    }


    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
        this.log.info("run on start of id0022 "+this+"   target"+currentId);
        startLatch.countDown();
    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.log.info("run on tear down of id0022 "+this);
        latch.countDown();
        stopLatch.countDown();
    }


}
