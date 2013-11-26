package org.jacp.test.missconfig;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherMissingComponentIds;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:58
 * Test if declared component ids in perspective are incorrect
 */
public class MissconfigComponentTest {
    @Test(expected = RuntimeException.class)
    public void failedToStartComponents() throws Exception {
        try {
            ApplicationLauncherMissingComponentIds.main(new String[0]);
        } catch (Exception e) {
            System.out.println("EXCEPTION");
            e.printStackTrace();
            throw e;
        }

        // Pause briefly to give FX a chance to start
        ApplicationLauncherMissingComponentIds.latch.await(1000, TimeUnit.MILLISECONDS);


    }


    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = WorkbenchMissingPerspectives.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        AllTests.resetApplication();
    }
}
