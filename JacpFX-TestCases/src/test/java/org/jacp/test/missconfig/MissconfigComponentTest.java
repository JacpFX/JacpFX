package org.jacp.test.missconfig;

import javafx.application.Platform;
import javafx.scene.Node;
import junit.framework.TestCase;
import org.jacp.doublePerspective.test.main.ApplicationLauncherDuplicateComponentTest;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherMissingComponentIds;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.jacpfx.rcp.handler.AErrorDialogHandler;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
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
            ApplicationLauncherMissingComponentIds.latch = new CountDownLatch(1);
            ApplicationLauncherMissingComponentIds.exceptionhandler = new CustomErrorDialogHandler();

            ApplicationLauncherMissingComponentIds.main(new String[0]);
        } catch (Exception e) {
            System.out.println("EXCEPTION");
            e.printStackTrace();
            throw e;
        }

        // Pause briefly to give FX a chance to start
        ApplicationLauncherMissingComponentIds.latch.await(1000, TimeUnit.MILLISECONDS);


    }


    public class CustomErrorDialogHandler extends AErrorDialogHandler {
        public CountDownLatch latch = new CountDownLatch(1);
        @Override
        public Node createExceptionDialog(Throwable e) {
            System.out.println("ERROR "+e.getMessage());
            //
            TestCase.assertTrue(e.getMessage().contains("more than one component found for id"));
            ApplicationLauncherMissingComponentIds.latch.countDown();
            Platform.exit();
            return null;
        }
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
