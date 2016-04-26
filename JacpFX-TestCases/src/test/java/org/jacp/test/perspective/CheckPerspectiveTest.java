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

package org.jacp.test.perspective;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.workbench.WorkbenchCheckComponentsAndPerspectives;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Andy Moncsek on 24.04.15.
 */
public class CheckPerspectiveTest extends TestFXJacpFXSpringLauncher {

    @Override
    public String getXmlConfig() {
        return "main.xml";
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchCheckComponentsAndPerspectives.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {
        System.out.println("post init stage");
    }





    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = WorkbenchCheckComponentsAndPerspectives.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }

    @Test
    public void checkPerspectives() {

        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length == perspectives.size());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {
            assertNotNull(p.getComponentHandler());
            assertNotNull(p.getContext());
            assertNotNull(p.getMessageQueue());
            assertNotNull(p.getMessageDelegateQueue());
            JacpContext<EventHandler<Event>, Object> context = p.getContext();
            assertNotNull(context.getParentId());
            assertNotNull(context.getId());
            assertNotNull(context.getResourceBundle());
        }
    }

}
