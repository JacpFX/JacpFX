package org.jacp.test.messaging;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.AsyncCallbackComponentMessagingTest1Component1;
import org.jacp.test.components.AsyncCallbackComponentMessagingTest1Component2;
import org.jacp.test.workbench.WorkbenchAsyncCallbackComponentMessageTesting1;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */
public class FXComponentAsyncCallbackMessagingTest extends TestFXJacpFXSpringLauncher {


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
        return WorkbenchAsyncCallbackComponentMessageTesting1.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }

    private void executeMessaging() throws InterruptedException {
        AsyncCallbackComponentMessagingTest1Component1.wait = new CountDownLatch(1);
        AsyncCallbackComponentMessagingTest1Component2.wait = new CountDownLatch(1);

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
    //  5211 ms osx
    // 3723ms macbook
    public void testComponentMessaging() throws InterruptedException {
        warmUp();
        withoutUI();
    }

    @Test
    // 3300ms linux
    // 2047 ms osx
    // 1550,399 macbook
    public void testBurstMessaging() throws InterruptedException {
        long start = System.currentTimeMillis();
        AsyncCallbackComponentMessagingTest1Component1.wait = new CountDownLatch(1);
        AsyncCallbackComponentMessagingTest1Component2.wait = new CountDownLatch(1);
        AsyncCallbackComponentMessagingTest1Component1.counter = new AtomicInteger(0);
        AsyncCallbackComponentMessagingTest1Component2.counter = new AtomicInteger(200000);
        AsyncCallbackComponentMessagingTest1Component2.MESSAGE = null;
        AsyncCallbackComponentMessagingTest1Component1.fireBurst(200000);

        //AsyncCallbackComponentMessagingTest1Component1.wait.await();
        AsyncCallbackComponentMessagingTest1Component2.wait.await();
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
