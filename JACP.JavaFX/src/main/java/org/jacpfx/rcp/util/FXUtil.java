/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [FX2Util.java]
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
package org.jacpfx.rcp.util;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.component.Component;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.context.JacpContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Util class with helper methods
 *
 * @author Andy Moncsek
 */
public class FXUtil {

    public static final String AFXCOMPONENT_ROOT = "root";
    public static final String ACOMPONENT_ACTIVE = "active";
    public static final String ACOMPONENT_ID = "id";
    public static final String ACOMPONENT_NAME = "name";
    public static final String ACOMPONENT_EXTARGET = "executionTarget";
    public static final String ACOMPONENT_BLOCKED = "blocked";
    public static final String ACOMPONENT_STARTED = "started";
    public static final String APERSPECTIVE_MQUEUE = "messageQueue";
    public static final String IDECLARATIVECOMPONENT_VIEW_LOCATION = "viewLocation";
    public static final String IDECLARATIVECOMPONENT_TYPE = "type";
    public static final String IDECLARATIVECOMPONENT_DOCUMENT_URL = "documentURL";
    public static final String IDECLARATIVECOMPONENT_LOCALE = "localeID";
    public static final String IDECLARATIVECOMPONENT_BUNDLE_LOCATION = "resourceBundleLocation";
    public static final String AFXPERSPECTIVE_PERSPECTIVE_LAYOUT = "perspectiveLayout";
    private final static String PATTERN_LOCALE ="_";
    private final static String PATTERN_SPLIT="\\.";
    private final static String PATTERN_GLOBAL=".";


    /**
     * contains constant values
     *
     * @author Andy Moncsek
     */
    public static class MessageUtil {
        public static final String INIT = "init";
    }

    /**
     * returns children of current node
     *
     * @param node , the node where you want to get the child list
     * @return all children of that node
     */
    @SuppressWarnings("unchecked")
    public static ObservableList<Node> getChildren(final Node node) {
        if (node instanceof Parent) {
            final Parent tmp = (Parent) node;
            Method protectedChildrenMethod;
            ObservableList<Node> returnValue = null;
            try {
                protectedChildrenMethod = Parent.class
                        .getDeclaredMethod("getChildren");
                protectedChildrenMethod.setAccessible(true);

                returnValue = (ObservableList<Node>) protectedChildrenMethod
                        .invoke(tmp);

            } catch (final NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(FXUtil.class.getName()).log(Level.SEVERE,
                        null, ex);
            }

            return returnValue;
        }

        return null;

    }

