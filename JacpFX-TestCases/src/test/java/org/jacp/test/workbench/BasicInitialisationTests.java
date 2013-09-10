package org.jacp.test.workbench;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.Injectable;
import org.jacp.api.context.Context;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.util.ClassRegistry;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;
import org.jacp.javafx.rcp.workbench.FXWorkbench;
import org.jacp.test.main.ApplicationLauncher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 06.09.13
 * Time: 08:38
 * To change this template use File | Settings | File Templates.
 */
public class BasicInitialisationTests {
    static Thread t;
    @AfterClass
    public static void exitWorkBench() {
        Platform.exit();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @BeforeClass
    public static void initWorkbench() {


        t = new Thread("JavaFX Init Thread") {
            public void run() {

                ApplicationLauncher.main(new String[0]);

            }
        };
        t.setDaemon(true);
        t.start();
        // Pause briefly to give FX a chance to start
        try
        {
            ApplicationLauncher.latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void checkApplicationLauncher() {
        ApplicationLauncher launcher =  ApplicationLauncher.instance[0];
        assertNotNull(launcher);
    }

    @Test
    public void checkComponentScanning() {
        ApplicationLauncher launcher =  new ApplicationLauncher();
        assertNotNull(launcher);
        launcher.startComponentScaning();
        assertNotNull(ClassRegistry.getAllClasses());
        assertFalse(ClassRegistry.getAllClasses().isEmpty());
    }

    @Test
    public void checkWorkspace() {
        ApplicationLauncher launcher =  ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> handler = workbench.getComponentHandler();
        assertNotNull(handler);
    }

    @Test
    public void checkWorkspaceAnnotations() {
        ApplicationLauncher launcher =  ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        assertNotNull(getPerspectiveAnnotations());
        assertTrue(getPerspectiveAnnotations().length>0);
        FXWorkbench fxworkbench = workbench.getComponentHandle();
        assertNotNull(fxworkbench);

    }

    @Test
    public void checkWorkspaceContext() {
        ApplicationLauncher launcher =  ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        Context<EventHandler<Event>,Event,Object> context = workbench.getContext();
        assertNotNull(context);
        assertNotNull(context.getName());
        assertNotNull(context.getId());
       // assertNotNull(context.getResourceBundle());

    }

    private String[] getPerspectiveAnnotations() {
        org.jacp.api.annotations.workbench.Workbench annotations = Workbench.class.getAnnotation(org.jacp.api.annotations.workbench.Workbench.class);
        return annotations.perspectives();
    }
    @Test
    public void checkPerspectives() {
        ApplicationLauncher launcher =  ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length==perspectives.size());
        for(IPerspective<EventHandler<Event>, Event, Object> p :perspectives) {
            assertNotNull(p.getComponentHandler());
            assertNotNull(p.getContext());
            assertNotNull(p.getComponentsMessageQueue());
            assertNotNull(p.getMessageDelegateQueue());
            Context<EventHandler<Event>, Event, Object> context = p.getContext();
            assertNotNull(context.getParentId());
            assertNotNull(context.getId());
            assertNotNull(context.getName());
            assertNotNull(context.getResourceBundle());
        }
    }

    @Test
    public void checkComponents() {
        ApplicationLauncher launcher =  ApplicationLauncher.instance[0];
        assertNotNull(launcher);
        AFXWorkbench workbench = launcher.getWorkbench();
        assertNotNull(workbench);
        List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = workbench.getPerspectives();
        assertNotNull(perspectives);
        assertFalse(perspectives.isEmpty());
        assertTrue(getPerspectiveAnnotations().length==perspectives.size());
        for(IPerspective<EventHandler<Event>, Event, Object> p :perspectives) {
            Injectable handler = p.getPerspectiveHandle();
            Perspective annotation = handler.getClass().getAnnotation(Perspective.class);
            String[] components = annotation.components();
            if(components.length==0) {
                assertTrue(p.getSubcomponents().isEmpty());
            } else {
                assertNotNull(p.getSubcomponents());
                assertFalse(p.getSubcomponents().isEmpty());
                assertTrue(components.length==p.getSubcomponents().size());
                List<ISubComponent<EventHandler<Event>,Event,Object>> subcomponents = p.getSubcomponents();
                for(ISubComponent<EventHandler<Event>,Event,Object> c : subcomponents) {
                       assertNotNull(c.getParentId());
                       assertTrue(c.getParentId().equals(p.getContext().getId()));
                    Context<EventHandler<Event>, Event, Object> context = c.getContext();
                    assertNotNull(context.getParentId());
                    assertNotNull(context.getId());
                    assertNotNull(context.getName());
                    assertNotNull(context.getResourceBundle());
                }
            }

        }

    }

}
