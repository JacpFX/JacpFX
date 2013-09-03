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
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.perspective.AFXPerspective;
import org.jacp.javafx.rcp.perspective.EmbeddedFXPerspective;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public WorkbenchUtil(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    public static WorkbenchUtil getInstance(final Launcher<?> launcher) {
        return new WorkbenchUtil(launcher);
    }


    /**
     * Creates all perspective instances by annotated id's in @Workbench annotation
     *
     * @param annotation
     * @return
     */
    public List<IPerspective<EventHandler<Event>, Event, Object>> createPerspectiveInstances(final Workbench annotation) {
        final String[] ids = annotation.perspectives();
        final List<String> componentIds = Arrays.asList(ids);
        final List<Injectable> perspectiveHandlerList = componentIds.stream()
                .map(this::mapToInjectable)
                .collect(Collectors.toList());
        return perspectiveHandlerList.stream().map(this::mapToPerspective).collect(Collectors.toList());
    }

    private IPerspective<EventHandler<Event>, Event, Object> mapToPerspective(Injectable handler) {
        return new EmbeddedFXPerspective(handler);
    }

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
     * @param perspective
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
        final String viewLocation = perspectiveAnnotation.viewLocation();
        if (viewLocation.length() > 1 && IDeclarative.class.isAssignableFrom(perspective.getClass())) {
            IDeclarative.class.cast(perspective).setViewLocation(perspectiveAnnotation.viewLocation());
            FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                    FXUtil.IDECLARATIVECOMPONENT_TYPE, UIType.DECLARATIVE);
        }

        final String localeID = perspectiveAnnotation.localeID();
        if (localeID.length() > 1)
            perspective.setLocaleID(localeID);
        final String resourceBundleLocation = perspectiveAnnotation
                .resourceBundleLocation();
        if (resourceBundleLocation.length() > 1)
            perspective.setResourceBundleLocation(resourceBundleLocation);


    }

    private static void initContext(final Context contextInterface, final String parentId, final String id, final boolean active, final String name) {
        final JACPContextImpl context = JACPContextImpl.class.cast(contextInterface);
        context.setParentId(parentId);
        context.setId(id);
        context.setActive(active);
        context.setName(name);
    }
}
