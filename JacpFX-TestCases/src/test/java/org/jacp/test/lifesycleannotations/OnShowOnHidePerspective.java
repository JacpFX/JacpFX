package org.jacp.test.lifesycleannotations;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationPredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveOnePredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveTwoPredestroyPerspectiveTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jacp.test.workbench.WorkbenchPredestroyPerspectiveTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 12.11.13
 * Time: 09:14
 * Test lifecycle annotation @OnShow @OnHide in perspective
 * */
public class OnShowOnHidePerspective {

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
    public void testOnShow() throws InterruptedException {

        PerspectiveOnePredestroyPerspectiveTest.showLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.showLatch = new CountDownLatch(1);

        PerspectiveOnePredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        WorkbenchPredestroyPerspectiveTest.showPerspective1();
        PerspectiveOnePredestroyPerspectiveTest.showLatch.await();
        assertTrue(true);
        WorkbenchPredestroyPerspectiveTest.showPerspective2();
        PerspectiveTwoPredestroyPerspectiveTest.showLatch.await();
        assertTrue(true);


        PerspectiveOnePredestroyPerspectiveTest.showLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.showLatch = new CountDownLatch(1);

        PerspectiveOnePredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        WorkbenchPredestroyPerspectiveTest.showPerspective1();
        PerspectiveOnePredestroyPerspectiveTest.showLatch.await();
        PerspectiveTwoPredestroyPerspectiveTest.hideLatch.await();
        assertTrue(true);

        WorkbenchPredestroyPerspectiveTest.showPerspective2();
        PerspectiveTwoPredestroyPerspectiveTest.showLatch.await();
        PerspectiveOnePredestroyPerspectiveTest.hideLatch.await();
        assertTrue(true);

    }

    @Test
    public void testOnHide() {

    }


}
