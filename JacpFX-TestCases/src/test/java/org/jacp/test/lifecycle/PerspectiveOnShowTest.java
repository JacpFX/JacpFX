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

package org.jacp.test.lifecycle;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacp.test.perspectives.PerspectiveOnShowTest1;
import org.jacp.test.perspectives.PerspectiveOnShowTest2;
import org.jacp.test.workbench.WorkbenchOnShowPerspective;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Andy Moncsek on 07.07.15.
 */
public class PerspectiveOnShowTest extends TestFXJacpFXSpringLauncher {




    @Override
    public String getXmlConfig() {
        return "main.xml";
    }



    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchOnShowPerspective.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }


    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = WorkbenchOnShowPerspective.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }

    @Test
    public void checkPerspectives() throws InterruptedException {

        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length == perspectives.size());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {
            if(!p.isStarted()) continue;
            assertNotNull(p.getComponentHandler());
            assertNotNull(p.getContext());
            assertNotNull(p.getMessageQueue());
            assertNotNull(p.getMessageDelegateQueue());
            JacpContext<EventHandler<Event>, Object> context = p.getContext();
            assertNotNull(context.getParentId());
            assertNotNull(context.getId());
            assertNotNull(context.getName());
            assertNotNull(context.getResourceBundle());
        }
        PerspectiveOnShowTest1.postconstruct.await();
        PerspectiveOnShowTest1.onShow.await();
        o1testStartP2();
        o2testStopP1();
    }

    public void o1testStartP2() throws InterruptedException {
        PerspectiveOnShowTest1.onHide = new CountDownLatch(1);
        PerspectiveOnShowTest2.postconstruct = new CountDownLatch(1);
        PerspectiveOnShowTest2.onShow = new CountDownLatch(1);
        PerspectiveOnShowTest1.send(PerspectiveIds.PerspectiveOnShowTest2,"start");
        PerspectiveOnShowTest1.onHide.await();
        PerspectiveOnShowTest2.postconstruct.await();
        PerspectiveOnShowTest2.onShow.await();

    }
    public void o2testStopP1() throws InterruptedException {
        PerspectiveOnShowTest2.onHide = new CountDownLatch(1);
        PerspectiveOnShowTest1.onShow = new CountDownLatch(1);
        PerspectiveOnShowTest1.predestroy = new CountDownLatch(1);
        PerspectiveOnShowTest2.send(PerspectiveIds.PerspectiveOnShowTest1,"stop");
        PerspectiveOnShowTest2.onHide.await();
        PerspectiveOnShowTest1.onShow.await();
        PerspectiveOnShowTest1.predestroy.await();
    }
}
