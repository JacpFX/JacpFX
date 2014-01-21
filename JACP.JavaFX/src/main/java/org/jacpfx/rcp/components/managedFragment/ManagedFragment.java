/************************************************************************
 *
 * Copyright (C) 2010 - 2013
 *
 * [ManagedFragment.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacpfx.rcp.components.managedFragment;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.fragment.Fragment;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.exceptions.ManagedFragmentAnnotationFXMLMissingException;
import org.jacpfx.api.exceptions.ManagedFragmentAnnotationMissingException;
import org.jacpfx.api.exceptions.ManagedFragmentNotInitializedException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.util.CustomSecurityManager;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * The ManagedFragment handles creation of managed dialog components. A
 * managed Fragment is part of an UIComponent, it has always a UIComponent as a
 * parent element (normally the caller component), it has full access to DI
 * injection (services, etc.) and it can be used as managed node or JACPModal
 * dialog.
 *
 * @author Andy Moncsek
 */
public class ManagedFragment {
    /**
     * the reference to DI container
     */
    private static Launcher<?> launcher;

    /**
     * The running instance.
     */
    private static ManagedFragment instance;


    /**
     * the singleton scoped dialogs cache
     */
    private static final Map<String, ManagedFragmentHandler<?>> cache = new ConcurrentHashMap<>();


    private final static CustomSecurityManager customSecurityManager =
            new CustomSecurityManager();

    /**
     * initialize the JACPManaged dialog.
     *
     * @param launcher The launcher object to recive new managed objects
     */
    public static void initManagedFragment(Launcher<?> launcher) {
        if (ManagedFragment.instance == null) {
            ManagedFragment.launcher = launcher;
            ManagedFragment.instance = new ManagedFragment();
        }
    }

    /**
     * Returns an instance of ManagedFragment, to create managed dialogs.
     *
     * @return the instance
     */
    public static ManagedFragment getInstance() {
        if (ManagedFragment.instance == null)
            throw new ManagedFragmentNotInitializedException();
        return ManagedFragment.instance;
    }

