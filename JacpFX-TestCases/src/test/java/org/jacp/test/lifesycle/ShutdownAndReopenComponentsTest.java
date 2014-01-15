package org.jacp.test.lifesycle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacp.test.AllTests;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests1;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests2;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests3;
import org.jacp.test.main.ApplicationShutdownAndRestartComponentsTest;
import org.jacp.test.perspectives.PerspectiveShutdownAndRestartComponents;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 13.11.13
 * Time: 21:37
 * To change this template use File | Settings | File Templates.
 */
public class ShutdownAndReopenComponentsTest {

    static Thread t;

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        AllTests.resetApplication();


    }

    @BeforeClass
    public static void initWorkbench() {
        ApplicationShutdownAndRestartComponentsTest.exceptionhandler = new CustomErrorDialogHandler();

        t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationShutdownAndRestartComponentsTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationShutdownAndRestartComponentsTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    /**
     * check if component is active
     */
    public void test1() {

        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
            assertFalse(components.isEmpty());
            components.forEach(c -> assertTrue(c.getContext().isActive()));
        }
    }

    @Test
    /**
     * Test shutDownComponent
     */
    public void test2() throws InterruptedException {
        // Component is active
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
            assertFalse(components.isEmpty());

            components.forEach(c -> assertTrue(c.getContext().isActive()));
        }

        testStopComponent();


    }

    private void testStopComponent() throws InterruptedException {
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests2.stopLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests3.stopLatch = new CountDownLatch(1);

        PerspectiveShutdownAndRestartComponents.stopFXComponent();
        ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
        ComponentShutdownAndRestartComponentsTests2.stopLatch.await();
        ComponentShutdownAndRestartComponentsTests3.stopLatch.await();
        perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            //Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
    }

    @Test
    /**
     * restartComponentTest
     */
    public void test3() throws InterruptedException {
        // Component is shut down
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            // Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
        int i = 0;
        while (i <100) {
            testStartComponent();
            testStopComponent();
            i++;
        }

    }

    private void testStartComponent() throws InterruptedException {
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        ComponentShutdownAndRestartComponentsTests1.startLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests2.startLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests3.startLatch = new CountDownLatch(1);

        PerspectiveShutdownAndRestartComponents.startComponent();
        ComponentShutdownAndRestartComponentsTests1.startLatch.await();
        ComponentShutdownAndRestartComponentsTests2.startLatch.await();
        ComponentShutdownAndRestartComponentsTests3.startLatch.await();
        //Thread.sleep(100);
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
            assertFalse(components.isEmpty());

            components.forEach(c -> assertTrue(c.getContext().isActive()));
        }
    }

    /**
     * stop message followed by a start message
     */
    @Test
    public void test4() throws InterruptedException {
        // Component is shut down
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            Thread.sleep(100);
            assertTrue(components.isEmpty());

        }

        int i = 0;
        while (i < 500) {
            ComponentShutdownAndRestartComponentsTests1.startLatch = new CountDownLatch(1);
            ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopStartFXComponent();
            ComponentShutdownAndRestartComponentsTests1.startLatch.await();
            ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
            //exceptionLatch.await();
           // Thread.sleep(10);
            i++;
        }
        System.out.println("");
    }

    @Test
    public void test5() throws InterruptedException {

        // Component is shut down
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];

        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            // Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
        int i = 0;
        while (i < 500) {
            CustomErrorDialogHandler.exceptionLatch = new CountDownLatch(1);

            ComponentShutdownAndRestartComponentsTests2.startLatch = new CountDownLatch(1);
            ComponentShutdownAndRestartComponentsTests2.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopStartComponent();
            ComponentShutdownAndRestartComponentsTests2.startLatch.await();
            ComponentShutdownAndRestartComponentsTests2.stopLatch.await();
            CustomErrorDialogHandler.exceptionLatch.await();
            i++;
        }
    }



    @Test              // test sending messages, stop the component while new messages are send
    public void test6() throws InterruptedException {
        // Component is shut down
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            // Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
        int i = 0;
        while (i < 500) {

            ComponentShutdownAndRestartComponentsTests1.startLatch = new CountDownLatch(1);
           // ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            int j=0;
            while (j<10){
                ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
                PerspectiveShutdownAndRestartComponents.startStopComponent();
                j++;
                ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
            }


            ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopFXComponent();
            ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
           /* ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
            int p=0;
            while (p<10){

                PerspectiveShutdownAndRestartComponents.startStopComponent();
                p++;
            }
            ComponentShutdownAndRestartComponentsTests1.stopLatch.await();*/
          //  exceptionLatch.await();
            i++;
        }
    }


    @Test
    @Ignore
    public void test7() throws InterruptedException {
        // Component is shut down
        ApplicationShutdownAndRestartComponentsTest launcher = ApplicationShutdownAndRestartComponentsTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);

        int i = 0;
        while (i < 100) {

            ComponentShutdownAndRestartComponentsTests3.startLatch = new CountDownLatch(1);
            ComponentShutdownAndRestartComponentsTests3.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopStartSatelessComponent();
            ComponentShutdownAndRestartComponentsTests3.startLatch.await();
            ComponentShutdownAndRestartComponentsTests3.stopLatch.await();

            i++;
        }
        Thread.sleep(2000);
    }


}
