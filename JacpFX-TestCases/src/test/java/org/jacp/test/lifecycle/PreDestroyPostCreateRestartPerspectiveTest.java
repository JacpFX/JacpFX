package org.jacp.test.lifecycle;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.PredestroyTestComponentFour;
import org.jacp.test.components.PredestroyTestComponentOne;
import org.jacp.test.components.PredestroyTestComponentThree;
import org.jacp.test.components.PredestroyTestComponentTwo;
import org.jacp.test.perspectives.PerspectiveOnePredestroyPerspectiveTest;
import org.jacp.test.workbench.WorkbenchPredestroyPerspectiveTest;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 16.10.13
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public class PreDestroyPostCreateRestartPerspectiveTest extends TestFXJacpFXSpringLauncher {


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



    private  void fireMessagesAndCheck() throws InterruptedException {
        PredestroyTestComponentOne.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentTwo.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentThree.countdownlatch = new CountDownLatch(10000);
        PredestroyTestComponentFour.countdownlatch = new CountDownLatch(10000);


        PerspectiveOnePredestroyPerspectiveTest.fireBurst(10000);

        PredestroyTestComponentOne.countdownlatch.await();

        PredestroyTestComponentTwo.countdownlatch.await();

        PredestroyTestComponentThree.countdownlatch.await();

        PredestroyTestComponentFour.countdownlatch.await();
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for(Perspective<Node, EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertTrue(p.getContext().isActive());
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }

    }

    private  void stopComponentsAndCheck(boolean burst)throws InterruptedException {
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        PerspectiveOnePredestroyPerspectiveTest.latch = new CountDownLatch(1);
        PredestroyTestComponentOne.latch = new CountDownLatch(1);
        PredestroyTestComponentTwo.latch = new CountDownLatch(1);
        PredestroyTestComponentThree.latch = new CountDownLatch(1);
        int val = getActiveAsyncCount(perspectives);
        System.out.println("active async: "+val);
        PredestroyTestComponentFour.latch = new CountDownLatch(val);
        PerspectiveOnePredestroyPerspectiveTest.stop();
        PerspectiveOnePredestroyPerspectiveTest.latch.await();
        PredestroyTestComponentOne.latch.await();
        PredestroyTestComponentTwo.latch.await();
        PredestroyTestComponentThree.latch.await();
        PredestroyTestComponentFour.latch.await();


        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for(Perspective<Node, EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertFalse(p.getContext().isActive());
                Thread.sleep(100);
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertTrue(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }
    }

    private  void restartComponentsAndCheck() throws InterruptedException {
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        PerspectiveOnePredestroyPerspectiveTest.startLatch = new CountDownLatch(1);
        PredestroyTestComponentFour.startLatch= new CountDownLatch(1);
        PredestroyTestComponentThree .startLatch= new CountDownLatch(1);
        PredestroyTestComponentTwo.startLatch= new CountDownLatch(1);
        PredestroyTestComponentOne.startLatch= new CountDownLatch(1);
        WorkbenchPredestroyPerspectiveTest.startPerspective();
        PerspectiveOnePredestroyPerspectiveTest.startLatch.await();
        PredestroyTestComponentFour.startLatch.await();
        PredestroyTestComponentThree .startLatch.await();
        PredestroyTestComponentTwo.startLatch.await();
        PredestroyTestComponentOne.startLatch.await();

        assertNotNull(workbench);
        perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());

        for(Perspective<Node, EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {
                assertTrue(p.getContext().isActive());
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());
            }else {
                assertTrue(p.getContext().isActive());
            }
        }
    }

    private static int getActiveAsyncCount(List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives) {
        for(Perspective<Node, EventHandler<Event>, Event, Object> p:perspectives) {
            Injectable handler = p.getPerspective();
            if(handler.getClass().isAssignableFrom(PerspectiveOnePredestroyPerspectiveTest.class)) {

                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                for(SubComponent<EventHandler<Event>, Event, Object> c: components) {
                       if(c instanceof AStatelessCallbackComponent) {
                           List<SubComponent<EventHandler<Event>, Event, Object>> instances = AStatelessCallbackComponent.class.cast(c).getInstances();
                           return Long.valueOf(instances.stream().filter(i->i.isStarted()).count()).intValue();
                       }
                }
            }
        }

        return 0;
    }

    @Test
    public void test1PreDestroyAnnotationAfterUse() throws InterruptedException {
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);
        restartComponentsAndCheck();
        fireMessagesAndCheck();
        stopComponentsAndCheck(true);

    }

}
