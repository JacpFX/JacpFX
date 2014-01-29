package org.jacp.test.context;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncher;
import org.jacp.test.main.ApplicationLauncherContextTest;
import org.jacp.test.perspectives.PerspectiveContextTest;
import org.jacp.test.workbench.WorkbenchContextTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:50
 * To change this template use File | Settings | File Templates.
 */
public class ContextObjectAccessTest {
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

                ApplicationLauncherContextTest.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try {
            ApplicationLauncherContextTest.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testContextInit() {
       assertNotNull(WorkbenchContextTest.context);
        assertNotNull(PerspectiveContextTest.context);
    }
}
