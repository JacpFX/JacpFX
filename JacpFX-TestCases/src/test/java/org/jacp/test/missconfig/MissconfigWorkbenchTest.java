package org.jacp.test.missconfig;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherMissingPerspectives;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:57
 * Tests if no perspective are declared in workbench
 */
public class MissconfigWorkbenchTest {

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

                ApplicationLauncherMissingPerspectives.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherMissingPerspectives.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noPerspectivesAnnotatedTest() {

        assertNotNull(getPerspectiveAnnotations());
        assertTrue(getPerspectiveAnnotations().length == 0);

        ApplicationLauncherMissingPerspectives launcher = ApplicationLauncherMissingPerspectives.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertTrue(perspectives.isEmpty());

    }


    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = WorkbenchMissingPerspectives.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }
}
