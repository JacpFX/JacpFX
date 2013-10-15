package org.jacp.test.messaging;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherPerspectiveMessaginTest;
import org.jacp.test.perspectives.PerspectiveMessagingTestP1;
import org.jacp.test.perspectives.PerspectiveMessagingTestP2;
import org.jacp.test.perspectives.PerspectiveMessagingTestP3;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class PerspectiveMessagingTest {
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

                ApplicationLauncherPerspectiveMessaginTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherPerspectiveMessaginTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeMessaging() throws InterruptedException {
        PerspectiveMessagingTestP1.wait = new CountDownLatch(1);
        PerspectiveMessagingTestP2.wait = new CountDownLatch(1);
        PerspectiveMessagingTestP3.wait = new CountDownLatch(1);
        PerspectiveMessagingTestP1.counter = new AtomicInteger(10000);
        PerspectiveMessagingTestP2.counter = new AtomicInteger(10000);
        PerspectiveMessagingTestP3.counter = new AtomicInteger(10000);
        PerspectiveMessagingTestP1.fireMessage();
        PerspectiveMessagingTestP1.wait.await();
        PerspectiveMessagingTestP2.wait.await();
        PerspectiveMessagingTestP3.wait.await();
    }

    private void warmUp() throws InterruptedException {
        executeMessaging();
    }

    @Test
    // default execution time was 54312 ms (linux) , macos 17826ms  // macos with 3 persp. and 300000 messages Execution time was 28494 ms.
    public void testPerspectiveMessaging() throws InterruptedException {
        warmUp();
        long start = System.currentTimeMillis();
        int i = 0;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution time was " + (end - start) + " ms.");
    }
}
