/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [PerspectiveUtil.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.component.Component;
import org.jacpfx.api.annotations.component.DeclarativeView;
import org.jacpfx.api.annotations.component.Stateless;
import org.jacpfx.api.annotations.component.View;
import org.jacpfx.api.component.*;
import org.jacpfx.api.exceptions.AnnotationNotFoundException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.rcp.component.*;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.perspective.EmbeddedFXPerspective;
import org.jacpfx.rcp.registry.ClassRegistry;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 21.09.13
 * Time: 16:26
 * Contains utility methods for perspective class
 */
public class PerspectiveUtil {

    private static final Logger LOGGER = Logger.getLogger(PerspectiveUtil.class.getName());

    private final Launcher<?> launcher;

    private PerspectiveUtil(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    /**
     * Returns an PerspectiveUtil instance.
     * @param launcher the launcher object
     * @return The PerspectiveUtil instance.
     */
    public static PerspectiveUtil getInstance(final Launcher<?> launcher) {
        return new PerspectiveUtil(launcher);
    }

    /**
     * Returns all declared subcomponents for an Perspective annotation.
     * @param perspectiveAnnotation the perspective annotation
     * @return  a list of al declared subcomponent instances.
     */
    public List<SubComponent<EventHandler<Event>, Event, Object>> createSubcomponents(final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final Stream<? extends Injectable> handlers = getInjectAbles(perspectiveAnnotation);
        return handlers.map(this::mapToSubcomponent).collect(Collectors.toList());
    }

    /**
     * Returns a single SunComponent by id
     * @param componentId the component id
     * @return the component instance
     */
    public SubComponent<EventHandler<Event>, Event, Object> createSubcomponentById(final String componentId) {
        return mapToSubcomponent(mapToInjectAbleComponent(FXUtil.getTargetPerspectiveId(componentId),FXUtil.getTargetComponentId(componentId)));
    }

    /**
     * Returns a list of all declared Injectables.
     * @param perspectiveAnnotation the perspective annotation
     * @return
     */
    private Stream<Injectable> getInjectAbles(final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final Stream<String> idStream = Stream.of(getComponentIds(perspectiveAnnotation));
        final String parentId = perspectiveAnnotation.id();
        return idStream.filter(id->!id.isEmpty()).map(id->mapToInjectAbleComponent(parentId,id));
    }

    /**
     * Returns an Injectable Class from Classpath by ID.
     * @param id the component id
     * @return
     */
    private Injectable mapToInjectAbleComponent(final String parentId,final String id) {
        final Class componentClass = ClassRegistry.getComponentClassById(id);
        final Scope scope = getCorrectScopeOfComponent(componentClass);
        final String qualifiedName = parentId.concat(FXUtil.PATTERN_GLOBAL).concat(id);
        final Object component = launcher.registerAndGetBean(componentClass, qualifiedName, scope);
        if (Injectable.class.isAssignableFrom(component.getClass())) {
            return Injectable.class.cast(component);
        } else {
            throw new InvalidParameterException("Only Injectable component are allowed");
        }
    }

    /**
     * Returns the ID from annotation for a perspective
     * @param perspective the perspective instance
     * @return the perspective id from annotation
     */
    public static String getPerspectiveIdFromAnnotation(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final Injectable handler = perspective.getPerspective();
        final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
        if (perspectiveAnnotation == null) throw new IllegalArgumentException("no perspective annotation found");
        final String id = perspectiveAnnotation.id();
        if (id == null) throw new IllegalArgumentException("no perspective id set");
        return id;
    }

    /**
     * Returns the correct scope.
     * @param componentClass the bean class
     * @return The Scope of a component.
     */
    private Scope getCorrectScopeOfComponent(final Class componentClass) {
        return componentClass.isAnnotationPresent(Stateless.class) ?
                Scope.PROTOTYPE :
                Scope.SINGLETON;
    }

    /**
     * Returns all component  id's from Perspective annotation
     * @param perspectiveAnnotation  the perspective annotation
     * @return all declared component id's from perspective annotation.
     */
    private String[] getComponentIds(final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        if (perspectiveAnnotation != null) {
            return perspectiveAnnotation.components();
        } else {
            throw new IllegalArgumentException("No perspective annotatation found");
        }
    }

    /**
     * Maps an Injectable interface to it's corresponding SubComponent,
     * This means that the Injectable will be wrapped to it's component type. This can be either a FXComponents, a Stateful- or a StatelessComponent.
     * @param handler , the component
     * @return a subcomponent
     */
    private SubComponent<EventHandler<Event>, Event, Object> mapToSubcomponent(final Injectable handler) {
        if (ComponentView.class.isAssignableFrom(handler.getClass())) {
            return new EmbeddedFXComponent(ComponentView.class.cast(handler));
        } else if (ComponentHandle.class.isAssignableFrom(handler.getClass())) {
            return handler.getClass().isAnnotationPresent(Stateless.class) ?
                    new EmbeddedStatelessCallbackComponent(ComponentHandle.class.cast(handler)) :
                    new EmbeddedStatefulComponent(ComponentHandle.class.cast(handler));
        } else {
            throw new InvalidParameterException("No useable component interface found");
        }

    }


    /**
     * Set meta attributes defined in annotations.
     *
     * @param component ; the component containing metadata.
     */
    public static void  handleComponentMetaAnnotation(final SubComponent<EventHandler<Event>, Event, Object> component) {
        final ComponentHandle<?,Event,Object> handler = component.getComponent();
        if(handler==null)return;
        final DeclarativeView declarativeComponent = handler.getClass()
                .getAnnotation(DeclarativeView.class);
        if (declarativeComponent != null && FXComponent.class.isAssignableFrom(handler.getClass())) {
            handleDeclarativeComponentAnnotation(component,declarativeComponent);
            return;
        }
        final Component componentAnnotation = handler.getClass().getAnnotation(Component.class);
        if(componentAnnotation !=null && CallbackComponent.class.isAssignableFrom(handler.getClass())){
            handleCallbackAnnotation(component, componentAnnotation);
            log("register CallbackComponent with annotations : " + componentAnnotation.id());
            return;
        }
        final View viewComponent = handler.getClass()
                .getAnnotation(View.class);
        if (viewComponent !=null && FXComponent.class.isAssignableFrom(handler.getClass())) {
            handleViewComponentAnnotation(component, viewComponent);
            log("register component with annotations : " + viewComponent.id());
            return;
        }

        if(FXComponent.class.isAssignableFrom(handler.getClass()) && declarativeComponent==null && viewComponent==null) {
            throw new AnnotationNotFoundException("FXComponents must declare either @View or @DeclarativeView! no valid annotation found for component:"+ (componentAnnotation != null ? componentAnnotation.id() : null));
        } else if(CallbackComponent.class.isAssignableFrom(handler.getClass()) && componentAnnotation==null) {
            throw new IllegalArgumentException("no @Component annotation found.");
        }

    }

    /**
     * Handle all metadata for an declarative component.
     * @param component, The target component.
     * @param declarativeComponent, The @Declarative component annotation.
     */
    private static void handleDeclarativeComponentAnnotation(final SubComponent<EventHandler<Event>, Event, Object> component, final DeclarativeView declarativeComponent) {
        setInitialLayoutTarget(component, declarativeComponent.initialTargetLayoutId());
        setLocale(component, declarativeComponent.localeID());
        setResourceBundleLocation(component, declarativeComponent.resourceBundleLocation());
        handleBaseAttributes(component, declarativeComponent.id(), declarativeComponent.active());
        EmbeddedFXComponent.class.cast(component).setViewLocation(declarativeComponent.viewLocation());
    }

    /**
     * Handle a callback component metadata.
     * @param component, The target component.
     * @param callbackAnnotation, The callback annotation.
     */
    private static void handleCallbackAnnotation(final SubComponent<EventHandler<Event>, Event, Object> component, final Component callbackAnnotation) {
        handleBaseAttributes(component, callbackAnnotation.id(), callbackAnnotation.active());
    }

    /**
     * Set all metadata from @View and @Component annotation to the target component.
     * @param component, The target component.
     * @param viewComponent, The @View annotation.
     */
    private static void handleViewComponentAnnotation(final SubComponent<EventHandler<Event>, Event, Object> component,final View viewComponent) {
        handleBaseAttributes(component, viewComponent.id(), viewComponent.active());
        setInitialLayoutTarget(component, viewComponent.initialTargetLayoutId());
        setLocale(component, viewComponent.localeID());
        setResourceBundleLocation(component, viewComponent.resourceBundleLocation());
        log("register component with annotations : " + viewComponent.id());
    }

    /**
     * set base component members
     *
     * @param component, the component where the base attributes are set
     * @param id, the component id
     * @param active, is component active
     */
    private static void handleBaseAttributes(final SubComponent<EventHandler<Event>, Event, Object> component, final String id, final boolean active) {
        final InternalContext context = InternalContext.class.cast(component.getContext());
        if (id != null) context.setId(id);
        context.updateActiveState(active);
    }


    /**
     * Set the resource bundle location to component.
     * @param component, The target component.
     * @param bundleLocation, The bundle location.
     */
    private static void setResourceBundleLocation(final SubComponent<EventHandler<Event>, Event, Object> component, String bundleLocation) {
        if (component.getResourceBundleLocation() != null)
            component.setResourceBundleLocation(bundleLocation);
    }

    /**
     * Set the locale id to component.
     * @param component, The target component.
     * @param locale , The target value.
     */
    private static void setLocale(final SubComponent<EventHandler<Event>, Event, Object> component, String locale) {
        if (component.getLocaleID() != null)
            component.setLocaleID(locale);
    }


    /**
     * Set the targetLayout on context object, if none was set before.
     * @param component The target component.
     * @param value The target value.
     */
    private static void setInitialLayoutTarget(final SubComponent<EventHandler<Event>, Event, Object> component, String value) {
        final String targetLayout = InternalContext.class.cast(component.getContext()).getTargetLayout();
        if (targetLayout==null)
            component.getContext().setTargetLayout(value);
    }

    /**
     * Returns the PerspectiveLayout instance from perspective interface
     * @param parentPerspective the parent perspective
     * @return the perspectiveLayout of this perspective
     */
    public static PerspectiveLayout getPerspectiveLayoutFromPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective) {
        final EmbeddedFXPerspective embeddedPerspective = EmbeddedFXPerspective.class.cast(parentPerspective);
        return PerspectiveLayout.class.cast(embeddedPerspective.getIPerspectiveLayout());
    }

    private static void log(final String message) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.finest(">> " + message);
        }
    }
}
