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

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests1;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests2;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests3;
import org.jacp.test.perspectives.PerspectiveShutdownAndRestartComponents;
import org.jacp.test.workbench.WorkbenchShutdownAndReopenComponentsTest;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 13.11.13
 * Time: 21:37
 * To change this template use File | Settings | File Templates.
 */
public class ShutdownAndReopenComponentsTest6 extends TestFXJacpFXSpringLauncher {


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
        return WorkbenchShutdownAndReopenComponentsTest.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }



    private void testStopComponent() throws InterruptedException {
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests2.stopLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests3.stopLatch = new CountDownLatch(1);

        PerspectiveShutdownAndRestartComponents.stopFXComponent();
        Thread.sleep(1000);
        ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
        ComponentShutdownAndRestartComponentsTests2.stopLatch.await();
        ComponentShutdownAndRestartComponentsTests3.stopLatch.await();

        perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();


            assertTrue(components.isEmpty());

        }
    }




    @Test              // test sending messages, stop the component while new messages are send
    public void test6() throws InterruptedException {
        // Component is shut down
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            // Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
        int i = 0;
        while (i < 100) {

            ComponentShutdownAndRestartComponentsTests1.startLatch = new CountDownLatch(1);
           // ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            int j=0;
            while (j<10){
                ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
                PerspectiveShutdownAndRestartComponents.startStopComponent();
                j++;
                ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
            }


            ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopFXComponent();
            ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
           /* ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            int p=0;
            while (p<10){

                PerspectiveShutdownAndRestartComponents.startStopComponent();
                p++;
            }
            ComponentShutdownAndRestartComponentsTests1.stopLatch.await();*/
          //  exceptionLatch.await();
            i++;
        }
    }


    @Test
    @Ignore
    public void test7() throws InterruptedException {
        // Component is shut down
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);

        int i = 0;
        while (i < 100) {

            ComponentShutdownAndRestartComponentsTests3.startLatch = new CountDownLatch(1);
            ComponentShutdownAndRestartComponentsTests3.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopStartSatelessComponent();
            ComponentShutdownAndRestartComponentsTests3.startLatch.await();
            ComponentShutdownAndRestartComponentsTests3.stopLatch.await();

            i++;
        }
        Thread.sleep(2000);
    }


}
