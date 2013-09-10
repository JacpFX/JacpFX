package org.jacp.test.missconfig;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IPerspective;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;
import org.jacp.test.main.ApplicationLauncherMissingPerspectives;
import org.jacp.test.main.ApplicationLauncherMissingWorkbenchId;
import org.jacp.test.workbench.WorkbenchMissingPerspectives;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 09.09.13
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class MissconfigWorkbenchIdTest {



    @Test(expected = RuntimeException.class)
    public void noIDSetOnWorkbench() {
        ApplicationLauncherMissingWorkbenchId.main(new String[0]);
        Platform.setImplicitExit(true);
        Platform.exit();
    }

    private String[] getPerspectiveAnnotations() {
        org.jacp.api.annotations.workbench.Workbench annotations = WorkbenchMissingPerspectives.class.getAnnotation(org.jacp.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }
}
