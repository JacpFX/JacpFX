package org.jacp.test.missconfig;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IPerspective;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;
import org.jacp.test.main.ApplicationLauncher;
import org.jacp.test.main.ApplicationLauncherMissconfigWorkbench;
import org.jacp.test.main.ApplicationLauncherMissingPerspectives;
import org.jacp.test.workbench.Workbench;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 09.09.13
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class MissconfigWorkbenchTest {

    @Test
    public void noPerspectivesAnnotatedTest() {

        Thread t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationLauncherMissingPerspectives.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try
        {
            ApplicationLauncherMissingPerspectives.latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        assertNotNull(getPerspectiveAnnotations());
        assertTrue(getPerspectiveAnnotations().length==0);

        ApplicationLauncherMissingPerspectives launcher =  ApplicationLauncherMissingPerspectives.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertTrue(perspectives.isEmpty());
    }

    private String[] getPerspectiveAnnotations() {
        org.jacp.api.annotations.workbench.Workbench annotations = WorkbenchMissingPerspectives.class.getAnnotation(org.jacp.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }
}
