/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXWorkerExceptionTest.java]
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

package org.jacpfx;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jacpfx.concurrency.FXWorker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.service.query.NodeQuery;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andy Moncsek on 02.09.15.
 */
public class FXWorkerExceptionTest extends ApplicationTest {

    Pane mainPane = new Pane();

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }


    @Before
    public void onStart() {

    }

    @Test
    public void invokeAndWaitExceptionTest() throws InterruptedException {
        try {
            FXWorker.invokeOnFXThreadAndWait(() -> {
                System.out.println("before----------");
                String value = null;
                value.toString(); // Throw exception
                System.out.println("after----------");

            });
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("exception----------");
        }
    }

    @Test
    public void basicHandlerTest() throws InterruptedException {

        FXWorker<?> handler = FXWorker.getInstance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);


        handler.
                supplyOnFXThread(() -> basicHandlerTestStep1(latch1)).
                onError(o -> basicHandlerTestStep2(latch2, o)).
                execute();

        System.out.println("---------XXXXXXXXX------------------");

        latch1.await();
        System.out.println("---------XXXXXXXXX------------------  1111");
        latch2.await();
        System.out.println("---------pass 1----------------------");

    }



    @Test
    public void mutipleFXHandlerTest() throws InterruptedException {

        FXWorker<?> handler = FXWorker.getInstance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        CountDownLatch latch6 = new CountDownLatch(1);


        handler.supplyOnFXThread(() -> mutipleFXHandlerTestStep1(latch1))
                .onError(o -> mutipleFXHandlerTestStep2(latch2))
                .consumeOnFXThread(val -> mutipleFXHandlerTestStep3(latch3, val))
                .supplyOnFXThread(() -> 1)
                .functionOnFXThread(val -> mutipleFXHandlerTestStep4(latch4, val))
                .onError(o -> mutipleFXHandlerTestStep5(latch5))
                .consumeOnFXThread(val -> mutipleFXHandlerTestStep6(latch6, val))
                .execute();

        System.out.println("---------XXXXXXXXX------------------");

        latch1.await();
        System.out.println("latch 1 --------------");
        latch2.await();
        System.out.println("latch 2 --------------");
        latch3.await();
        System.out.println("latch 3 --------------");
        latch4.await();
        System.out.println("latch 4 --------------");
        latch5.await();
        System.out.println("latch 5 --------------");
        latch6.await();
        System.out.println("latch 6 --------------");

    }



    @Test
    public void mutipleExecutorAndFXHandlerTest() throws InterruptedException {

        FXWorker<?> handler = FXWorker.getInstance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        CountDownLatch latch6 = new CountDownLatch(1);


        handler
                .supplyOnExecutorThread(() -> {
                    try {
                        System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                        TimeUnit.MILLISECONDS.sleep(2000);
                        latch1.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "abc";
                })
                .functionOnFXThread(val -> {
                    if (val.equals("abc")) {
                        latch2.countDown();
                    }
                    Button b1 = null; // NPE !!!
                    b1.setId("hello");
                    return b1;

                })
                .onError(th -> {
                    Button b1 = new Button("hello");
                    b1.setId("hello");
                    latch3.countDown();
                    return b1;
                })
                .functionOnFXThread(button -> {
                    mainPane.getChildren().add(button);
                    return "OK";
                })
                .consumeOnFXThread(val -> {
                    if (val.equals("OK")) {
                        latch4.countDown();
                    }
                })
                .supplyOnExecutorThread(() -> {
                    Button b1 = null; // NPE !!!
                    b1.setId("hello");
                    return b1;
                })
                .onError(tn -> {
                    latch3.countDown();
                    Button b1 = new Button("hello2");
                    b1.setId("hello2");
                    latch5.countDown();
                    return b1;
                })
                .execute(val -> {
                    mainPane.getChildren().add(val);
                    latch6.countDown();
                });

        System.out.println("---------XXXXXXXXX------------------");

        latch1.await();
        System.out.println("latch 1 --------------");
        latch2.await();
        System.out.println("latch 2 --------------");
        latch3.await();
        System.out.println("latch 3 --------------");
        latch4.await();
        System.out.println("latch 4 --------------");
        latch5.await();
        System.out.println("latch 5 --------------");
        latch6.await();
        System.out.println("latch 6 --------------");

        NodeQuery button = lookup("#hello");
        Assert.assertTrue(button.tryQueryFirst().isPresent());

        NodeQuery button1 = lookup("#hello2");
        Assert.assertTrue(button1.tryQueryFirst().isPresent());

    }


    private String basicHandlerTestStep2(CountDownLatch latch2, Throwable o) {
        o.printStackTrace();
        latch2.countDown();
        return o.getMessage();
    }

    private String basicHandlerTestStep1(CountDownLatch latch1) {
        try {
            System.out.println("THREAD POOL 1: " + Thread.currentThread());
            TimeUnit.MILLISECONDS.sleep(500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        latch1.countDown();
        String test = null;
        test.toString(); // cause a nullpointer !!
        return "abc";
    }

    private String mutipleFXHandlerTestStep1(CountDownLatch latch1) {
        try {
            System.out.println("THREAD POOL 1: " + Thread.currentThread());
            TimeUnit.MILLISECONDS.sleep(2000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        latch1.countDown();
        String test = null;
        test.toString(); // cause a nullpointer !!
        return "abc";
    }

    private String mutipleFXHandlerTestStep2(CountDownLatch latch2) {
        System.out.println("Error 1----------");
        // o.printStackTrace();
        latch2.countDown();
        return "cde";
    }

    private void mutipleFXHandlerTestStep3(CountDownLatch latch3, String val) {
        if (val.equalsIgnoreCase("cde")) {
            latch3.countDown();
        }
    }

    private Integer mutipleFXHandlerTestStep4(CountDownLatch latch4, Integer val) {
        if (val == 1) {
            latch4.countDown();
        }
        String test = null;
        test.toString(); // cause a nullpointer !!
        return 2;
    }

    private Integer mutipleFXHandlerTestStep5(CountDownLatch latch5) {
        System.out.println("Error 2----------");
        //o.printStackTrace();
        latch5.countDown();
        return 3;
    }

    private void mutipleFXHandlerTestStep6(CountDownLatch latch6, Integer val) {
        if (val == 3) {
            latch6.countDown();
        }
    }


    private void consume(String myVal) {
        Assert.assertTrue(myVal.equals(new String("abs")));
    }


}