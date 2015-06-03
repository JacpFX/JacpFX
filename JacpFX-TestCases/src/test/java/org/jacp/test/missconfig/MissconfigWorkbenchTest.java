package org.jacp.test.missconfig;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.NonUITests;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:57
 * Tests if no perspective are declared in workbench
 */
public class MissconfigWorkbenchTest extends TestFXJacpFXSpringLauncher {


    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return  WorkbenchMissingPerspectives.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    protected void postInit(Stage stage) {
        System.out.println("post init stage");
    }

    @Override
    public String getXmlConfig() {
        return  "main.xml";
    }



    @Test
    public void noPerspectivesAnnotatedTest() {

        assertNotNull(getPerspectiveAnnotations());
        assertTrue(getPerspectiveAnnotations().length == 0);

        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertTrue(perspectives.isEmpty());

    }


    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = WorkbenchMissingPerspectives.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }
    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        NonUITests.resetApplication();


    }

}
