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
import org.jacp.test.components.ComponentIds;
import org.jacp.test.main.ApplicationLauncherHandleToolBarButtonsBetweenPerspectives;
import org.jacp.test.toolbar.base.HandleToolbarBase;
import org.jacp.test.toolbar.perspectives.PerspectiveTwoToolbarSwitchPerspectives;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.component.Component;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.method.OnAsyncMessage;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.CallbackComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
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

@Component(id = ComponentHandleToolBarBetweenPerspectives1.ID,
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US")
public class ComponentHandleToolBarBetweenPerspectives1 extends HandleToolbarBase implements CallbackComponent {

    public static final String ID = ComponentIds.ComponentHandleToolBarBetweenPerspectives1;
    public static boolean ui = false;
    @Resource
    protected static Context context;
    private final Logger log = Logger.getLogger(ComponentHandleToolBarBetweenPerspectives1.class.getName());

    public static void switchTarget() {
        context.send(ComponentHandleToolBarBetweenPerspectives1.ID, SWITCH_MESSAGE);
    }

    public static synchronized Context getContext() {
        return context;
    }

    @Override
    /**
     * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
     */
    public Object handle(final Message<Event, Object> action) {
      return null;

    }

    @OnAsyncMessage(String.class)
    public Object onStringMessage(final Message<Event, Object> action) {
        String currentAction = (String) action.getMessageBody();

        switch (currentAction) {
            case FXUtil.MessageUtil.INIT:
                ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.latch.countDown();
                PerspectiveTwoToolbarSwitchPerspectives.start.countDown();
                return SWITCH_MESSAGE;
            case SWITCH_MESSAGE:
                this.switchCurrentId();
                context.setExecutionTarget(currentId);
                return null;
            default:
                countdownlatch.countDown();
                return null;

        }
    }

    @PostConstruct
    /**
     * The @OnStart annotation labels methods executed when the component switch from inactive to active state
     * @param arg0
     * @param resourceBundle
     */
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
        this.log.info("run on start of " + ComponentHandleToolBarBetweenPerspectives1.ID + " " + this + " target" + currentId);
        startLatch.countDown();
    }

    @PreDestroy
    /**
     * The @OnTearDown annotations labels methods executed when the component is set to inactive
     * @param arg0
     */
    public void onTearDownComponent(final FXComponentLayout arg0) {
        this.log.info("run on tear down of " + ComponentHandleToolBarBetweenPerspectives1.ID + " " + this);
        latch.countDown();
        stopLatch.countDown();
    }


}
