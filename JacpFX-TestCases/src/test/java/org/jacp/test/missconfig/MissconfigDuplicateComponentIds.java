package org.jacp.test.missconfig;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;
import junit.framework.TestCase;
import org.jacp.doublePerspective.test.main.ApplicationLauncherDuplicateComponentTest;
import org.jacp.doublePerspective.test.workbench.WorkbenchDuplicateComponentsTest;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.NonUITests;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.rcp.handler.AErrorDialogHandler;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.09.13
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public class MissconfigDuplicateComponentIds extends TestFXJacpFXSpringLauncher {


    @Override
    public String getXmlConfig() {
        return "main.xml";
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchDuplicateComponentsTest.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.doublePerspective.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }

    @Test
    public void failedToStartDuplicatePerspectives() throws Exception {

    }

    /**
     * Returns an ErrorDialog handler to display exceptions and errors in workspace. Overwrite this method if you need a customized handler.
     *
     * @return
     */
    @Override
    protected ErrorDialogHandler<Node> getErrorHandler() {
        return new CustomErrorDialogHandler();
    }

    public class CustomErrorDialogHandler extends AErrorDialogHandler {
        public CountDownLatch latch = new CountDownLatch(1);
        @Override
        public Node createExceptionDialog(Throwable e) {
            System.out.println("ERROR "+e.getMessage());
            //
            TestCase.assertTrue(e.getMessage().contains("more than one component found for id"));
            ApplicationLauncherDuplicateComponentTest.latch.countDown();
            return null;
        }
    }
    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }
}
