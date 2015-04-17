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

package org.jacp.test.handler;

import org.jacpfx.rcp.context.AsyncHandler;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.04.15.
 */
public class AsyncHandlerTest {
    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @Test
    public void basicHandlerTest() throws InterruptedException {
        AsyncHandler handler = AsyncHandler.getInstance();
        System.err.println("THREAD: " + Thread.currentThread());
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        CountDownLatch latch6 = new CountDownLatch(1);
        CountDownLatch latch7 = new CountDownLatch(1);
        handler.onExecutorThread(() -> {
            try {
                System.out.println("THREAD POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);
                latch2.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        });
        handler.onFXThread(() -> {
                    try {
                        latch2.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("THREAD FX1: " + Thread.currentThread());
                    latch3.countDown();
                    return "hello";
                }
        );
        handler.onFXThread((Consumer) (value) -> {
            System.out.println("THREAD FX2: " + Thread.currentThread());
            System.out.println("----" + value);
            try {
                latch3.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            latch4.countDown();
        });
        handler.onExecutorThread(() -> {
            System.out.println("THREAD POOL 2: " + Thread.currentThread());
            try {
                latch4.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch5.countDown();
            return "XXX";
        });
        handler.execute((Consumer) (value) -> {
            try {
                latch5.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch6.countDown();
            System.out.println("END:: " + value);
        });
        latch1.countDown();
        System.out.println("---------XXXXXXXXX");

        latch1.await();
        latch6.await();

    }
}
