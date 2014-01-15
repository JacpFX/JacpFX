package org.jacpfx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.component.Declarative;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.context.ContextImpl;
import org.jacpfx.rcp.perspective.EmbeddedFXPerspective;
import org.jacpfx.rcp.registry.ClassRegistry;

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
    public List<Perspective<EventHandler<Event>, Event, Object>> createPerspectiveInstances(final Workbench annotation) {
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
    private Perspective<EventHandler<Event>, Event, Object> mapToPerspective(Injectable handler) {
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
            throw new InvalidParameterException("Only Perspective components are allowed");
        }
    }

    /**
     * set meta attributes defined in annotations
     *
     * @param perspective, the perspective where to handle the metadata
     * @param parentId, the id of parent workbench
     */
    public static void handleMetaAnnotation(
            final Perspective<EventHandler<Event>, Event, Object> perspective, final String parentId) {
        final Injectable handler = perspective.getPerspective();
        final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
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
     * Returns the ID from annotation for a perspective
     * @param perspective
     * @return
     */
    public static String getPerspectiveIdFromAnnotation(final Perspective<EventHandler<Event>, Event, Object> perspective) {
        final Injectable handler = perspective.getPerspective();
        final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
        if (perspectiveAnnotation == null) throw new IllegalArgumentException("no perspective annotation found");
        final String id = perspectiveAnnotation.id();
        if (id == null) throw new IllegalArgumentException("no perspective id set");
        return id;
    }

    /**
     * Set all resource bundle attributes.
     * @param perspective, the perspective instance
     * @param perspectiveAnnotation, the @Perspective annotation
     */
    private static void initResourceBundleAttributes(final Perspective<EventHandler<Event>, Event, Object> perspective,final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
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
    private static void initLocaleAttributes(final Perspective<EventHandler<Event>, Event, Object> perspective,final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final String localeID = perspectiveAnnotation.localeID();
        if (localeID.length() > 1)
            perspective.setLocaleID(localeID);
    }

    /**
     * Set all metadata for a declarative perspective
     * @param perspective , the perspective instance
     * @param perspectiveAnnotation,the @Perspective annotation
     */
    private static void initDeclarativePerspectiveParts(final Perspective<EventHandler<Event>, Event, Object> perspective,final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final String viewLocation = perspectiveAnnotation.viewLocation();
        if (viewLocation.length() > 1 && Declarative.class.isAssignableFrom(perspective.getClass())) {
            final Declarative declarative = Declarative.class.cast(perspective);
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
    private static void initContext(final JacpContext contextInterface, final String parentId, final String id, final boolean active, final String name) {
        final ContextImpl context = ContextImpl.class.cast(contextInterface);
        context.setParentId(parentId);
        context.setId(id);
        context.setActive(active);
        context.setName(name);
    }
}
