package org.jacp.test.toolbar.base;

import org.jacp.test.toolbar.perspectives.PerspectiveOneToolbarSwitchPerspectives;
import org.jacp.test.toolbar.perspectives.PerspectiveTwoToolbarSwitchPerspectives;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;

import java.util.concurrent.CountDownLatch;

/**
 * @author Patrick Symmangk (pete.jacp@gmail.com)
 */
public class HandleToolbarBase {


    public static CountDownLatch wait = new CountDownLatch(1);
    public static CountDownLatch latch = new CountDownLatch(AStatelessCallbackComponent.MAX_INCTANCE_COUNT);
    public static CountDownLatch countdownlatch = new CountDownLatch(1);
    public static CountDownLatch stopLatch = new CountDownLatch(1);
    public static CountDownLatch startLatch = new CountDownLatch(1);
    public static CountDownLatch switchLatch = new CountDownLatch(1);
    public static String currentId = PerspectiveOneToolbarSwitchPerspectives.ID;

    protected void switchCurrentId() {
        currentId = PerspectiveOneToolbarSwitchPerspectives.ID.equals(currentId) ? PerspectiveTwoToolbarSwitchPerspectives.ID : PerspectiveOneToolbarSwitchPerspectives.ID;

    }

}
