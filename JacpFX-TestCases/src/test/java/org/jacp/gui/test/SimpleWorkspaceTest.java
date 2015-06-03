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

package org.jacp.gui.test;

import javafx.application.Platform;
import javafx.scene.Parent;
import org.jacp.test.NonUITests;
import org.jacp.test.main.ApplicationLauncher;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.categories.TestFX;

/**
 * Created by amo on 28.01.14.
 */
@Category(TestFX.class)
public class SimpleWorkspaceTest extends GuiTest {
    static Thread t;

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }

    @Override
    public void setupStage() {
        t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationLauncher.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncher.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Parent getRootNode() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        return null;
    }

    @Test
    public void testGetWorkbenchNode() {

    }
}
