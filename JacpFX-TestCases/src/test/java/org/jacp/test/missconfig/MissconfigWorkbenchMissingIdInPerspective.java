package org.jacp.test.missconfig;

import javafx.application.Platform;
import org.jacp.test.NonUITests;
import org.jacp.test.main.ApplicationLauncherMissingIdInPerspective;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Tests the exception when a perspective set to workbench where the ID assignment is missing.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:57
 * Tests if id attribute in Perspective is Empty
 */
public class MissconfigWorkbenchMissingIdInPerspective {


    @Test(expected = RuntimeException.class)
    public void failedToStartPerspective() throws Exception {
        try {
            ApplicationLauncherMissingIdInPerspective.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Pause briefly to give FX a chance to start
        ApplicationLauncherMissingIdInPerspective.latch.await(5000, TimeUnit.MILLISECONDS);
        NonUITests.resetApplication();

    }

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }
}
