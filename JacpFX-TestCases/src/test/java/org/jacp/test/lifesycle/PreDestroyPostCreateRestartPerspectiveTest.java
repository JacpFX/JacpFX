package org.jacp.test.lifesycle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacp.test.AllTests;
import org.jacp.test.components.PredestroyTestComponentFour;
import org.jacp.test.components.PredestroyTestComponentOne;
import org.jacp.test.components.PredestroyTestComponentThree;
import org.jacp.test.components.PredestroyTestComponentTwo;
import org.jacp.test.main.ApplicationPredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveOnePredestroyPerspectiveTest;
import org.jacp.test.workbench.WorkbenchPredestroyPerspectiveTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 16.10.13
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public class PreDestroyPostCreateRestartPerspectiveTest {
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

                ApplicationPredestroyPerspectiveTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationPredestroyPerspectiveTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




    private static void fireMessagesAndCheck() throws InterruptedException {
        PredestroyTestComponentOne.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentTwo.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentThree.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentFour.countdownlatch = new CountDownLatch(10000);


        PerspectiveOnePredestroyPerspectiveTest.fireBurst(10000);

        PredestroyTestComponentOne.countdownlatch.await();

        PredestroyTestComponentTwo.countdownlatch.await();

        PredestroyTestComponentThree.countdownlatch.await();

        PredestroyTestComponentFour.countdownlatch.await();
        ApplicationPredestroyPerspectiveTest launcher = ApplicationPredestroyPerspectiveTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for(Perspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertTrue(p.getContext().isActive());
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }

    }

    private static void stopComponentsAndCheck(boolean burst)throws InterruptedException {
        ApplicationPredestroyPerspectiveTest launcher = ApplicationPredestroyPerspectiveTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        PerspectiveOnePredestroyPerspectiveTest.latch = new CountDownLatch(1);
        PredestroyTestComponentOne.latch = new CountDownLatch(1);
        PredestroyTestComponentTwo.latch = new CountDownLatch(1);
        PredestroyTestComponentThree.latch = new CountDownLatch(1);
        int val = getActiveAsyncCount(perspectives);
        System.out.println("active async: "+val);
        PredestroyTestComponentFour.latch = new CountDownLatch(val);
        PerspectiveOnePredestroyPerspectiveTest.stop();
        PerspectiveOnePredestroyPerspectiveTest.latch.await();
        PredestroyTestComponentOne.latch.await();
        PredestroyTestComponentTwo.latch.await();
        PredestroyTestComponentThree.latch.await();
        PredestroyTestComponentFour.latch.await();


        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for(Perspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertFalse(p.getContext().isActive());
                Thread.sleep(100);
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertTrue(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }
    }

    private static void restartComponentsAndCheck() throws InterruptedException {
        ApplicationPredestroyPerspectiveTest launcher = ApplicationPredestroyPerspectiveTest.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        PerspectiveOnePredestroyPerspectiveTest.startLatch = new CountDownLatch(1);
        PredestroyTestComponentFour.startLatch= new CountDownLatch(1);
        PredestroyTestComponentThree .startLatch= new CountDownLatch(1);
        PredestroyTestComponentTwo.startLatch= new CountDownLatch(1);
        PredestroyTestComponentOne.startLatch= new CountDownLatch(1);
        WorkbenchPredestroyPerspectiveTest.startPerspective();
        PerspectiveOnePredestroyPerspectiveTest.startLatch.await();
        PredestroyTestComponentFour.startLatch.await();
        PredestroyTestComponentThree .startLatch.await();
        PredestroyTestComponentTwo.startLatch.await();
        PredestroyTestComponentOne.startLatch.await();

        assertNotNull(workbench);
        perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for(Perspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertTrue(p.getContext().isActive());
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }
    }

    private static int getActiveAsyncCount(List<Perspective<EventHandler<Event>, Event, Object>> perspectives) {
        for(Perspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {

                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                for(SubComponent<EventHandler<Event>, Event, Object> c: components) {
                       if(c instanceof AStatelessCallbackComponent) {
                           List<SubComponent<EventHandler<Event>, Event, Object>> instances = AStatelessCallbackComponent.class.cast(c).getInstances();
                           return Long.valueOf(instances.stream().filter(i->i.isStarted()).count()).intValue();
                       }
                }
            }
        }

        return 0;
    }

    @Test
    public void test1PreDestroyAnnotationAfterUse() throws InterruptedException {
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);

    }

}
