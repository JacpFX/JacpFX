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

package org.jacp.test.messaging;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.components.ComponentMessagingTest1Component1;
import org.jacp.test.components.ComponentMessagingTest1Component2;
import org.jacp.test.main.ApplicationLauncher;
import org.jacp.test.main.ApplicationLauncherComponentMessaginTest1;
import org.jacp.test.main.ApplicationLauncherMessagingTest;
import org.jacp.test.perspectives.PerspectiveComponentMessagingTest1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class FXComponentMessagingTest2 {
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

                ApplicationLauncherMessagingTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherMessagingTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeMessaging() throws InterruptedException {
        ComponentMessagingTest1Component1.wait = new CountDownLatch(1);
        ComponentMessagingTest1Component2.wait = new CountDownLatch(1);

        ComponentMessagingTest1Component1.counter = new AtomicInteger(10000);
        ComponentMessagingTest1Component2.counter = new AtomicInteger(10000);

        PerspectiveComponentMessagingTest1.fireMessage();

        ComponentMessagingTest1Component1.wait.await();
        ComponentMessagingTest1Component2.wait.await();

    }


    @Test
    public void checkApplicationLauncher() {
        ApplicationLauncherMessagingTest launcher = ApplicationLauncherMessagingTest.instance[0];
        assertNotNull(launcher);
    }



    private void warmUp() throws InterruptedException {
        executeMessaging();
    }


    private void withUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i = 0;
        ComponentMessagingTest1Component1.ui = true;
        ComponentMessagingTest1Component2.ui = true;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution with ui time was " + (end - start) + " ms.");
    }

    private void withoutUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i = 0;
        ComponentMessagingTest1Component1.ui = false;
        ComponentMessagingTest1Component2.ui = false;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution without ui time was " + (end - start) + " ms.");
    }
}
