package org.jacp.test.messaging;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.components.CallbackComponentMessagingTest1Component1;
import org.jacp.test.components.CallbackComponentMessagingTest1Component2;
import org.jacp.test.main.ApplicationLauncherCallbackComponentMessaginTest1;
import org.jacp.test.perspectives.PerspectiveCallbackComponentMessagingTest1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 10.09.13
 * Time: 21:48
 * Tests messaging between callback components
 */
public class FXComponentCallBackMessagingTest {
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

                ApplicationLauncherCallbackComponentMessaginTest1.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherCallbackComponentMessaginTest1.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeMessaging() throws InterruptedException {
        CallbackComponentMessagingTest1Component1.wait = new CountDownLatch(1);
        CallbackComponentMessagingTest1Component2.wait = new CountDownLatch(1);

        CallbackComponentMessagingTest1Component1.counter = new AtomicInteger(10000);
        CallbackComponentMessagingTest1Component2.counter = new AtomicInteger(10000);

        PerspectiveCallbackComponentMessagingTest1.fireMessage();

        CallbackComponentMessagingTest1Component1.wait.await();
        CallbackComponentMessagingTest1Component2.wait.await();

    }

    private void warmUp() throws InterruptedException {
        executeMessaging();
    }

    @Test
    // default Execution time was 6147 ms..  linux ...
    // default Execution time 2302 ms OSX
    // 2221ms macbook
    public void testComponentMessaging() throws InterruptedException {
        warmUp();
        withoutUI();
    }

    @Test
    // 1805ms in linux
    // 1049 ms in OSX
    // 875 macbook
    public void testBurstMessaging() throws InterruptedException {
        long start = System.currentTimeMillis();
        CallbackComponentMessagingTest1Component1.wait = new CountDownLatch(1);
        CallbackComponentMessagingTest1Component2.wait = new CountDownLatch(1);
        CallbackComponentMessagingTest1Component1.counter = new AtomicInteger(0);
        CallbackComponentMessagingTest1Component2.counter = new AtomicInteger(200000);
        CallbackComponentMessagingTest1Component2.MESSAGE = null;
        PerspectiveCallbackComponentMessagingTest1.fireBurst(200000);

        CallbackComponentMessagingTest1Component2.wait.await();
        long end = System.currentTimeMillis();

        System.out.println("Execution testBurstMessaging time was " + (end - start) + " ms.");
    }


    private void withoutUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i = 0;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution without ui time was " + (end - start) + " ms.");
    }
}
