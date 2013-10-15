package org.jacp.test.missconfig;

import org.jacp.doublePerspective.test.main.ApplicationLauncherDuplicateComponentTest;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.09.13
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public class MissconfigDuplicateComponentIds {
    @Test(expected = RuntimeException.class)
    public void failedToStartDuplicatePerspectives() throws Exception {
        try {
            ApplicationLauncherDuplicateComponentTest.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Pause briefly to give FX a chance to start
        ApplicationLauncherDuplicateComponentTest.latch.await(5000, TimeUnit.MILLISECONDS);


    }
}
