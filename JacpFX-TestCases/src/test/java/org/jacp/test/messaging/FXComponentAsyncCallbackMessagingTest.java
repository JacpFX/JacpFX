package org.jacp.test.messaging;

import javafx.application.Platform;
import junit.framework.Assert;
import org.jacp.test.AllTests;
import org.jacp.test.components.AsyncCallbackComponentMessagingTest1Component1;
import org.jacp.test.components.AsyncCallbackComponentMessagingTest1Component2;
import org.jacp.test.components.CallbackComponentMessagingTest1Component2;
import org.jacp.test.main.ApplicationLauncherAsyncCallbackComponentMessaginTest1;
import org.jacp.test.main.ApplicationLauncherCallbackComponentMessaginTest1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */
public class FXComponentAsyncCallbackMessagingTest {
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

                ApplicationLauncherAsyncCallbackComponentMessaginTest1.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try
        {
            ApplicationLauncherAsyncCallbackComponentMessaginTest1.latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void executeMessaging() throws InterruptedException {
        AsyncCallbackComponentMessagingTest1Component1.wait= new CountDownLatch(1);
        AsyncCallbackComponentMessagingTest1Component2.wait= new CountDownLatch(1);

        AsyncCallbackComponentMessagingTest1Component1.counter = new AtomicInteger(10000);
        AsyncCallbackComponentMessagingTest1Component2.counter = new AtomicInteger(10000);

        AsyncCallbackComponentMessagingTest1Component1.fireMessage();

        AsyncCallbackComponentMessagingTest1Component1.wait.await();
        AsyncCallbackComponentMessagingTest1Component2.wait.await();

    }

    private void warmUp() throws InterruptedException {
        executeMessaging();
    }

    @Test
    // default Execution time was 10959 ms..  linux ...
    public void testComponentMessaging() throws InterruptedException {
        warmUp();
        withoutUI();
    }
    @Test
    // 3300ms linux
    public void testBurstMessaging() throws InterruptedException {
        long start = System.currentTimeMillis();
        AsyncCallbackComponentMessagingTest1Component1.wait= new CountDownLatch(1);
        AsyncCallbackComponentMessagingTest1Component2.wait= new CountDownLatch(1);
        AsyncCallbackComponentMessagingTest1Component1.counter = new AtomicInteger(0);
        AsyncCallbackComponentMessagingTest1Component2.counter = new AtomicInteger(200000);
        AsyncCallbackComponentMessagingTest1Component2.MESSAGE=null;
        AsyncCallbackComponentMessagingTest1Component1.fireBurst(200000);

        //AsyncCallbackComponentMessagingTest1Component1.wait.await();
        AsyncCallbackComponentMessagingTest1Component2.wait.await();
        long end = System.currentTimeMillis();

        System.out.println("Execution testBurstMessaging time was "+(end-start)+" ms.");
    }



    private void withoutUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i=0;
        while(i<10){
            executeMessaging();
            Assert.assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution without ui time was "+(end-start)+" ms.");
    }
}
