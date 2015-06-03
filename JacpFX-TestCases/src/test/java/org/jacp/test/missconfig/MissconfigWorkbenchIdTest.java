package org.jacp.test.missconfig;

import javafx.application.Platform;
import org.jacp.test.NonUITests;
import org.jacp.test.main.ApplicationLauncherMissingWorkbenchId;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * This test checks the exception when no workbench id is set
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:57
 * Tests if id attribute in workbench is missing
 */
public class MissconfigWorkbenchIdTest {


    @Test(expected = RuntimeException.class)
    public void noIDSetOnWorkbench() {

        try {
            ApplicationLauncherMissingWorkbenchId.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        NonUITests.resetApplication();
    }

    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }
}
