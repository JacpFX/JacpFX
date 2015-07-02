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

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jacpfx.rcp.context.AsyncHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.service.query.NodeQuery;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Andy Moncsek on 17.04.15.
 */
public class AsyncHandlerTest extends ApplicationTest {

    Pane mainPane = new Pane();

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @Before
    public void onStart() {

    }

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
        handler.supplyOnExecutorThread(() -> {
            try {
                System.out.println("THREAD POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);
                latch2.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "abc";
        });
        handler.supplyOnFXThread(() -> {
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
        handler.consumeOnFXThread((Consumer) (value) -> {
            System.out.println("THREAD FX2: " + Thread.currentThread());
            System.out.println("----" + value);
            try {
                latch3.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            latch4.countDown();
        });
        handler.supplyOnExecutorThread(() -> {
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


        System.out.println("---------XXXXXXXXX------------------");

        latch6.await();
        System.out.println("---------pass 1----------------------");

    }

    @Test
    public void executeHandlerTest() throws InterruptedException {
        AsyncHandler<Object> handler = AsyncHandler.getInstance();
        CountDownLatch latch5 = new CountDownLatch(1);
        handler.supplyOnExecutorThread(() -> {
            try {
                System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "abc";
        }).consumeOnFXThread((value) -> {
            System.out.println("-- THREAD consume FX1: " + Thread.currentThread());
            System.out.println("----" + value);


        }).supplyOnFXThread(() -> {

                    System.out.println("-- THREAD supply FX2: " + Thread.currentThread());

                    return "hello";
                }
        ).consumeOnExecutorThread((val) -> {
            System.out.println("-- THREAD CONSUME POOL 2: " + Thread.currentThread());


        }).execute((cc) -> {
            System.out.println("STOP");
            latch5.countDown();
        });


        System.out.println("---------XXXXXXXXX------------------");

        latch5.await();
        System.out.println("---------pass 2----------------------");
        Assert.assertTrue(true);
    }

    @Test
    public void executeSupplierOnFXThread() throws InterruptedException {
        AsyncHandler<Object> handler = AsyncHandler.getInstance();
        CountDownLatch latch5 = new CountDownLatch(1);
        handler.supplyOnExecutorThread(() -> {
            try {
                System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "abc";
        }).consumeOnFXThread((value) -> {
            System.out.println("-- THREAD consume FX1: " + Thread.currentThread());
            System.out.println("----" + value);


        }).supplyOnFXThread(() -> {

                    System.out.println("-- THREAD supply FX2: " + Thread.currentThread());

                    return new Button("ok");
                }
        ).consumeOnFXThread((val) -> {
            System.out.println("-- THREAD CONSUME FX3 : " + Thread.currentThread() + "  val:" + val);


        }).execute((cc) -> {
            System.out.println("STOP");
            latch5.countDown();
        });


        System.out.println("---------XXXXXXXXX------------------");

        latch5.await();
        System.out.println("---------pass 3----------------------");
        Assert.assertTrue(true);
    }

    @Test
    public void testAddNodes() throws InterruptedException {
        AsyncHandler<Object> handler = AsyncHandler.getInstance();
        CountDownLatch latch5 = new CountDownLatch(1);
        handler.supplyOnExecutorThread(() -> {
            try {
                System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                TimeUnit.MILLISECONDS.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "abc";
        }).consumeOnFXThread((value) -> {
            System.out.println("-- THREAD consume FX1: " + Thread.currentThread());
            System.out.println("----" + value);
            Button b1 = new Button(value);
            b1.setId(value);
            mainPane.getChildren().add(b1);

        }).supplyOnFXThread(() -> {

                    System.out.println("-- THREAD supply FX2: " + Thread.currentThread());
                    Button ok = new Button("ok");
                    ok.setId("ok");
                    return ok;
                }
        ).consumeOnFXThread((val) -> {
            System.out.println("-- THREAD CONSUME FX3 : " + Thread.currentThread() + "  val:" + val);
            mainPane.getChildren().add(val);

        }).execute((cc) -> {
            System.out.println("STOP");
            latch5.countDown();
        });


        System.out.println("---------XXXXXXXXX------------------");

        latch5.await();

        NodeQuery button = lookup("#abc");
        Assert.assertTrue(button.tryQueryFirst().isPresent());

        NodeQuery button1 = lookup("#ok");
        Assert.assertTrue(button1.tryQueryFirst().isPresent());

        System.out.println("---------pass 4----------------------");
        Assert.assertTrue(true);
        Collections.emptyList().stream().map(val -> new Button(val.toString())).collect(Collectors.toList());
    }

    @Test
    public void testTypes() {

        AsyncHandler<Object> handler = AsyncHandler.getInstance();
        handler.
                supplyOnExecutorThread(() -> new Integer(3)).
                consumeOnExecutorThread((intVal) -> Assert.assertTrue(intVal.equals(new Integer(3)))).
                supplyOnFXThread(() -> new String("abs")).
                consumeOnFXThread(this::consume).
                supplyOnFXThread(() -> new Integer(3)).execute();
    }

    @Test
    public void testExecuteOnWorkerThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable r = () -> {
            AsyncHandler<Object> handler = AsyncHandler.getInstance();
            handler.
                    supplyOnExecutorThread(() -> new Integer(3)).
                    consumeOnExecutorThread((intVal) -> Assert.assertTrue(intVal.equals(new Integer(3)))).
                    supplyOnFXThread(() -> new String("abs")).
                    consumeOnFXThread(this::consume).
                    supplyOnFXThread(() -> new Integer(3)).execute(() -> {
                System.out.println(Platform.isFxApplicationThread() + "  Thread:" + Thread.currentThread());
                Assert.assertTrue(Platform.isFxApplicationThread());
                latch.countDown();
            });
        };
        new Thread(r).start();
        latch.await();

    }

    @Test
    public void testExecuteOnWorkerThread2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable r = () -> {
            AsyncHandler<Object> handler = AsyncHandler.getInstance();
            handler.
                    supplyOnExecutorThread(() -> new Integer(3)).
                    consumeOnExecutorThread((intVal) -> Assert.assertTrue(intVal.equals(new Integer(3)))).
                    supplyOnFXThread(() -> new String("abs")).
                    consumeOnFXThread(this::consume).
                    supplyOnFXThread(() -> new Integer(3)).execute((val) -> {
                System.out.println(Platform.isFxApplicationThread() + "  Thread:" + Thread.currentThread() + " value" + val);
                Assert.assertTrue(Platform.isFxApplicationThread());
                latch.countDown();
            });
        };
        new Thread(r).start();
        latch.await();

    }

    @Test
    public void testExecuteOnWorkerThread3() throws InterruptedException {
        AsyncHandler<?> handler = AsyncHandler.getInstance();
        CountDownLatch latch5 = new CountDownLatch(1);
        Runnable r = () -> {
            handler.supplyOnExecutorThread(() -> {
                try {
                    System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                    TimeUnit.MILLISECONDS.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "abc";
            }).consumeOnFXThread((value) -> {
                System.out.println("-- THREAD consume FX1: " + Thread.currentThread());
                System.out.println("----" + value);
                Button b1 = new Button(value);
                b1.setId(value);
                mainPane.getChildren().add(b1);

            }).supplyOnFXThread(() -> {

                        System.out.println("-- THREAD supply FX2: " + Thread.currentThread());
                        Button ok = new Button("ok");
                        ok.setId("ok");
                        return ok;
                    }
            ).execute((cc) -> {
                mainPane.getChildren().add(cc);
                System.out.println("STOP");
                latch5.countDown();
            });
        };
        new Thread(r).start();

        System.out.println("---------XXXXXXXXX------------------");

        latch5.await();

        NodeQuery button = lookup("#abc");
        Assert.assertTrue(button.tryQueryFirst().isPresent());

        NodeQuery button1 = lookup("#ok");
        Assert.assertTrue(button1.tryQueryFirst().isPresent());

        System.out.println("---------pass 4----------------------");
        Assert.assertTrue(true);
        Collections.emptyList().stream().map(val -> new Button(val.toString())).collect(Collectors.toList());
    }


    @Test
    public void testExecuteFunctionOnWorkerThread() throws InterruptedException {
        AsyncHandler<?> handler = AsyncHandler.getInstance();
        CountDownLatch latch5 = new CountDownLatch(1);
        Runnable r = () -> {
            handler.supplyOnExecutorThread(() -> {
                try {
                    System.out.println("-- THREAD SUPPLY POOL 1: " + Thread.currentThread());
                    TimeUnit.MILLISECONDS.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "abc";
            }).functionOnExecutorThread((input) -> {
                try {
                    System.out.println("-- THREAD functionOnExecutorThread POOL 1: " + Thread.currentThread());
                    TimeUnit.MILLISECONDS.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return input;
            }).functionOnFXThread(i -> {
                System.out.println("-- THREAD functionOnFXThread FX1: " + Thread.currentThread());
                System.out.println("----" + i);
                Button b1 = new Button(i);
                b1.setId(i);
                mainPane.getChildren().add(b1);
                return i;
            }).consumeOnFXThread((value) -> {
                System.out.println("-- THREAD consume FX1: " + Thread.currentThread());
                System.out.println("----" + value);
                Button b1 = new Button(value+1);
                b1.setId(value+1);
                mainPane.getChildren().add(b1);

            }).supplyOnFXThread(() -> {

                        System.out.println("-- THREAD supply FX2: " + Thread.currentThread());
                        Button ok = new Button("ok");
                        ok.setId("ok");
                        return ok;
                    }
            ).execute((cc) -> {
                mainPane.getChildren().add(cc);
                System.out.println("STOP");
                latch5.countDown();
            });
        };
        new Thread(r).start();

        System.out.println("---------XXXXXXXXX------------------");

        latch5.await();

        NodeQuery button = lookup("#abc");
        Assert.assertTrue(button.tryQueryFirst().isPresent());

        NodeQuery button1 = lookup("#ok");
        Assert.assertTrue(button1.tryQueryFirst().isPresent());

        NodeQuery button2 = lookup("#abc1");
        Assert.assertTrue(button2.tryQueryFirst().isPresent());

        System.out.println("---------pass 4----------------------");
        Assert.assertTrue(true);
        Collections.emptyList().stream().map(val -> new Button(val.toString())).collect(Collectors.toList());
    }


    private void consume(String myVal) {
        Assert.assertTrue(myVal.equals(new String("abs")));
    }


}
