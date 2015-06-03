package org.jacp.test.workbench;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.test.NonUITests;
import org.jacp.test.main.ApplicationLauncher;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.registry.ClassRegistry;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 06.09.13
 * Time: 08:38
 * To change this template use File | Settings | File Templates.
 */
public class BasicInitialisationTests {
    static Thread t;

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }

    @BeforeClass
    public static void initWorkbench() {


        t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationLauncher.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncher.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkApplicationLauncher() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        assertNotNull(launcher);
    }

    @Test
    public void checkComponentScanning() {
        ApplicationLauncher launcher = new ApplicationLauncher();
        assertNotNull(launcher);
        launcher.startComponentScaning();
        assertNotNull(ClassRegistry.getAllClasses());
        assertFalse(ClassRegistry.getAllClasses().isEmpty());
    }

    @Test
    public void checkWorkspace() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> handler = workbench.getComponentHandler();
        assertNotNull(handler);
    }

    @Test
    public void checkWorkspaceAnnotations() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        assertNotNull(getPerspectiveAnnotations());
        assertTrue(getPerspectiveAnnotations().length > 0);
        FXWorkbench fxworkbench = workbench.getComponentHandle();
        assertNotNull(fxworkbench);

    }

    @Test
    public void checkWorkspaceContext() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        JacpContext<EventHandler<Event>, Object> context = workbench.getContext();
        assertNotNull(context);
        assertNotNull(context.getName());
        assertNotNull(context.getId());
        // assertNotNull(context.getResourceBundle());

    }

    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = Workbench.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }

    @Test
    public void checkPerspectives() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length == perspectives.size());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {
            assertNotNull(p.getComponentHandler());
            assertNotNull(p.getContext());
            assertNotNull(p.getMessageQueue());
            assertNotNull(p.getMessageDelegateQueue());
            JacpContext< EventHandler<Event>,Object> context = p.getContext();
            assertNotNull(context.getParentId());
            assertNotNull(context.getId());
            assertNotNull(context.getName());
            assertNotNull(context.getResourceBundle());
        }
    }

    @Test
    public void checkComponents() {
        ApplicationLauncher launcher = ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length == perspectives.size());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {
            Injectable handler = p.getPerspective();
            org.jacpfx.api.annotations.perspective.Perspective annotation = handler.getClass().getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
            String[] components = annotation.components();
            if (components.length == 0) {
                assertTrue(p.getSubcomponents().isEmpty());
            } else {
                assertNotNull(p.getSubcomponents());
                assertFalse(p.getSubcomponents().isEmpty());
                assertTrue(components.length == p.getSubcomponents().size());
                List<SubComponent<EventHandler<Event>, Event, Object>> subcomponents = p.getSubcomponents();
                for (SubComponent<EventHandler<Event>, Event, Object> c : subcomponents) {
                    assertNotNull(c.getContext().getParentId());
                    assertTrue(c.getContext().getParentId().equals(p.getContext().getId()));
                    JacpContext<EventHandler<Event>,Object> context = c.getContext();
                    assertNotNull(context.getParentId());
                    assertNotNull(context.getId());
                    assertNotNull(context.getName());
                    assertNotNull(context.getResourceBundle());
                }
            }

        }

    }

}
