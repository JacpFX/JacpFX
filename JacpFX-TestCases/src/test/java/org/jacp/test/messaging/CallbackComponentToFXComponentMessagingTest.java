package org.jacp.test.messaging;

import javafx.application.Platform;
import org.jacp.test.NonUITests;
import org.jacp.test.components.ComponentToCallbackMessagingTest1Component1;
import org.jacp.test.components.ComponentToCallbackMessagingTest1Component2;
import org.jacp.test.main.ApplicationLauncherComponentToCallbackComponentMessaginTest1;
import org.jacp.test.perspectives.PerspectiveComponentToCallbackComponentMessagingTest1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 15.10.13
 * Time: 13:59
 * Messeging between UI and non UI component
 */
public class CallbackComponentToFXComponentMessagingTest {
    static Thread t;

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }

    @BeforeClass
    public static void initWorkbench() {


        t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationLauncherComponentToCallbackComponentMessaginTest1.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherComponentToCallbackComponentMessaginTest1.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeMessaging() throws InterruptedException {
        ComponentToCallbackMessagingTest1Component1.wait = new CountDownLatch(1);
        ComponentToCallbackMessagingTest1Component2.wait = new CountDownLatch(1);

        ComponentToCallbackMessagingTest1Component1.counter = new AtomicInteger(10000);
        ComponentToCallbackMessagingTest1Component2.counter = new AtomicInteger(10000);

        PerspectiveComponentToCallbackComponentMessagingTest1.fireMessage();

        ComponentToCallbackMessagingTest1Component1.wait.await();
        ComponentToCallbackMessagingTest1Component2.wait.await();

    }

    private void warmUp() throws InterruptedException {
        executeMessaging();
    }

    @Test
    // osx: 7995 with ui ... 4184 without ui
    // macbook 4174ms,3919ms with ui ... 2654ms without ui
    public void testComponentMessaging() throws InterruptedException {
        warmUp();
        withUI();
        withoutUI();
    }

    private void withUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i = 0;
        ComponentToCallbackMessagingTest1Component1.ui = true;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution with ui time was " + (end - start) + " ms.");
    }

    private void withoutUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i = 0;
        ComponentToCallbackMessagingTest1Component1.ui = false;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution without ui time was " + (end - start) + " ms.");
    }
}

