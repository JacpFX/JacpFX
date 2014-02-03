package org.jacpfx.spring.launcher;

import javafx.stage.Stage;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.exceptions.AnnotationNotFoundException;
import org.jacpfx.api.exceptions.AttributeNotFoundException;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.rcp.handler.ExceptionHandler;
import org.jacpfx.rcp.workbench.EmbeddedFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * JavaFX / Spring application launcher; This abstract class handles reference
 * to spring context and contains the JavaFX start method; Implement this
 * abstract class and add a main method to call the default JavaFX launch
 * ("Application.launch(args);") sequence
 *
 * @author Andy Moncsek
 */
public abstract class AFXSpringXmlLauncher extends ASpringLauncher {


    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws Exception {
        initExceptionHandler();
        scanPackegesAndInitRegestry();
        final Launcher<ClassPathXmlApplicationContext> launcher = new SpringXmlConfigLauncher(getXmlConfig());
        final Class<? extends FXWorkbench> workbenchHandler = getWorkbenchClass();
        if (workbenchHandler == null) throw new ComponentNotFoundException("no FXWorkbench class defined");
        initWorkbench(stage, launcher, workbenchHandler);
    }



    private void initWorkbench(final Stage stage, final Launcher<ClassPathXmlApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        if (workbenchHandler.isAnnotationPresent(Workbench.class)) {
            this.workbench = createWorkbench(launcher, workbenchHandler);
            workbench.init(launcher, stage);
            postInit(stage);
        } else {
            throw new AnnotationNotFoundException("no @Workbench annotation found on class");
        }
    }

    private EmbeddedFXWorkbench createWorkbench(final Launcher<ClassPathXmlApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        final Workbench annotation = workbenchHandler.getAnnotation(Workbench.class);
        final String id = annotation.id();
        if (id.isEmpty()) throw new AttributeNotFoundException("no workbench id found for: " + workbenchHandler);
        final FXWorkbench handler = launcher.registerAndGetBean(workbenchHandler, id, Scope.SINGLETON);
        return new EmbeddedFXWorkbench(handler);
    }

    public abstract String getXmlConfig();


}
