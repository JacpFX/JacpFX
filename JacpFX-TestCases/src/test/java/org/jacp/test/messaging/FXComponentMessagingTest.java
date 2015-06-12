package org.jacp.test.messaging;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.ComponentMessagingTest1Component1;
import org.jacp.test.components.ComponentMessagingTest1Component2;
import org.jacp.test.perspectives.PerspectiveComponentMessagingTest1;
import org.jacp.test.workbench.WorkbenchComponentMessageTesting1;
import org.jacpfx.rcp.workbench.FXWorkbench;
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
public class FXComponentMessagingTest extends TestFXJacpFXSpringLauncher {


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
        return WorkbenchComponentMessageTesting1.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }

    private void executeMessaging() throws InterruptedException {
        ComponentMessagingTest1Component1.wait = new CountDownLatch(1);
        ComponentMessagingTest1Component2.wait = new CountDownLatch(1);

        ComponentMessagingTest1Component1.counter = new AtomicInteger(10000);
        ComponentMessagingTest1Component2.counter = new AtomicInteger(10000);

        PerspectiveComponentMessagingTest1.fireMessage();

        ComponentMessagingTest1Component1.wait.await();
        ComponentMessagingTest1Component2.wait.await();

    }

    private void warmUp() throws InterruptedException {
        executeMessaging();
    }

    @Test
    // default Execution with ui time was 82442/82854/79245 ms.. with ui --  without ui 41751/41750/40689 linux ... / mac ui 26120 ... non ui 21269ms    ---windows ui 121747ms  --- nonui 65836ms
    // before change windows: 121747ms with ui, ... 65836ms without ui
    // before change linux: 79245ms with ui, ... 40689 without ui
    // before change osx: 26120ms with ui, ... 21269ms without ui
    // after change windows: 53912ms with ui,   16962ms without ui
    // after change linux: 56105ms with ui ... 28301ms without ui
    // after change osx: 13961 with ui ... 10158 without ui
    //macbook : 8100ms,7891,7550,7246,17399,17428,16880,7418 with ui ...   6131,6061,5907,5862,5892,5778,5941,5764 without ui
    public void testComponentMessaging() throws InterruptedException {
        warmUp();
        withUI();
        withoutUI();
    }

    private void withUI() throws InterruptedException {
        long start = System.currentTimeMillis();
        int i = 0;
        ComponentMessagingTest1Component1.ui = true;
        ComponentMessagingTest1Component2.ui = true;
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
        ComponentMessagingTest1Component1.ui = false;
        ComponentMessagingTest1Component2.ui = false;
        while (i < 10) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution without ui time was " + (end - start) + " ms.");
    }
}
