package org.jacp.javafx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.api.annotations.workbench.Workbench;
import org.jacp.api.component.IDeclarative;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.Injectable;
import org.jacp.api.context.Context;
import org.jacp.api.dialog.Scope;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.perspective.EmbeddedFXPerspective;
import org.jacp.javafx.rcp.registry.ClassRegistry;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 03.09.13
 * Time: 08:59
 * Contains utility classes for a Workbench
 */
public class WorkbenchUtil {

    private static final Logger LOGGER = Logger.getLogger(WorkbenchUtil.class.getName());

    private final Launcher<?> launcher;

    private WorkbenchUtil(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    /**
     * Returns an instance of the WorkbenchUtil
     * @param launcher
     * @return  The WorkbechUtil instance
     */
    public static WorkbenchUtil getInstance(final Launcher<?> launcher) {
        return new WorkbenchUtil(launcher);
    }


    /**
     * Creates all perspective instances by annotated id's in @Workbench annotation
     *
     * @param annotation , the workbench annotation
     * @return  a list with all perspectives associated with a workbench
     */
    public List<IPerspective<EventHandler<Event>, Event, Object>> createPerspectiveInstances(final Workbench annotation) {
        final Stream<String> componentIds = CommonUtil.getStringStreamFromArray(annotation.perspectives());
        final Stream<Injectable> perspectiveHandlerList = componentIds
                .map(this::mapToInjectable);
        return perspectiveHandlerList.map(this::mapToPerspective).collect(Collectors.toList());
    }

    /**
     * Returns a FXPerspective instance.
     * @param handler, the handler
     * @return The FXPerspective instance
     */
    private IPerspective<EventHandler<Event>, Event, Object> mapToPerspective(Injectable handler) {
        return new EmbeddedFXPerspective(handler);
    }

    /**
     * Returns a handler by id.
     * @param id, The component id
     * @return, The handler instance.
     */
    private Injectable mapToInjectable(final String id) {
        final Class perspectiveClass = ClassRegistry.getPerspectiveClassById(id);
        final Object component = launcher.registerAndGetBean(perspectiveClass, id, Scope.SINGLETON);
        if (Injectable.class.isAssignableFrom(component.getClass())) {
            return Injectable.class.cast(component);
        } else {
            throw new InvalidParameterException("Only IPerspective components are allowed");
        }
    }

    /**
     * set meta attributes defined in annotations
     *
     * @param perspective, the perspective where to handle the metadata
     * @param parentId, the id of parent workbench
     */
    public static void handleMetaAnnotation(
            final IPerspective<EventHandler<Event>, Event, Object> perspective, final String parentId) {
        final Injectable handler = perspective.getPerspectiveHandle();
        final Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(Perspective.class);
        if (perspectiveAnnotation == null) throw new IllegalArgumentException("no perspective annotation found");
        final String id = perspectiveAnnotation.id();
        if (id == null) throw new IllegalArgumentException("no perspective id set");
        initContext(perspective.getContext(), parentId, id, perspectiveAnnotation.active(), perspectiveAnnotation.name());
        LOGGER.fine("register perspective with annotations : "
                + perspectiveAnnotation.id());
        initDeclarativePerspectiveParts(perspective,perspectiveAnnotation);
        initLocaleAttributes(perspective,perspectiveAnnotation);
        initResourceBundleAttributes(perspective,perspectiveAnnotation);
    }

    /**
     * Set all resource bundle attributes.
     * @param perspective, the perspective instance
     * @param perspectiveAnnotation, the @Perspective annotation
     */
    private static void initResourceBundleAttributes(final IPerspective<EventHandler<Event>, Event, Object> perspective,final Perspective perspectiveAnnotation) {
        final String resourceBundleLocation = perspectiveAnnotation
                .resourceBundleLocation();
        if (resourceBundleLocation.length() > 1)
            perspective.setResourceBundleLocation(resourceBundleLocation);
    }

    /**
     * Set locale attributes.
     * @param perspective , the perspective instance
     * @param perspectiveAnnotation, the @Perspective annotation
     */
    private static void initLocaleAttributes(final IPerspective<EventHandler<Event>, Event, Object> perspective,final Perspective perspectiveAnnotation) {
        final String localeID = perspectiveAnnotation.localeID();
        if (localeID.length() > 1)
            perspective.setLocaleID(localeID);
    }

    /**
     * Set all metadata for a declarative perspective
     * @param perspective , the perspective instance
     * @param perspectiveAnnotation,the @Perspective annotation
     */
    private static void initDeclarativePerspectiveParts(final IPerspective<EventHandler<Event>, Event, Object> perspective,final Perspective perspectiveAnnotation) {
        final String viewLocation = perspectiveAnnotation.viewLocation();
        if (viewLocation.length() > 1 && IDeclarative.class.isAssignableFrom(perspective.getClass())) {
            final IDeclarative declarative = IDeclarative.class.cast(perspective);
            declarative.setViewLocation(perspectiveAnnotation.viewLocation());
            declarative.setUIType(UIType.DECLARATIVE);
        }
    }

    /**
     * Create context object instance.
     * @param contextInterface, the context instance
     * @param parentId, the parent id
     * @param id, the component id
     * @param active, the active state
     * @param name, the component name
     */
    private static void initContext(final Context contextInterface, final String parentId, final String id, final boolean active, final String name) {
        final JACPContextImpl context = JACPContextImpl.class.cast(contextInterface);
        context.setParentId(parentId);
        context.setId(id);
        context.setActive(active);
        context.setName(name);
    }
}
