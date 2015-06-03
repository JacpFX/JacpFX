package org.jacp.test;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Platform;
import org.jacp.test.missconfig.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 09.09.13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ MissconfigWorkbenchTest.class,
        MissconfigWorkbenchMissingIdInPerspective.class,
        MissconfigDuplicateComponentIds.class,
        MissconfigWorkbenchIdTest.class,
        MissconfigLauncherTest.class,
        MissconfigFXComponentTest.class,

        MissconfigDuplicatePerspectiveIds.class,
        MissconfigComponentTest.class})
public class NonUITests {
    // TODO do not use allTests due to problems with main thread.
    public static void resetApplication() {
        Platform.setImplicitExit(true);
        Platform.exit();
        Field launchCalledField = null;
        try {
            launchCalledField = LauncherImpl.class.getDeclaredField("launchCalled");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        launchCalledField.setAccessible(true);
        try {
            AtomicBoolean atomic = (AtomicBoolean) launchCalledField.get(null);
            atomic.set(false);
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
