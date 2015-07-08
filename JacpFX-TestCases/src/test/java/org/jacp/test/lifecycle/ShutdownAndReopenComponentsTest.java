package org.jacp.test.lifecycle;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests1;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests2;
import org.jacp.test.components.ComponentShutdownAndRestartComponentsTests3;
import org.jacp.test.perspectives.PerspectiveShutdownAndRestartComponents;
import org.jacp.test.workbench.WorkbenchShutdownAndReopenComponentsTest;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
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
public class ShutdownAndReopenComponentsTest extends TestFXJacpFXSpringLauncher {




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
        return WorkbenchShutdownAndReopenComponentsTest.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }

    @Test
    /**
     * check if component is active
     */
    public void test1() {

        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

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
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
            assertFalse(components.isEmpty());

            components.forEach(c -> assertTrue(c.getContext().isActive()));
        }

        testStopComponent();

    }

    @Test
    /**
     * restartComponentTest
     */
    public void test3() throws InterruptedException {
        // Component is shut down
        AFXWorkbench workbench =  getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        testStopComponent();
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

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

    /**
     * stop message followed by a start message
     */
    @Test
    public void test4() throws InterruptedException {
        // Component is shut down
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        testStopComponent();
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

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
        Thread.sleep(1000);
        System.out.println("");
    }
    @Test
    public void test5() throws InterruptedException {

        // Component is shut down

        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
       // testStartComponent();
       // testStopComponent();
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            // Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
        int i = 0;
        while (i < 500) {

            ComponentShutdownAndRestartComponentsTests2.startLatch = new CountDownLatch(1);
            ComponentShutdownAndRestartComponentsTests2.stopLatch = new CountDownLatch(1);
            PerspectiveShutdownAndRestartComponents.stopStartComponent();
            ComponentShutdownAndRestartComponentsTests2.startLatch.await();
            ComponentShutdownAndRestartComponentsTests2.stopLatch.await();
            i++;
        }
    }


    @Test              // test sending messages, stop the component while new messages are send
    public void test6() throws InterruptedException {
        // Component is shut down
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        testStopComponent();
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            // Thread.sleep(1000);
            assertTrue(components.isEmpty());

        }
        int i = 0;
        while (i < 100) {

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

    private void testStartComponent() throws InterruptedException {
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
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
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
            assertFalse(components.isEmpty());

            components.forEach(c -> assertTrue(c.getContext().isActive()));
        }
    }

    private void testStopComponent() throws InterruptedException {

        ComponentShutdownAndRestartComponentsTests1.stopLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests2.stopLatch = new CountDownLatch(1);
        ComponentShutdownAndRestartComponentsTests3.stopLatch = new CountDownLatch(1);

        PerspectiveShutdownAndRestartComponents.stopFXComponent();

        ComponentShutdownAndRestartComponentsTests1.stopLatch.await();
        ComponentShutdownAndRestartComponentsTests2.stopLatch.await();
        ComponentShutdownAndRestartComponentsTests3.stopLatch.await();

        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();

            System.out.println(components+" THREAD:"+Thread.currentThread());
            assertTrue(components.isEmpty());

        }
    }




}
