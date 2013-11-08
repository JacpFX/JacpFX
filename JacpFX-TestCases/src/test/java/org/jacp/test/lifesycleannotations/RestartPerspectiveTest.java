package org.jacp.test.lifesycleannotations;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.Injectable;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;
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
 * User: ady
 * Date: 08.11.13
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class RestartPerspectiveTest {
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
    @Test
    public void test3DestroyAndRestart() throws InterruptedException {
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
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for(IPerspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertTrue(p.getContext().isActive());
                List<ISubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }


        PerspectiveOnePredestroyPerspectiveTest.stop();
        PerspectiveOnePredestroyPerspectiveTest.latch.await();
        PredestroyTestComponentOne.latch.await();
        PredestroyTestComponentTwo.latch.await();
        PredestroyTestComponentThree.latch.await();
        PredestroyTestComponentFour.latch.await();
        for(IPerspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertFalse(p.getContext().isActive());
                Thread.sleep(1000);
                List<ISubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertTrue(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }

        ApplicationPredestroyPerspectiveTest.latch = new CountDownLatch(5);
        WorkbenchPredestroyPerspectiveTest.startPerspective();
        try {
            ApplicationPredestroyPerspectiveTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(IPerspective<EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertTrue(p.getContext().isActive());
                List<ISubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }
    }

}
