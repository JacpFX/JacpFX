package org.jacp.test.missconfig;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.Injectable;
import org.jacp.api.context.Context;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherMissconfigComponents;
import org.jacp.test.workbench.WorkbenchMissingComponents;
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
 * Date: 09.09.13
 * Time: 20:58
 * Several test scenarios if perspective is missconfigured
 */
public class MissconfigPerspectiveTest {
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

                ApplicationLauncherMissconfigComponents.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherMissconfigComponents.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkApplicationLauncher() {
        ApplicationLauncherMissconfigComponents launcher = ApplicationLauncherMissconfigComponents.instance[0];
        assertNotNull(launcher);
    }

    private String[] getPerspectiveAnnotations() {
        org.jacp.api.annotations.workbench.Workbench annotations = WorkbenchMissingComponents.class.getAnnotation(org.jacp.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }

    @Test
    public void checkPerspectives() {
        ApplicationLauncherMissconfigComponents launcher = ApplicationLauncherMissconfigComponents.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length == perspectives.size());
        for (IPerspective<EventHandler<Event>, Event, Object> p : perspectives) {
            assertNotNull(p.getComponentHandler());
            assertNotNull(p.getContext());
            assertNotNull(p.getComponentsMessageQueue());
            assertNotNull(p.getMessageDelegateQueue());
            Context<EventHandler<Event>, Event, Object> context = p.getContext();
            assertNotNull(context.getParentId());
            assertNotNull(context.getId());
            assertNotNull(context.getName());
            assertNotNull(context.getResourceBundle());
        }
    }

    @Test
    public void checkComponents() {
        ApplicationLauncherMissconfigComponents launcher = ApplicationLauncherMissconfigComponents.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length == perspectives.size());
        for (IPerspective<EventHandler<Event>, Event, Object> p : perspectives) {
            assertNotNull(p.getComponentHandler());
            assertNotNull(p.getContext());
            assertNotNull(p.getComponentsMessageQueue());
            assertNotNull(p.getMessageDelegateQueue());
            Context<EventHandler<Event>, Event, Object> context = p.getContext();
            assertNotNull(context.getParentId());
            assertNotNull(context.getId());
            assertNotNull(context.getName());
            assertNotNull(context.getResourceBundle());
            Injectable handler = p.getPerspective();
            Perspective annotation = handler.getClass().getAnnotation(Perspective.class);
            String[] components = annotation.components();
            assertTrue(components.length == 0);

            assertTrue(p.getSubcomponents().isEmpty());
        }


    }
}
