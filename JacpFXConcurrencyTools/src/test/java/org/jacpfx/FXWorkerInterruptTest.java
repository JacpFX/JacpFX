/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXWorkerInterruptTest.java]
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jacpfx.concurrency.FXWorker;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 02.09.15.
 */
public class FXWorkerInterruptTest extends ApplicationTest {

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
    public void basicCancel() throws InterruptedException {

        FXWorker<?> handler = FXWorker.instance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);


        FXWorker<?> handlerTMP = handler.
                supplyOnExecutorThread(() -> {
                    System.out.println("---------b1 before------------------  ");
                    sleep(1000);
                    System.out.println("---------b1 after------------------  ");
                    return "";
                }).
                supplyOnExecutorThread(() -> {
                    System.out.println("---------b2 before------------------  ");
                    sleep(1000);
                    System.out.println("---------b2 after------------------  ");
                    return "";
                });
        handlerTMP.cancel();
        handlerTMP.execute(()-> System.out.println("finish"));
        System.out.println("---------XXXXXXXXX------------------");
        handlerTMP.cancel();
         sleep(5000);
        System.out.println("---------YYYYYYYY------------------ ");

        System.out.println("---------pass 1----------------------");

    }




}