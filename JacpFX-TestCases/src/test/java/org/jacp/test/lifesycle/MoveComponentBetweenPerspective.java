package org.jacp.test.lifesycle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import junit.framework.Assert;
import org.jacp.test.AllTests;
import org.jacp.test.components.ComponentMoveComponentsBetweenPerspectives1;
import org.jacp.test.components.ComponentMoveComponentsBetweenPerspectives2;
import org.jacp.test.main.ApplicationLauncherMoveComponentsBetweenComponents;
import org.jacp.test.main.ApplicationShutdownAndRestartComponentsTest;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
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

    private IPerspective<EventHandler<Event>, Event, Object> getPerspectiveById(List<IPerspective<EventHandler<Event>, Event, Object>> perspectives, String id) {
        for (IPerspective<EventHandler<Event>, Event, Object> p : perspectives) {

            if(p.getContext().getId().equals(id)) {
                return p;
            }

        }

        return null;
    }

    private ISubComponent<EventHandler<Event>, Event, Object> getComponentById(List<ISubComponent<EventHandler<Event>, Event, Object>> components, String id) {

        for(ISubComponent<EventHandler<Event>, Event, Object> c: components) {
             if(c.getContext().getId().equals(id)) return c;
        }

        return null;
    }

    @Test
    public void testMoveFXComponent() throws InterruptedException {
        ApplicationLauncherMoveComponentsBetweenComponents launcher = ApplicationLauncherMoveComponentsBetweenComponents.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (IPerspective<EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            if(p.getContext().getId().equals("id20")) {
                List<ISubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());

                components.forEach(c -> assertTrue(c.getContext().isActive()));
            }

        }

        int i=0;
        while (i<10000){
            IPerspective<EventHandler<Event>,Event,Object> p = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(p);
            assertNotNull(getComponentById(p.getSubcomponents(), "id0024"));
            ComponentMoveComponentsBetweenPerspectives2.stopLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives2.startLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives2.switchTarget();
            ComponentMoveComponentsBetweenPerspectives2.stopLatch.await();
            ComponentMoveComponentsBetweenPerspectives2.startLatch.await();
            IPerspective<EventHandler<Event>,Event,Object> p1 = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(p1);
            assertNotNull(getComponentById(p1.getSubcomponents(),"id0024"));
            i++;
        }

    }



    @Test
    public void testMoveCallbackComponent() throws InterruptedException {
        ApplicationLauncherMoveComponentsBetweenComponents launcher = ApplicationLauncherMoveComponentsBetweenComponents.instance[0];
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());


        int i=0;
        while (i<10000){
            IPerspective<EventHandler<Event>,Event,Object> p = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives1.currentId);
            assertNotNull(p);
            assertNotNull(getComponentById(p.getSubcomponents(),"id0023"));
            ComponentMoveComponentsBetweenPerspectives1.stopLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives1.startLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives1.switchTarget();
            ComponentMoveComponentsBetweenPerspectives1.stopLatch.await();
            ComponentMoveComponentsBetweenPerspectives1.startLatch.await();
            IPerspective<EventHandler<Event>,Event,Object> p1 = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives1.currentId);
            assertNotNull(p1);
            assertNotNull(getComponentById(p1.getSubcomponents(),"id0023"));
            i++;
        }

    }
}
