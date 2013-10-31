package org.jacp.test.lifesycleannotations;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.components.PredestroyTestComponentFour;
import org.jacp.test.components.PredestroyTestComponentOne;
import org.jacp.test.components.PredestroyTestComponentThree;
import org.jacp.test.components.PredestroyTestComponentTwo;
import org.jacp.test.main.ApplicationLauncherPerspectiveMessaginTest;
import org.jacp.test.main.ApplicationPredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveOnePredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveTwoPredestroyPerspectiveTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

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
        PerspectiveOnePredestroyPerspectiveTest.stop();
        PerspectiveOnePredestroyPerspectiveTest.latch.await();
        PredestroyTestComponentOne.latch.await();
        PredestroyTestComponentTwo.latch.await();
        PredestroyTestComponentThree.latch.await();
        PredestroyTestComponentFour.latch.await();
    }

    @Test
    public void testPreDestroyAnnotationAfterUse() throws InterruptedException {
        PredestroyTestComponentOne.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentTwo.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentThree.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentFour.countdownlatch = new CountDownLatch(10000);


        PerspectiveOnePredestroyPerspectiveTest.fireBurst(10000);

        PredestroyTestComponentOne.countdownlatch.await();

        PredestroyTestComponentTwo.countdownlatch.await();

        PredestroyTestComponentThree.countdownlatch.await();

        PredestroyTestComponentFour.countdownlatch.await();

        PerspectiveOnePredestroyPerspectiveTest.stop();
        PerspectiveOnePredestroyPerspectiveTest.latch.await();
        PredestroyTestComponentOne.latch.await();
        PredestroyTestComponentTwo.latch.await();
        PredestroyTestComponentThree.latch.await();
        PredestroyTestComponentFour.latch.await();
    }

}
