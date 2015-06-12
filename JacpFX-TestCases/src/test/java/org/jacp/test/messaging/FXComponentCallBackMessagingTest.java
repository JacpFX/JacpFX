package org.jacp.test.messaging;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.CallbackComponentMessagingTest1Component1;
import org.jacp.test.components.CallbackComponentMessagingTest1Component2;
import org.jacp.test.perspectives.PerspectiveCallbackComponentMessagingTest1;
import org.jacp.test.workbench.WorkbenchCallbackComponentMessageTesting1;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 10.09.13
 * Time: 21:48
 * Tests messaging between callback component
 */
public class FXComponentCallBackMessagingTest extends TestFXJacpFXSpringLauncher {


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
        return WorkbenchCallbackComponentMessageTesting1.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

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