    /**
     * Creates a managed dialog.
     *
     * @param type the class of the requested managed fragment
     * @param callerClassName the caller class of this method
     * @param <T> the type of the requested managed fragment
     * @return a managed dialog handler see {@link ManagedFragmentHandler}
     */
    public <T> ManagedFragmentHandler<T> getManagedFragment(Class<? extends T> type, final String callerClassName) {
        final Fragment dialogAnnotation = type.getAnnotation(Fragment.class);
        if (dialogAnnotation == null)
            throw new ManagedFragmentAnnotationMissingException();
        final String id = dialogAnnotation.id();
        final ManagedFragmentHandler<T> dialogFromCache = getDialogfromCache(id);
        if (dialogFromCache != null) return dialogFromCache;
        final Scope scope = dialogAnnotation.scope();
        final T bean = launcher.registerAndGetBean(type, id, scope);
        final String resourceBundleLocation = dialogAnnotation
                .resourceBundleLocation();
        final String localeID = dialogAnnotation.localeID();
        final ResourceBundle bundle = FXUtil.getBundle(resourceBundleLocation,
                localeID);
        try {
            checkMemberAnnotations(bean, bundle, callerClassName);
        } catch (IllegalArgumentException e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
        if (bean instanceof Node)
            return new ManagedFragmentHandler<>(bean, (Node) bean, id);

        return putDialogToCache(id, scope, createFXMLDialog(dialogAnnotation, id, bean, bundle));
    }

    /**
     * Creates a managed dialog.
     *
     * @param type the class of the requested managed fragment
     * @param <T> the type of the requested managed fragment
     * @return a managed dialog handler see {@link ManagedFragmentHandler}
     */
    public <T> ManagedFragmentHandler<T> getManagedDialog(Class<? extends T> type) {
        final String callerClassName = customSecurityManager.getCallerClassName();
        return getManagedFragment(type, callerClassName);

    }

    private <T> ManagedFragmentHandler<T> getDialogfromCache(final String id) {
        if (cache.containsKey(id)) return (ManagedFragmentHandler<T>) cache.get(id);
        return null;
    }

    /**
     * puts a singleton dialog instance to cache
     *
     * @param id
     * @param scope
     * @param handler
     * @param <T>
     */
    private <T> ManagedFragmentHandler<T> putDialogToCache(final String id, final Scope scope, final ManagedFragmentHandler<T> handler) {
        if (scope.equals(Scope.SINGLETON) && !cache.containsKey(id)) cache.put(id, handler);
        return handler;
    }

    /**
     * create the root node from FXML
     *
     * @param dialogAnnotation
     * @param id
     * @param bean
     * @param bundle
     * @return
     */
    private <T> ManagedFragmentHandler<T> createFXMLDialog(
            final Fragment dialogAnnotation, final String id,
            final T bean, final ResourceBundle bundle) {
        final String viewLocation = dialogAnnotation.viewLocation();
        if (viewLocation == null)
            throw new ManagedFragmentAnnotationFXMLMissingException();
        final URL url = getClass().getResource(viewLocation);
        return new ManagedFragmentHandler<>(bean,
                FXUtil.loadFXMLandSetController(bean, bundle, url), id);
    }

    /**
     * checks and handles all annotations
     *
     * @param bean
     * @param bundle
     * @param callerClassName
     * @param <T>
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private <T> void checkMemberAnnotations(final T bean,
                                            final ResourceBundle bundle, final String callerClassName)
            throws IllegalArgumentException {
        final Field[] fields = bean.getClass().getDeclaredFields();
        Stream.of(fields).parallel().forEach(field -> {
            final Resource resource = field.getAnnotation(Resource.class);
            if (resource != null) {
                try {
                    if (bundle != null && field.getType().isAssignableFrom(bundle.getClass())) {
                        field.setAccessible(true);
                        field.set(bean, bundle);
                    } else if (FXComponent.class.isAssignableFrom(field.getType())) {
                        handleParentComponentAnnotation(bean, field, resource,
                                callerClassName);
                    } else if (FXPerspective.class.isAssignableFrom(field.getType())) {
                        handleParentPerspectiveAnnotation(bean, field, resource,
                                callerClassName);
                    } else if (JacpContext.class.isAssignableFrom(field.getType())) {
                        handleParentComponentContextAnnotation(bean, field, resource,
                                callerClassName);
                    }
                } catch (IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    private <T> void handleParentComponentAnnotation(final T bean,
                                                     final Field field, final Resource resource,
                                                     final String callerClassName) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException {
        final SubComponent<EventHandler<Event>, Event, Object> comp = findSubcomponent(resource, callerClassName);
        if (comp == null)
            throw new IllegalArgumentException("component could not be found");
        field.setAccessible(true);
        field.set(bean, comp.getComponent());
    }

    private <T> void handleParentPerspectiveAnnotation(final T bean,
                                                     final Field field, final Resource resource,
                                                     final String callerClassName) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException {
        final Perspective<EventHandler<Event>, Event, Object> persp = findPerspective(resource, callerClassName);
        if (persp == null)
            throw new IllegalArgumentException("component could not be found for class name: "+callerClassName);
        field.setAccessible(true);
        field.set(bean, persp.getPerspective());
    }

    private <T> void handleParentComponentContextAnnotation(final T bean,
                                                            final Field field, final Resource resource,
                                                            final String callerClassName) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        final SubComponent<EventHandler<Event>, Event, Object> comp = findSubcomponent(resource, callerClassName);
        if (comp != null) {
            field.set(bean, comp.getContext());
            return;
        }
        final Perspective<EventHandler<Event>, Event, Object> persp = findPerspective(resource, callerClassName);
        if (persp == null) throw new IllegalArgumentException("component could not be found");
        field.set(bean, persp.getContext());


    }

    private SubComponent<EventHandler<Event>, Event, Object> findSubcomponent(final Resource resource, final String callerClassName) throws ClassNotFoundException {
        final String parentId = resource.parentId();
        if (parentId.isEmpty()) {
            return ComponentRegistry.findComponentByClass(Class
                    .forName(callerClassName));
        } else {
            return ComponentRegistry.findComponentById(parentId);
        }

    }

    private Perspective<EventHandler<Event>, Event, Object> findPerspective(final Resource resource, final String callerClassName) throws ClassNotFoundException {
        final String parentId = resource.parentId();
        if (parentId.isEmpty()) {
            return PerspectiveRegistry.findPerspectiveByClass(Class
                    .forName(callerClassName));
        } else {
            return PerspectiveRegistry.findPerspectiveById(parentId);
        }
    }

}
