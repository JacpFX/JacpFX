/************************************************************************
 *
 * Copyright (C) 2010 - 2013
 *
 * [JACPManagedDialog.java]
 * AHCP Project (http://jacp.googlecode.com)
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
package org.jacp.javafx.rcp.components.managedDialog;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.annotations.Dialog;
import org.jacp.api.annotations.Resource;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.context.Context;
import org.jacp.api.dialog.Scope;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.util.CustomSecurityManager;
import org.jacp.javafx.rcp.component.ASubComponent;
import org.jacp.javafx.rcp.util.ComponentRegistry;
import org.jacp.javafx.rcp.util.FXUtil;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The JACPManagedDialog handles creation of managed dialog components. A
 * managed Dialog is part of an IUIComponent, it has always a IUIComponent as a
 * parent element (normally the caller component), it has full access to DI
 * injection (services, etc.) and it can be used as managed node or JACPModal
 * dialog.
 *
 * @author Andy Moncsek
 */
public class JACPManagedDialog {
    /**
     * the reference to DI container
     */
    private static Launcher<?> launcher;

    /**
     * The running instance.
     */
    private static JACPManagedDialog instance;


    /**
     * the singleton scoped dialogs cache
     */
    private static final Map<String, ManagedDialogHandler<?>> cache = new ConcurrentHashMap<>();


    private final static CustomSecurityManager customSecurityManager =
            new CustomSecurityManager();

    /**
     * initialize the JACPManaged dialog.
     *
     * @param launcher
     */
    public static void initManagedDialog(Launcher<?> launcher) {
        if (JACPManagedDialog.instance == null) {
            JACPManagedDialog.launcher = launcher;
            JACPManagedDialog.instance = new JACPManagedDialog();
        }
    }

    /**
     * Returns an instance of JACPManagedDialog, to create managed dialogs.
     *
     * @return the instance
     */
    public static JACPManagedDialog getInstance() {
        if (JACPManagedDialog.instance == null)
            throw new ManagedDialogNotInitializedException();
        return JACPManagedDialog.instance;
    }

    /**
     * Creates a managed dialog.
     *
     * @param type
     * @return a managed dialog handler see {@link ManagedDialogHandler}
     */
    public <T> ManagedDialogHandler<T> getManagedDialog(Class<? extends T> type,final String callerClassName) {
        final Dialog dialogAnnotation = type.getAnnotation(Dialog.class);
        if (dialogAnnotation == null)
            throw new ManagedDialogAnnotationMissingException();
        final String id = dialogAnnotation.id();
        final ManagedDialogHandler<T> dialogFromCache = getDialogfromCache(id);
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
            e.printStackTrace();
        }
        if (bean instanceof Node)
            return new ManagedDialogHandler<>(bean, (Node) bean, id);

        return putDialogToCache(id, scope, createFXMLDialog(dialogAnnotation, id, scope, bean, bundle));
    }
    /**
     * Creates a managed dialog.
     *
     * @param type
     * @return a managed dialog handler see {@link ManagedDialogHandler}
     */
    public <T> ManagedDialogHandler<T> getManagedDialog(Class<? extends T> type) {
        final String callerClassName = customSecurityManager.getCallerClassName();
          return getManagedDialog(type,callerClassName);

    }

    private <T> ManagedDialogHandler<T> getDialogfromCache(final String id) {
        if (cache.containsKey(id)) return (ManagedDialogHandler<T>) cache.get(id);
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
    private <T> ManagedDialogHandler<T> putDialogToCache(final String id, final Scope scope, final ManagedDialogHandler<T> handler) {
        if (scope.equals(Scope.SINGLETON) && !cache.containsKey(id)) cache.put(id, handler);
        return handler;
    }

    /**
     * create the root node from FXML
     *
     * @param dialogAnnotation
     * @param id
     * @param scope
     * @param bean
     * @param bundle
     * @param <T>
     * @return
     */
    private <T> ManagedDialogHandler<T> createFXMLDialog(
            final Dialog dialogAnnotation, final String id, final Scope scope,
            final T bean, final ResourceBundle bundle) {
        final String viewLocation = dialogAnnotation.viewLocation();
        if (viewLocation == null)
            throw new ManagedDialogAnnotationFXMLMissingException();
        final URL url = getClass().getResource(viewLocation);
        return new ManagedDialogHandler<>(bean,
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
        final List<Field> fieldList = Arrays.asList(fields);
        fieldList.parallelStream().forEach(field -> {
            final Resource resource = field.getAnnotation(Resource.class);
            if (resource != null) {
                try {
                    if (bundle != null && field.getType().isAssignableFrom(bundle.getClass())) {
                        field.setAccessible(true);
                        field.set(bean, bundle);

                    } else if (ASubComponent.class.isAssignableFrom(field.getType())
                            || IComponentHandle.class.isAssignableFrom(field.getType())) {
                        handleParentComponentAnnotation(bean, field, resource,
                                callerClassName);
                    } else if (Context.class.isAssignableFrom(field.getType())) {
                        handleParentComponentContextAnnotation(bean, field, resource,
                                callerClassName);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    private <T> void handleParentComponentAnnotation(final T bean,
                                                     final Field field, final Resource resource,
                                                     final String callerClassName) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException {
        final ISubComponent<EventHandler<Event>, Event, Object> comp = findSubcomponent(resource,callerClassName);
        if (comp == null)
            throw new IllegalArgumentException("component could not be found");
        field.setAccessible(true);
        field.set(bean, comp.getComponentHandle());
    }

    private <T> void handleParentComponentContextAnnotation(final T bean,
                                                            final Field field, final Resource resource,
                                                            final String callerClassName) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException {

        final ISubComponent<EventHandler<Event>, Event, Object> comp = findSubcomponent(resource,callerClassName);
        if (comp == null)
            throw new IllegalArgumentException("component could not be found");
        field.setAccessible(true);
        field.set(bean, comp.getContext());
    }

    private ISubComponent<EventHandler<Event>, Event, Object> findSubcomponent(final Resource resource,final String callerClassName) throws ClassNotFoundException {
        final String parentId = resource.parentId();
        ISubComponent<EventHandler<Event>, Event, Object> comp = null;
        if (parentId.isEmpty()) {
            comp = ComponentRegistry.findComponentByClass(Class
                    .forName(callerClassName));
        } else {
            comp = ComponentRegistry.findComponentById(parentId);
        }

        return comp;
    }

}
