package org.jacp.test.missconfig;

import javafx.application.Platform;
import javafx.scene.Node;
import junit.framework.TestCase;
import org.jacp.doublePerspective.test.main.ApplicationLauncherDuplicateComponentTest;
import org.jacpfx.rcp.handler.AErrorDialogHandler;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.09.13
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public class MissconfigDuplicateComponentIds {
    @Test
    public void failedToStartDuplicatePerspectives() throws Exception {
        try {
            ApplicationLauncherDuplicateComponentTest.latch = new CountDownLatch(1);
            ApplicationLauncherDuplicateComponentTest.exceptionhandler = new CustomErrorDialogHandler();
            ApplicationLauncherDuplicateComponentTest.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Pause briefly to give FX a chance to start
        ApplicationLauncherDuplicateComponentTest.latch.await(5000, TimeUnit.MILLISECONDS);


    }

    public class CustomErrorDialogHandler extends AErrorDialogHandler {
        public CountDownLatch latch = new CountDownLatch(1);
        @Override
        public Node createExceptionDialog(Throwable e) {
            System.out.println("ERROR "+e.getMessage());
            //
            TestCase.assertTrue(e.getMessage().contains("more than one component found for id"));
            ApplicationLauncherDuplicateComponentTest.latch.countDown();
            Platform.exit();
            return null;
        }
    }
}
