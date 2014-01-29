package org.jacp.test.components;

import org.jacp.test.perspectives.PerspectiveIds;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;

import java.util.concurrent.CountDownLatch;

/**
 * @author: Patrick Symmangk (pete.jacp@gmail.com)
 */
public abstract class AHandleToolBarPerspective {

    public static CountDownLatch wait = new CountDownLatch(1);
    public static CountDownLatch latch = new CountDownLatch(AStatelessCallbackComponent.MAX_INCTANCE_COUNT);
    public static CountDownLatch countdownlatch = new CountDownLatch(1);
    public static CountDownLatch stopLatch = new CountDownLatch(1);
    public static CountDownLatch startLatch = new CountDownLatch(1);

    protected static final String PERSPECTIVE_ONE = PerspectiveIds.PerspectiveToolbarOne;
    protected static final String PERSPECTIVE_TWO = PerspectiveIds.PerspectiveToolbarTwo;
    protected static final String MESSAGE_SWITCH = "switch";
    protected static final String MESSAGE_MESSAGE = "message";

    public static String currentId = PERSPECTIVE_ONE;


}
