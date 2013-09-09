package org.jacp.test.missconfig;

import org.jacp.api.exceptions.ComponentNotFoundException;
import org.jacp.javafx.rcp.util.ClassRegistry;
import org.jacp.test.main.ApplicationLauncher;
import org.jacp.test.main.ApplicationLauncherMissconfigWorkbench;
import org.jacp.test.main.ApplicationLauncherMissconfigWorkbench2;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:56
 * This testcase test if errors in ApplicationLauncher are handled correctly
 */
public class MissconfigLauncherTest {

    @Test(expected = RuntimeException.class)
    public void noWorkbenchClassTest() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ApplicationLauncherMissconfigWorkbench.main(new String[0]);
        }   finally{
            latch.countDown();
        }
        // Pause briefly to give FX a chance to start
        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Test(expected = RuntimeException.class)
    public void noWorkbenchAnnotationClassTest() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ApplicationLauncherMissconfigWorkbench2.main(new String[0]);
        }   finally{
            latch.countDown();
        }
        // Pause briefly to give FX a chance to start
        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Test(expected = InvalidParameterException.class)
    public void noPackagesDefined() {
        ApplicationLauncherMissconfigWorkbench2 launcher = new ApplicationLauncherMissconfigWorkbench2();
        launcher.startComponentScaning();
        assertNotNull(ClassRegistry.getAllClasses());
        assertTrue(ClassRegistry.getAllClasses().isEmpty());
    }
}
