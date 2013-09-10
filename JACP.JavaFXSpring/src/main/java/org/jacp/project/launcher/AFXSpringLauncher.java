package org.jacp.project.launcher;

import org.jacp.api.annotations.workbench.Workbench;
import org.jacp.api.dialog.Scope;
import org.jacp.api.exceptions.AnnotationNotFoundException;
import org.jacp.api.exceptions.AttributeNotFoundException;
import org.jacp.api.exceptions.ComponentNotFoundException;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.workbench.IWorkbench;
import org.jacp.javafx.rcp.util.ClassFinder;
import org.jacp.javafx.rcp.util.ClassRegistry;
import org.jacp.javafx.rcp.util.ComponentRegistry;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;
import org.jacp.javafx.rcp.workbench.EmbeddedFXWorkbench;
import org.jacp.javafx.rcp.workbench.FXWorkbench;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

/**
 * JavaFX2 / Spring application launcher; This abstract class handles reference
 * to spring context and contains the JavaFX2 start method; Implement this
 * abstract class and add a main method to call the default JavaFX2 launch
 * ("Application.launch(args);") sequence
 * 
 * @author Andy Moncsek
 * 
 */
public abstract class AFXSpringLauncher extends Application {
    private final String springXML;
	private final String workbenchName;
    private AFXWorkbench workbench;
	/**
	 * default constructor; add reference to valid spring.xml
	 * @param springXML
	 */
    protected AFXSpringLauncher(final String springXML) {
		this.springXML = springXML;
		this.workbenchName = null;
	}

	public AFXSpringLauncher(final String springXML, final String workbenchName) {
		this.springXML = springXML;
		this.workbenchName = workbenchName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage) throws Exception {
        scanPackegesAndInitRegestry();
        final Launcher<ClassPathXmlApplicationContext> launcher = new SpringLauncher(this.springXML);
        final Class<? extends FXWorkbench> workbenchHandler = getWorkbechClass();
        if(workbenchHandler==null)throw new ComponentNotFoundException("no FXWorkbench class defined");
        if(workbenchHandler.isAnnotationPresent(Workbench.class)) {
            this.workbench = createWorkbench(workbenchHandler,launcher);
            workbench.init(launcher, stage);
            postInit(stage);
        } else {
            throw new AnnotationNotFoundException("no @Workbench annotation found on class");
        }


	}

    private EmbeddedFXWorkbench createWorkbench(final Class<? extends FXWorkbench> workbenchHandler,final Launcher<ClassPathXmlApplicationContext> launcher) {
        final Workbench annotation = workbenchHandler.getAnnotation(Workbench.class);
        final String id = annotation.id();
        if(id.isEmpty()) throw new AttributeNotFoundException("no workbench id found for: "+workbenchHandler.getClass());
        final FXWorkbench handler = launcher.registerAndGetBean(workbenchHandler, id, Scope.SINGLETON);
        return  new EmbeddedFXWorkbench(handler);
    }

    public AFXWorkbench getWorkbench() {
        return this.workbench;
    }

    protected abstract Class<? extends FXWorkbench> getWorkbechClass();

    protected void scanPackegesAndInitRegestry() {
       final String[] packages = getBasePackages();
       if(packages==null) throw new InvalidParameterException("no packes declared, declare all packages containing perspectives and components");
       final List<String> packageList = Arrays.asList(packages);
       final ClassFinder finder = new ClassFinder();
       packageList.parallelStream().forEach(p->{
            try {
                ClassRegistry.addClasses(Arrays.asList(finder.getAll(p)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Return all packages which contains components and perspectives that should be scanned. This is needed to find components/prespectives by id.
     * @return
     */
    protected abstract String[] getBasePackages();

    /**
     *  Will be executed after Spring/JavaFX initialisation.
     * @param stage
     */
	protected abstract void postInit(Stage stage);

}
