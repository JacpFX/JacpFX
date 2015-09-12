/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXWorkerUpdateMessageTest.java]
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
import javafx.scene.control.Label;
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
public class FXWorkerUpdateMessageTest extends ApplicationTest {

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
    public void basicMessageUpdateTest() throws InterruptedException, ExecutionException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        CountDownLatch latch6 = new CountDownLatch(1);
        final FXWorker<?> handler = FXWorker.instance();


        Label messageLabel = new Label("---");
        messageLabel.setId("messageLabel");
        messageLabel.textProperty().bind(handler.messageProperty());
        FXWorker.invokeOnFXThreadAndWait(() -> mainPane.getChildren().add(messageLabel));


        handler.supplyOnExecutorThread(() -> {
            try {
                System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.updateMessage("pass1");
            latch1.countDown();

            return "abc";
        }).functionOnExecutorThread((input) -> {
            try {
                System.out.println("-- THREAD functionOnExecutorThread POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.updateMessage("pass2");
            latch2.countDown();
            return input;
        }).functionOnFXThread(i -> {
            System.out.println("-- THREAD functionOnFXThread FX1: " + Thread.currentThread());
            System.out.println("----" + i);
            Button b1 = new Button(i);
            b1.setId(i);
            mainPane.getChildren().add(b1);
            handler.updateMessage("pass3");
            latch3.countDown();
            return i;
        }).consumeOnFXThread((value) -> {
            System.out.println("-- THREAD consume FX1: " + Thread.currentThread());
            System.out.println("----" + value);
            Button b1 = new Button(value + 1);
            b1.setId(value + 1);
            mainPane.getChildren().add(b1);
            handler.updateMessage("pass4");
            latch4.countDown();

        }).supplyOnFXThread(() -> {

                    System.out.println("-- THREAD supply FX2: " + Thread.currentThread());
                    Button ok = new Button("ok");
                    ok.setId("ok");
                    handler.updateMessage("pass5");
                    latch5.countDown();
                    return ok;
                }
        ).execute((cc) -> {
            mainPane.getChildren().add(cc);
            System.out.println("STOP");
            handler.updateMessage("pass6");
            latch6.countDown();
        });
        latch1.await();

        NodeQuery message1 = lookup("#messageLabel");
        checkMessage(message1, "pass1", "stage1: ");


        latch2.await();
        NodeQuery message2 = lookup("#messageLabel");
        checkMessage(message2, "pass2", "stage2: ");



        latch3.await();
        NodeQuery message3 = lookup("#messageLabel");
        checkMessage(message3, "pass3", "stage3: ");

        latch4.await();
        NodeQuery message4 = lookup("#messageLabel");
        checkMessage(message4, "pass4", "stage4: ");


        latch5.await();
        NodeQuery message5 = lookup("#messageLabel");
        checkMessage(message5, "pass5", "stage5: ");


        latch6.await();
        NodeQuery message6 = lookup("#messageLabel");
        checkMessage(message6, "pass6", "stage6: ");


        NodeQuery button = lookup("#ok");
        Assert.assertTrue(button.tryQueryFirst().isPresent());

    }

    private void checkMessage(NodeQuery message1, String expectedValue, String stage) {
        Assert.assertTrue(message1.tryQueryFirst().isPresent());
        Label messageTemp1 = (Label) message1.tryQueryFirst().get();
        Assert.assertTrue(messageTemp1.getText().equalsIgnoreCase(expectedValue));
        System.out.println(stage + messageTemp1.getText());
    }


}