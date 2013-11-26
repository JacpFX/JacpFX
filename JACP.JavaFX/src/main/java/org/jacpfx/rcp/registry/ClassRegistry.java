package org.jacpfx.rcp.registry;

import org.jacpfx.api.annotations.component.Component;
import org.jacpfx.api.annotations.component.DeclarativeView;
import org.jacpfx.api.annotations.component.View;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.exceptions.NonUniqueComponentException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 13.08.13
 * Time: 16:09
 * Contains registered classes found by scanning. This class should be thread save by convention. addClasses Method is performed ONCE while bootstrapping the application, while getClasses/Perspectives is called during initialisation of workbench and perspective.
 */
public class ClassRegistry {
    private static final List<Class>  allClasses = new CopyOnWriteArrayList<>();

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
     * @return The component class for requested id
     */
    public static Class getComponentClassById(final String id) {
        if(id==null || id.isEmpty())   throw new ComponentNotFoundException("following component id was not found: "+id);
        final List<Class> result = allClasses.parallelStream()
                .filter(ClassRegistry::checkForAnntotation)
                .filter(component -> checkIdMatch(component,id))
                .collect(Collectors.toList());
        return checkAndGetClassSearch(result,id);

    }

    /**
     * Returns a perspective class by ID.
     * @param id
     * @return The perspective class for requested id
     */
    public static Class getPerspectiveClassById(final String id) {
        if(id==null || id.isEmpty())   throw new ComponentNotFoundException("following perspective id was not found: "+id);
        final List<Class> result = allClasses.parallelStream()
                .filter(clazz->clazz.isAnnotationPresent(Perspective.class))
                .filter(component -> checkPerspectiveIdMatch(component, id)).collect(Collectors.toList());
        return checkAndGetClassSearch(result,id);
    }

    private static Class checkAndGetClassSearch(final List<Class> result,final String id) {
        if(result.isEmpty()) throw new ComponentNotFoundException("following perspective or component id was not found: "+id);
        if(result.size()>1) throw new NonUniqueComponentException("more than one component found for id "+id +" components: "+result);
        return result.get(0);
    }

    private static boolean checkForAnntotation(final Class c) {
        return c.isAnnotationPresent(Component.class) || c.isAnnotationPresent(View.class) || c.isAnnotationPresent(DeclarativeView.class);
    }

    private static boolean checkIdMatch(final Class component, final String id) {
        return getIdFromAnnotation(component).equalsIgnoreCase(id);
    }

    private static String getIdFromAnnotation(final Class component) {
        if(component.isAnnotationPresent(Component.class))return Component.class.cast(component.getAnnotation(Component.class)).id();
        if(component.isAnnotationPresent(View.class))return View.class.cast(component.getAnnotation(View.class)).id();
        if(component.isAnnotationPresent(DeclarativeView.class))return DeclarativeView.class.cast(component.getAnnotation(DeclarativeView.class)).id();
        return "";
    }

    private static boolean checkPerspectiveIdMatch(final Class perspective, final String id) {
        final Perspective annotation = (Perspective) perspective.getAnnotation(Perspective.class);
        return annotation.id().equalsIgnoreCase(id);
    }
}
