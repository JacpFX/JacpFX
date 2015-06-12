package org.jacp.test.lifecycle;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.components.ComponentMoveComponentsBetweenPerspectives2;
import org.jacp.test.workbench.WorkbenchMoveComponentsBetweenPerspectives;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 14.11.13
 * Time: 20:59
 * All Tests related to moving component between perspective
 */
public class MoveComponentBetweenPerspective extends TestFXJacpFXSpringLauncher {


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
        return WorkbenchMoveComponentsBetweenPerspectives.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }

    private Perspective<Node, EventHandler<Event>, Event, Object> getPerspectiveById(List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives, String id) {
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            if (p.getContext().getId().equals(id)) {
                return p;
            }

        }

        return null;
    }

    private SubComponent<EventHandler<Event>, Event, Object> getComponentById(List<SubComponent<EventHandler<Event>, Event, Object>> components, String id) {

        for (SubComponent<EventHandler<Event>, Event, Object> c : components) {
            if (c.getContext().getId().equals(id)) return c;
        }

        return null;
    }

    @Test
    public void testMoveFXComponent() throws InterruptedException {
        AFXWorkbench workbench = getWorkbench();
        assertNotNull(workbench);
        List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        for (Perspective<Node, EventHandler<Event>, Event, Object> p : perspectives) {

            assertTrue(p.getContext().isActive());
            if (p.getContext().getId().equals("id20")) {
                List<SubComponent<EventHandler<Event>, Event, Object>> components = p.getSubcomponents();
                assertFalse(components.isEmpty());

                components.forEach(c -> {assertTrue(c.getContext().isActive());
                    System.out.println(c);});
            }

        }
        ComponentMoveComponentsBetweenPerspectives2.startLatch.await();
        int i = 0;
        while (i < 1000) {
            Perspective<Node, EventHandler<Event>, Event, Object> p = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(p);
            assertNotNull(getComponentById(p.getSubcomponents(), "id0024"));
            SubComponent<EventHandler<Event>, Event, Object> c = getComponentById(p.getSubcomponents(), "id0024");
            ComponentMoveComponentsBetweenPerspectives2 comp = c.getComponent();
            ComponentMoveComponentsBetweenPerspectives2.stopLatch = new CountDownLatch(1);
            ComponentMoveComponentsBetweenPerspectives2.startLatch = new CountDownLatch(1);
            comp.switchTarget();
            ComponentMoveComponentsBetweenPerspectives2.stopLatch.await();
            ComponentMoveComponentsBetweenPerspectives2.startLatch.await();
            Thread.sleep(10);
            ComponentMoveComponentsBetweenPerspectives2.showPerspective(ComponentMoveComponentsBetweenPerspectives2.currentId);
            Thread.sleep(10);
            Perspective<Node, EventHandler<Event>, Event, Object> p1 = getPerspectiveById(perspectives, ComponentMoveComponentsBetweenPerspectives2.currentId);
            assertNotNull(p1);
            assertNotNull(getComponentById(p1.getSubcomponents(), "id0024"));
            Thread.sleep(10);
            i++;
        }

    }


}
