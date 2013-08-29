package org.jacp.javafx.rcp.util;

import org.jacp.api.annotations.component.Component;
import org.jacp.api.annotations.perspective.Perspective;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 13.08.13
 * Time: 16:09
 * Contains registered classes found by scanning. This class should be thread save by convention. addClasses Method is performed ONCE while bootstrapping the application, while getClasses/Perspectives is called during initialisation of workbench and perspective.
 */
public class ClassRegistry {
    private static volatile List<Class>  allClasses = new CopyOnWriteArrayList<>();

    /**
     * Add classes that were found while package scanning at application start up.
     * @param classes
     */
    public static void addClasses(List<Class> classes) {
        allClasses.addAll(classes);
    }

    /**
     * Returns an un-modifiable  list of all classes.
     * @return
     */
    public static List<Class> getAllClasses() {
        return Collections.unmodifiableList(allClasses);
    }


    /**
     * Returns a component class by ID
     * @param id
     * @return
     */
    public static Class getComponentClassById(final String id) {
        final Optional<Class> result = allClasses.parallelStream()
                .filter(ClassRegistry::checkForAnntotation)
                .filter(component -> checkIdMatch(component,id))
                .findFirst();

        return result.isPresent()?result.get():null;

    }

    /**
     * Returns a perspective class by ID.
     * @param id
     * @return
     */
    public static Class getPerspectiveClassById(final String id) {
        final Optional<Class> result = allClasses.parallelStream()
                .filter(ClassRegistry::checkForPerspectiveAnntotation)
                .filter(component -> checkPerspectiveIdMatch(component,id))
                .findFirst();

        return result.isPresent()?result.get():null;
    }

    private static boolean checkForAnntotation(final Class c) {
        return c.isAnnotationPresent(Component.class);
    }

    private static boolean checkForPerspectiveAnntotation(final Class c) {
        return c.isAnnotationPresent(Perspective.class);
    }

    private static boolean checkIdMatch(final Class component, final String id) {
        final Component annotation = (Component) component.getAnnotation(Component.class);
        return annotation.id().equalsIgnoreCase(id);
    }

    private static boolean checkPerspectiveIdMatch(final Class perspective, final String id) {
        final Perspective annotation = (Perspective) perspective.getAnnotation(Perspective.class);
        return annotation.id().equalsIgnoreCase(id);
    }
}
