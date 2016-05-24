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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;
import junit.framework.Assert;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.CallbackComponentMessagingTest1_1;
import org.jacp.test.components.ComponentMessagingTest1;
import org.jacp.test.components.ComponentMessagingTest2;
import org.jacp.test.components.ComponentMessagingTest3;
import org.jacp.test.workbench.WorkbenchMessagingTest;
import org.jacpfx.rcp.handler.AErrorDialogHandler;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class FXComponentMessagingTest2 extends TestFXJacpFXSpringLauncher {



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
        return WorkbenchMessagingTest.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }


    @Test
    public void B_checkLocalMessages() throws InterruptedException {
        ComponentMessagingTest1.waitButton1 = new CountDownLatch(1);
        ComponentMessagingTest1.waitButton2 = new CountDownLatch(1);
        ComponentMessagingTest1.waitButton3 = new CountDownLatch(1);


        ComponentMessagingTest2.waitButton3 = new CountDownLatch(1);
        ComponentMessagingTest3.waitButton1 = new CountDownLatch(1);
        org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonOne();
        ComponentMessagingTest1.waitButton1.await();
        Assert.assertTrue(ComponentMessagingTest1.value[0].equals("message1Local"));

        org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonTwo();
        ComponentMessagingTest1.waitButton2.await();
        Assert.assertTrue(ComponentMessagingTest1.value[0].equals("message1"));

        org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonThree();
        ComponentMessagingTest1.waitButton3.await();
        Assert.assertTrue(ComponentMessagingTest1.value[0].equals("message2"));


        org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonSix();
        ComponentMessagingTest3.waitButton1.await();
        Assert.assertTrue(ComponentMessagingTest3.value[0].equals("message5"));

        org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonSeven();
        ComponentMessagingTest2.waitButton3.await();
        Assert.assertTrue(ComponentMessagingTest2.value[0].equals("message6"));
    }

    @Test
    public void C_sendMessageToInactiveComponent() throws InterruptedException {
        run(() -> {
            try {
                ComponentMessagingTest2.waitButton1 = new CountDownLatch(1);
                ComponentMessagingTest2.waitButton2 = new CountDownLatch(1);
                ComponentMessagingTest2.waitButton4 = new CountDownLatch(1);
                org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonFour();
                ComponentMessagingTest2.waitButton1.await();
                Assert.assertTrue(ComponentMessagingTest2.value[0].equals("message3"));



                org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonFive();
                ComponentMessagingTest2.waitButton2.await();
                Assert.assertTrue(ComponentMessagingTest2.value[0].equals("message4"));

                ComponentMessagingTest2.waitButton4 = new CountDownLatch(1);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void D_sendMessagesToInactiveCallback() throws InterruptedException {
        run(() -> {
            try {


                CallbackComponentMessagingTest1_1.wait1 = new CountDownLatch(1);

                CallbackComponentMessagingTest1_1.wait2 = new CountDownLatch(1);
                CallbackComponentMessagingTest1_1.wait3 = new CountDownLatch(1);
                org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonEight();
                CallbackComponentMessagingTest1_1.wait1.await();
                Assert.assertTrue(CallbackComponentMessagingTest1_1.value[0].equals("message7"));


                org.jacp.test.perspectives.PerspectiveMessagingTest.ClickButtonNine();
                CallbackComponentMessagingTest1_1.wait2.await();

                Assert.assertTrue(CallbackComponentMessagingTest1_1.value[0].equals("message8"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


   //@Test
    public void E_checkNonUniqueException() throws InterruptedException {
        ComponentMessagingTest1.waitButton4 = new CountDownLatch(2);
        org.jacp.test.perspectives.PerspectiveMessagingTest.MoveC1FromP1ToP2();
        ComponentMessagingTest1.waitButton4.await();

        ComponentMessagingTest1.waitButton4 = new CountDownLatch(2);
        org.jacp.test.perspectives.PerspectiveMessagingTest.MoveC1FromP3ToP1();
        ComponentMessagingTest1.waitButton4.await();

        // should throw non unique component exception
       CustomErrorDialogHandler.latch = new CountDownLatch(1);
        org.jacp.test.perspectives.PerspectiveMessagingTest.MoveC1FromP1ToP2();
       CustomErrorDialogHandler.latch.await();
    }


    public void run(Runnable r) {
        long start = System.currentTimeMillis();
        IntStream.rangeClosed(1, 500).forEach(i->r.run());
        long end = System.currentTimeMillis();

        System.out.println("Execution  time was " + (end - start) + " ms.");
    }
    public static class CustomErrorDialogHandler extends AErrorDialogHandler {
        public static CountDownLatch latch = new CountDownLatch(1);
        @Override
        public Node createExceptionDialog(Throwable e) {
            System.out.println("ERROR "+e.getMessage());
            //
           // TestCase.assertTrue(e.getMessage().contains("more than one component found for id"));
            latch.countDown();
            Platform.exit();

            return null;
        }
    }

}