    /**
     * Set a value to a private member on specified object
     * @param superClass , the class
     * @param object , the Object with the private member to be set
     * @param member , the name of the member
     * @param value  , the vakue of the member
     */
    public static void setPrivateMemberValue(final Class<?> superClass,
                                             final Object object, final String member, final Object value) {
        try {
            final Field privateStringField = superClass
                    .getDeclaredField(member);
            privateStringField.setAccessible(true);
            privateStringField.set(object, value);

        } catch (final SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Logger.getLogger(FXUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    /**
     * find annotated method in component and pass value
     * @param annotation , the annotation to find
     * @param component , the component with the annotated method
     * @param value , the values to pass to the annotated method
     */
    public static void invokeHandleMethodsByAnnotation(
            final Class annotation, final Object component,
            final Object... value) {
        final Class<?> componentClass = component.getClass();
        final Method[] methods = componentClass.getMethods();
        for (final Method m : methods) {
            if (m.isAnnotationPresent(annotation)) {
                try {
                    final Class<?>[] types = m.getParameterTypes();
                    if (types.length == value.length) {
                        m.invoke(component, value);
                        return;
                    }
                    if (types.length > 0) {
                        m.invoke(component, getValidParameterList(types, value));
                        return;
                    }

                    m.invoke(component);
                    return;

                } catch (final IllegalArgumentException e) {
                    throw new UnsupportedOperationException(
                            "use @PostConstruct and @PreDestroy either with paramter extending BaseLayout<Node> layout (like FXComponentLayout) or with no arguments  ",
                            e.getCause());
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    Logger.getLogger(FXUtil.class.getName()).log(Level.SEVERE,
                            null, e);
                }
                break;
            }
        }
    }

    /**
     * Injects all Resource memberc like Context
     * @param handler , the component where injection should be performed
     * @param context , the context object
     */
    public static void performResourceInjection(final Injectable handler,JacpContext<EventHandler<Event>, Object> context) {
        final Field[] fields = handler.getClass().getDeclaredFields();
        final List<Field> fieldList = Arrays.asList(fields);
        fieldList.parallelStream().filter(f -> f.isAnnotationPresent(Resource.class)).forEach(f -> {
            // context injection
            if (f.getType().isAssignableFrom(context.getClass())) {
                injectContext(handler, f, context);
            } else if (context.getResourceBundle() != null && f.getType().isAssignableFrom(context.getResourceBundle().getClass())) {
                injectResourceBundle(handler, f, context.getResourceBundle());
            }

        });
    }

    /**
     *
     * @param handler the component where injection should be performed
     * @param f the field which should be injected
     * @param context, the context object
     */
    private static void injectContext(final Injectable handler,final Field f, final JacpContext context) {
        f.setAccessible(true);
        try {
            f.set(handler, context);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Injects the resource bundle to component
     * @param handler the component where injection should be performed
     * @param f  the field which should be injected
     * @param bundle  the bundle that sould be injected
     */
    private static void injectResourceBundle(final Injectable handler,final Field f, final ResourceBundle bundle) {
        f.setAccessible(true);
        try {
            f.set(handler, bundle);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Object[] getValidParameterList(final Class<?>[] types,
                                                  Object... value) {
        final List<Object> resultList = Arrays.asList(types).
                parallelStream().map(t -> findByClass(t, value)).
                collect(Collectors.toList());
        return !resultList.isEmpty() ?resultList.toArray(new Object[resultList.size()]):new Object[types.length];
    }

    /**
     * Returns an object instance by class
     * @param key
     * @param values
     * @return The instance
     */
    private static Object findByClass(Class<?> key, Object[] values) {
        if (key == null)
            return null;
        for (Object val : values) {
            if (val == null)
                return null;
            final Class<?> clazz = val.getClass();
            if (clazz == null)
                return null;
            if (clazz.getGenericSuperclass().equals(key) || clazz.equals(key))
                return val;
        }
        return null;
    }

    /**
     * Returns the correct locale by String
     * @param localeID the locale id
     * @return  The locale object
     */
    public static Locale getCorrectLocale(final String localeID) {
        final Locale locale = Locale.getDefault();
        if (localeID != null && localeID.length() > 1) {
            if (localeID.contains(PATTERN_LOCALE)) {
                final String[] loc = Pattern.compile(PATTERN_LOCALE).split(localeID);
                return new Locale(loc[0], loc[1]);
            } else {
                return new Locale(localeID);
            }

        }
        return locale;
    }

    /**
     * Returns the resourceBundle
     *
     * @param resourceBundleLocation thge location of your resource bundle
     * @param localeID  the locale id
     * @return The resouceBundle instance
     */
    public static ResourceBundle getBundle(String resourceBundleLocation,
                                           final String localeID) {
        if (resourceBundleLocation == null
                || resourceBundleLocation.length() <= 1)
            return null;
        return ResourceBundle.getBundle(resourceBundleLocation,
                FXUtil.getCorrectLocale(localeID));
    }

    /**
     * Loads the FXML document provided by viewLocation-
     *
     * @param bean   the controller
     * @param bundle the ressource bundle
     * @param url    the fxml url
     * @param <T> the type of the bean
     * @return The components root Node.
     */
    public static <T> Node loadFXMLandSetController(final T bean,
                                                    final ResourceBundle bundle, final URL url) {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        if (bundle != null) {
            fxmlLoader.setResources(bundle);
        }
        fxmlLoader.setLocation(url);
        fxmlLoader.setController(bean);
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new MissingResourceException(
                    "fxml file not found --  place in resource folder and reference like this: viewLocation = \"/myUIFile.fxml\"",
                    url.getPath(), "");
        }
    }





    /**
     * returns the message target perspective id
     *
     * @param messageId the message id to analyze
     * @return returns the perspective id
     */
    public static String getTargetPerspectiveId(final String messageId) {
        if (!FXUtil.isLocalMessage(messageId)) {
            return getParentFromId(messageId);
        }
        return messageId;
    }

    /**
     * Returns the parent part of id ... parent.child
     * @param messageId the message id to analyze
     * @return returns the first part of message id "parent.child"
     */
    public static String getParentFromId(final String messageId) {
        final String[] targetId = FXUtil.getTargetId(messageId);
        return targetId[0];
    }

    /**
     * returns the message target component id
     *
     * @param messageId the message id to analyze
     * @return  returns the component id
     */
    public static String getTargetComponentId(final String messageId) {
        if (!FXUtil.isLocalMessage(messageId)) {
            final String[] targetId = FXUtil.getTargetId(messageId);
            return targetId[1];
        }
        return messageId;
    }

    /**
     * when id has no separator it is a local message
     *
     * @param messageId the message id to analyze
     * @return true when message is not seperated by a dot
     */
    public static boolean isLocalMessage(final String messageId) {
        return !messageId.contains(PATTERN_GLOBAL);
    }

    /**
     * returns target message with perspective and component name as array
     *
     * @param messageId the message id to analyze
     * @return  returns a string array of the message id
     */
    private static String[] getTargetId(final String messageId) {
        return messageId.split(PATTERN_SPLIT);
    }

    /**
     * Returns a component by id from a provided component list
     *
     * @param id the component id to look for
     * @param components the component list
     * @param <P>  the concrete type of components
     * @return  the component by id
     */
    public static <P extends Component<EventHandler<Event>, Object>> P getObserveableById(
            final String id, final List<P> components) {
        final Optional<P> filter = components.parallelStream().
                filter(comp -> comp.getContext().getId() != null).
                filter(c -> c.getContext().getId().equals(id)).
                findFirst();
        if (filter.isPresent()) return filter.get();
        return null;
    }
    /**
     * Returns a component by full qualified id (like parentId.componentId) from a provided component list
     *
     * @param id the component id to look for
     * @param components the component list
     * @param <P>  the concrete type of components
     * @return  the component by id
     */
    public static <P extends Component<EventHandler<Event>, Object>> P getObserveableByQualifiedId(
            final String id, final List<P> components) {
        final String parentId=  getParentFromId(id);
        final String componentId= getTargetComponentId(id);
        final Optional<P> filter = components.parallelStream().
                filter(comp -> comp.getContext().getId() != null && comp.getContext().getParentId()!=null).
                filter(c -> c.getContext().getId().equals(componentId) && c.getContext().getParentId().equals(parentId)).
                findFirst();
        if (filter.isPresent()) return filter.get();
        return null;
    }


    /**
     * find the parent perspective to id; should be only used when no
     * responsible component was found ,
     *
     * @param id the id of the child component
     * @param perspectives  the perspective list
     * @return  the parent perspective for the component with the id in param list
     */
    public static Perspective<EventHandler<Event>, Event, Object> findRootByObserveableId(
            final String id,
            final List<Perspective<EventHandler<Event>, Event, Object>> perspectives) {
        final Optional<Perspective<EventHandler<Event>, Event, Object>> result = perspectives.
                parallelStream().
                filter(perspective ->
                        perspective.getSubcomponents().
                                parallelStream().map(s -> s.getContext().getId()).
                                anyMatch(cId -> cId.equals(id))).findFirst();
        if (result.isPresent()) return result.get();
        return null;
    }





}
