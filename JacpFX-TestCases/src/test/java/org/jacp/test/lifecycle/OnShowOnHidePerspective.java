package org.jacp.test.lifecycle;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.perspectives.PerspectiveOnePredestroyPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveTwoPredestroyPerspectiveTest;
import org.jacp.test.workbench.WorkbenchPredestroyPerspectiveTest;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 12.11.13
 * Time: 09:14
 * Test lifecycle annotation @OnShow @OnHide in perspective
 * */
public class OnShowOnHidePerspective extends TestFXJacpFXSpringLauncher {


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
        return WorkbenchPredestroyPerspectiveTest.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }

    @Test
    public void testOnShow() throws InterruptedException {

        PerspectiveOnePredestroyPerspectiveTest.showLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.showLatch = new CountDownLatch(1);

        PerspectiveOnePredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        PerspectiveOnePredestroyPerspectiveTest.startLatch.await();
        PerspectiveTwoPredestroyPerspectiveTest.startLatch.await();
        WorkbenchPredestroyPerspectiveTest.showPerspective1();
        System.out.println("show Perspective 1---------------------------");
        //PerspectiveOnePredestroyPerspectiveTest.showLatch.await();
        assertTrue(true);
        System.out.println("show Perspective 1---------------------------XXXXXX");
        WorkbenchPredestroyPerspectiveTest.showPerspective2();
        System.out.println("show Perspective 2-----------------------------");
        PerspectiveTwoPredestroyPerspectiveTest.showLatch.await();
        System.out.println("show Perspective 2-----------------------------XXXXXX");
        assertTrue(true);

        for(int i =0; i<100;i++) {
            OnShowHide();
        }


    }

    private void OnShowHide() throws InterruptedException {
        PerspectiveOnePredestroyPerspectiveTest.showLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.showLatch = new CountDownLatch(1);

        PerspectiveOnePredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        PerspectiveTwoPredestroyPerspectiveTest.hideLatch = new CountDownLatch(1);
        WorkbenchPredestroyPerspectiveTest.showPerspective1();
        System.out.println("show Perspective 1.1");
        PerspectiveOnePredestroyPerspectiveTest.showLatch.await();
        PerspectiveTwoPredestroyPerspectiveTest.hideLatch.await();
        assertTrue(true);

        WorkbenchPredestroyPerspectiveTest.showPerspective2();
        System.out.println("show Perspective 2.1");
        PerspectiveTwoPredestroyPerspectiveTest.showLatch.await();
        PerspectiveOnePredestroyPerspectiveTest.hideLatch.await();
        assertTrue(true);
    }

    @Test
    public void testOnHide() {

    }


}
