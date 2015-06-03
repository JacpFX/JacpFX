package org.jacp.test.missconfig;

import javafx.application.Platform;
import org.jacp.doublePerspective.test.main.ApplicationLauncher;
import org.jacp.test.NonUITests;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.09.13
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class MissconfigDuplicatePerspectiveIds {

    @Test(expected = RuntimeException.class)
    public void failedToStartDuplicatePerspectives() throws Exception {
        try {
            ApplicationLauncher.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Pause briefly to give FX a chance to start
        ApplicationLauncher.latch.await(5000, TimeUnit.MILLISECONDS);

        NonUITests.resetApplication();
    }
    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }
}
