package org.jacp.test.missconfig;

import javafx.application.Platform;
import org.jacp.test.AllTests;
import org.jacp.test.main.ApplicationLauncherMissingWorkbenchId;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 20:57
 * Tests if id attribute in workbench is missing
 */
public class MissconfigWorkbenchIdTest {


    @Test(expected = RuntimeException.class)
    public void noIDSetOnWorkbench() {

        try {
            ApplicationLauncherMissingWorkbenchId.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        Platform.setImplicitExit(true);
        Platform.exit();
        AllTests.resetApplication();
    }

    private String[] getPerspectiveAnnotations() {
        org.jacpfx.api.annotations.workbench.Workbench annotations = WorkbenchMissingPerspectives.class.getAnnotation(org.jacpfx.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }
}
