package org.jacp.test.lifesycleannotations;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherPerspectiveMessaginTest;
import org.jacp.test.main.ApplicationPredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveTwoPredestroyPerspectiveTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 16.10.13
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public class PreDestroyPerspectiveTest {
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

                ApplicationPredestroyPerspectiveTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationPredestroyPerspectiveTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testPreDestroyAnnotation() throws InterruptedException {
        PerspectiveTwoPredestroyPerspectiveTest.stop();
        PerspectiveTwoPredestroyPerspectiveTest.latch.await();
    }

}
