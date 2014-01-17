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

package org.jacp.test.dialogs;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.components.ComponentDialogInPerspective;
import org.jacp.test.main.ApplicationLauncherDialogInPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveDialogInPerspectiveTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

/**
 * Created by Andy Moncsek on 06.01.14.
 */
public class DialogInComponentTest {
    static Thread t;

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        AllTests.resetApplication();
    }

    @BeforeClass
    public static void initWorkbench() {


        t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationLauncherDialogInPerspectiveTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherDialogInPerspectiveTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleDialogInit() throws InterruptedException {
        PerspectiveDialogInPerspectiveTest.initDialog1();
        DialogDialogInPerspectiveTest.latch.await();
        assertTrue(true);
    }

    @Test
    public void testSimpleFXMLDialogInit() throws InterruptedException {
        PerspectiveDialogInPerspectiveTest.initDialog2();
        DialogXMLDialogInPerspectiveTest.latch.await();
        assertTrue(true);
    }


    @Test
    public void testSimpleDialogInComponentInit() throws InterruptedException {
        ComponentDialogInPerspective.initDialog1();
        DialogDialogInComponentTest.latch.await();
        assertTrue(true);
    }

    @Test
    public void testSimpleDialogSingletonInComponentInit() throws InterruptedException {
        ComponentDialogInPerspective.initDialog2();
        DialogScopeSingletonComponentTest.latch.await();
        assertTrue(true);
    }

    @Test
    public void testSimpleDialogPrototypeInComponentInit() throws InterruptedException {
        ComponentDialogInPerspective.initDialog3();
        DialogScopePrototypeComponentTest.latch.await();
        assertTrue(true);
    }
}
