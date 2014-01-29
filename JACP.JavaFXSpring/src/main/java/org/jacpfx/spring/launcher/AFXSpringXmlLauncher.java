package org.jacpfx.spring.launcher;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.exceptions.AnnotationNotFoundException;
import org.jacpfx.api.exceptions.AttributeNotFoundException;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.rcp.handler.DefaultErrorDialogHandler;
import org.jacpfx.rcp.handler.ExceptionHandler;
import org.jacpfx.rcp.registry.ClassRegistry;
import org.jacpfx.rcp.util.ClassFinder;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.EmbeddedFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * JavaFX / Spring application launcher; This abstract class handles reference
 * to spring context and contains the JavaFX start method; Implement this
 * abstract class and add a main method to call the default JavaFX launch
 * ("Application.launch(args);") sequence
 *
 * @author Andy Moncsek
 */
public abstract class AFXSpringXmlLauncher extends Application {
    private AFXWorkbench workbench;


    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws Exception {
        ExceptionHandler.initExceptionHandler(getErrorHandler());
        scanPackegesAndInitRegestry();
        final Launcher<ClassPathXmlApplicationContext> launcher = new SpringXmlConfigLauncher(getXmlConfig());
        final Class<? extends FXWorkbench> workbenchHandler = getWorkbechClass();
        if (workbenchHandler == null) throw new ComponentNotFoundException("no FXWorkbench class defined");
        initWorkbench(stage, launcher, workbenchHandler);

        Thread.currentThread().setUncaughtExceptionHandler(ExceptionHandler.getInstance());

    }

    private void initWorkbench(final Stage stage, final Launcher<ClassPathXmlApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        if (workbenchHandler.isAnnotationPresent(Workbench.class)) {
            this.workbench = createWorkbench(launcher,workbenchHandler);
            workbench.init(launcher, stage);
            postInit(stage);
        } else {
            throw new AnnotationNotFoundException("no @Workbench annotation found on class");
        }
    }

    private EmbeddedFXWorkbench createWorkbench(final Launcher<ClassPathXmlApplicationContext> launcher,final Class<? extends FXWorkbench> workbenchHandler) {
        final Workbench annotation = workbenchHandler.getAnnotation(Workbench.class);
        final String id = annotation.id();
        if (id.isEmpty()) throw new AttributeNotFoundException("no workbench id found for: " + workbenchHandler);
        final FXWorkbench handler = launcher.registerAndGetBean(workbenchHandler, id, Scope.SINGLETON);
        return new EmbeddedFXWorkbench(handler);
    }

    public AFXWorkbench getWorkbench() {
        return this.workbench;
    }

    public abstract String getXmlConfig();

    protected abstract Class<? extends FXWorkbench> getWorkbechClass();

    protected void scanPackegesAndInitRegestry() {
        final String[] packages = getBasePackages();
        if (packages == null)
            throw new InvalidParameterException("no packes declared, declare all packages containing perspectives and components");
        final ClassFinder finder = new ClassFinder();
        Stream.of(packages).parallel().forEach(p -> {
            try {
                ClassRegistry.addClasses(Arrays.asList(finder.getAll(p)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Return all packages which contains components and perspectives that should be scanned. This is needed to find components/prespectives by id.
     *
     * @return
     */
    protected abstract String[] getBasePackages();

    /**
     * Will be executed after Spring/JavaFX initialisation.
     *
     * @param stage
     */
    protected abstract void postInit(Stage stage);

    /**
     * Returns an ErrorDialog handler to display exceptions and errors in workspace. Overwrite this method if you need a customized handler.
     *
     * @return
     */
    protected ErrorDialogHandler<Node> getErrorHandler() {
        return new DefaultErrorDialogHandler();
    }

}
