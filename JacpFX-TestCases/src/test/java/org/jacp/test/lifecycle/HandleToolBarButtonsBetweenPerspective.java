package org.jacp.test.lifecycle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.apache.log4j.Logger;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherHandleToolBarButtonsBetweenPerspectives;
import org.jacp.test.toolbar.components.ComponentHandleToolBarBetweenPerspectives2;
import org.jacp.test.toolbar.perspectives.PerspectiveOneToolbarSwitchPerspectives;
import org.jacp.test.toolbar.perspectives.PerspectiveTwoToolbarSwitchPerspectives;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.GlobalMediator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.*;

public class HandleToolBarButtonsBetweenPerspective {

    static Thread t;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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

    private Perspective<Node, EventHandler<Event>, Event, Object> getPerspectiveById(List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives, String id) {
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {
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
        logger.info("...::: START TEST :::...");
        ApplicationLauncherHandleToolBarButtonsBetweenPerspectives launcher = ApplicationLauncherHandleToolBarButtonsBetweenPerspectives.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());

            if (PerspectiveOneToolbarSwitchPerspectives.ID.equals(p.getContext().getId())) {
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
                components.forEach(c -> assertTrue(c.getContext().isActive()));
            }

        }
        int waitingTime = 50;

        Perspective<Node, EventHandler<Event>, Event, Object> p = this.getPerspectiveById(perspectives, ComponentHandleToolBarBetweenPerspectives2.currentId);
        assertNotNull(p);
        // INITAL -> 6
        assertEquals(6, GlobalMediator.getInstance().countVisibleButtons());
        PerspectiveTwoToolbarSwitchPerspectives.switchLatch = new CountDownLatch(1);
        PerspectiveOneToolbarSwitchPerspectives.switchPerspective();
        PerspectiveTwoToolbarSwitchPerspectives.switchLatch.await();
        Thread.sleep(waitingTime);

        // SWITCH PERSPECTIVE --> 4
        assertEquals(4, GlobalMediator.getInstance().countVisibleButtons());
        PerspectiveOneToolbarSwitchPerspectives.switchLatch = new CountDownLatch(1);
        PerspectiveTwoToolbarSwitchPerspectives.switchPerspective();
        PerspectiveOneToolbarSwitchPerspectives.switchLatch.await();
        Thread.sleep(waitingTime);

        // BACK TO INITAL --> 6
        assertEquals(6, GlobalMediator.getInstance().countVisibleButtons());
        ComponentHandleToolBarBetweenPerspectives2.stopLatch = new CountDownLatch(1);
        ComponentHandleToolBarBetweenPerspectives2.startLatch = new CountDownLatch(1);
        ComponentHandleToolBarBetweenPerspectives2.switchTarget();
        ComponentHandleToolBarBetweenPerspectives2.stopLatch.await();
        ComponentHandleToolBarBetweenPerspectives2.startLatch.await();
        Thread.sleep(waitingTime);

        // MOVE COMPONENT --> 4
        assertEquals(4, GlobalMediator.getInstance().countVisibleButtons());
        PerspectiveTwoToolbarSwitchPerspectives.switchLatch = new CountDownLatch(1);
        PerspectiveOneToolbarSwitchPerspectives.switchPerspective();
        PerspectiveTwoToolbarSwitchPerspectives.switchLatch.await();
        Thread.sleep(waitingTime);

        // SWITCH PERSPECTIVE --> 6
        assertEquals(6, GlobalMediator.getInstance().countVisibleButtons());

        Perspective<Node, EventHandler<Event>, Event, Object> p1 = this.getPerspectiveById(perspectives, ComponentHandleToolBarBetweenPerspectives2.currentId);
        assertNotNull(p1);

        assertNotNull(getComponentById(p1.getSubcomponents(), ComponentHandleToolBarBetweenPerspectives2.ID));

    }


}
