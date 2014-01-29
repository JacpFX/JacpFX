package org.jacp.test.lifecycle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.test.AllTests;
import org.jacp.test.components.ComponentHandleToolBarBetweenPerspectives2;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.main.ApplicationLauncherHandleToolBarButtonsBetweenPerspectives;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.*;

public class HandleToolBarButtonsBetweenPerspective {

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
                ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.main(new String[0]);
            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.latch.await();
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
    public void testMoveToolBar() throws InterruptedException {
        ApplicationLauncherHandleToolBarButtonsBetweenPerspectives launcher = ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            if (p.getContext().getId().equals(PerspectiveIds.PerspectiveToolbarOne)) {
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());

                components.forEach(c -> assertTrue(c.getContext().isActive()));
            }

        }

        int i = 0;
        while (i < 1000) {
            Perspective<EventHandler<Event>, Event, Object> p = this.getPerspectiveById(perspectives, ComponentHandleToolBarBetweenPerspectives2.currentId);
            assertNotNull(p);
            ComponentHandleToolBarBetweenPerspectives2.stopLatch = new CountDownLatch(1);
            ComponentHandleToolBarBetweenPerspectives2.startLatch = new CountDownLatch(1);
            ComponentHandleToolBarBetweenPerspectives2.switchTarget();
            ComponentHandleToolBarBetweenPerspectives2.stopLatch.await();
            ComponentHandleToolBarBetweenPerspectives2.startLatch.await();
            Perspective<EventHandler<Event>, Event, Object> p1 = this.getPerspectiveById(perspectives, ComponentHandleToolBarBetweenPerspectives2.currentId);
            assertNotNull(p1);
            assertNotNull(getComponentById(p1.getSubcomponents(), ComponentIds.ComponentHandleToolBarBetweenPerspectives2));
            i++;
        }

    }

}
