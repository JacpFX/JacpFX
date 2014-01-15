package org.jacp.test.lifesycle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.test.AllTests;
import org.jacp.test.components.ComponentMoveComponentsBetweenPerspectives1;
import org.jacp.test.components.ComponentMoveComponentsBetweenPerspectives2;
import org.jacp.test.main.ApplicationLauncherMoveComponentsBetweenComponents;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
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
 * Date: 14.11.13
 * Time: 20:59
 * All Tests related to moving components between perspectives
 */
public class MoveComponentBetweenPerspective {

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

                ApplicationLauncherMoveComponentsBetweenComponents.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherMoveComponentsBetweenComponents.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Perspective<EventHandler<Event>, Event, Object> getPerspectiveById(List<Perspective<EventHandler<Event>, Event, Object>> perspectives, String id) {
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            if (p.getContext().getId().equals(id)) {
                return p;
            }

        }

        return null;
    }

    private SubComponent<EventHandler<Event>, Event, Object> getComponentById(List<SubComponent<EventHandler<Event>, Event, Object>> components, String id) {

        for (SubComponent<EventHandler<Event>, Event, Object> c : components) {
            if (c.getContext().getId().equals(id)) return c;
        }

        return null;
    }

    @Test
    public void testMoveFXComponent() throws InterruptedException {
        ApplicationLauncherMoveComponentsBetweenComponents launcher = ApplicationLauncherMoveComponentsBetweenComponents.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            if (p.getContext().getId().equals("id20")) {
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());

                components.forEach(c -> assertTrue(c.getContext().isActive()));
            }

        }

        int i = 0;
        while (i < 1000) {
            Perspective<EventHandler<Event>, Event, Object> p = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(p);
            assertNotNull(getComponentById(p.getSubcomponents(), "id0024"));
            ComponentMoveComponentsBetweenPerspectives2.stopLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives2.startLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives2.switchTarget();
            ComponentMoveComponentsBetweenPerspectives2.stopLatch.await();
            ComponentMoveComponentsBetweenPerspectives2.startLatch.await();
            Perspective<EventHandler<Event>, Event, Object> p1 = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(p1);
            ComponentMoveComponentsBetweenPerspectives2.showPerspective(ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(getComponentById(p1.getSubcomponents(), "id0024"));
            Thread.sleep(10);
            i++;
        }

    }

    @Test
    @Ignore
    public void testMoveCallbackComponent() throws InterruptedException {
        ApplicationLauncherMoveComponentsBetweenComponents launcher = ApplicationLauncherMoveComponentsBetweenComponents.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        ApplicationLauncherMoveComponentsBetweenComponents.latch= new CountDownLatch(1);
        try {
            ApplicationLauncherMoveComponentsBetweenComponents.handler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                   // TestCase.assertTrue(e.getMessage().contains("no targetLayout for layoutID:"));
                    ApplicationLauncherMoveComponentsBetweenComponents.latch.countDown();
                   e.printStackTrace();
                }
            };
            Thread.currentThread().setUncaughtExceptionHandler(ApplicationLauncherMoveComponentsBetweenComponents.handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        ApplicationLauncherMoveComponentsBetweenComponents.updateExceptionHandler();

        Perspective<EventHandler<Event>, Event, Object> p = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives1.currentId);
        assertNotNull(p);
        assertNotNull(getComponentById(p.getSubcomponents(), "id0023"));
        ComponentMoveComponentsBetweenPerspectives1.switchTarget();
        ApplicationLauncherMoveComponentsBetweenComponents.latch.await();


    }
}
