/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [WorkerUtil.java]
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

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.method.OnMessage;
import org.jacpfx.api.annotations.method.OnMessageAsync;
import org.jacpfx.api.component.ComponentView;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.UIComponent;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.component.EmbeddedFXComponent;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 11.10.13
 * Time: 14:22
 * This util class contains methods needed in all types  of workers
 */
public class WorkerUtil {


    /**
     * find valid target and add type specific new component. Handles Container,
     * ScrollPanes, Menus and Bar Entries from user
     *
     * @param validContainer, a valid container where component root will be added
     * @param component,      the component
     */
    public static void addComponentByType(
            final Node validContainer,
            final UIComponent<Node, EventHandler<Event>, Event, Object> component) {
        handleAdd(validContainer, component.getRoot());
    }

    /**
     * enables component an add to container
     *
     * @param validContainer    , a valid container where component root will be added
     * @param componentViewNode , the component
     */
    private static void handleAdd(final Node validContainer, final Node componentViewNode) {
        if (componentViewNode != null) {
            handleViewState(componentViewNode, true);
            final Optional<ObservableList<Node>> children = FXUtil
                    .getChildren(validContainer);
            children.ifPresent(childList -> {
                if (!childList.contains(componentViewNode)) childList.add(componentViewNode);
            });

        }

    }

    /**
     * set visibility and enable/disable
     *
     * @param componentViewNode, a Node where to set the state
     * @param state,             the boolean value of the state
     */
    public static void handleViewState(final Node componentViewNode,
                                       final boolean state) {
        componentViewNode.setVisible(state);
        componentViewNode.setDisable(!state);
        componentViewNode.setManaged(state);
    }

    /**
     * delegate component handle return value to specified target
     *
     * @param comp,     the component
     * @param targetId, the message target id
     * @param value,    the message value
     * @param action,   the message
     */
    public static void delegateReturnValue(
            final SubComponent<EventHandler<Event>, Event, Object> comp,
            final String targetId, final Object value,
            final Message<Event, Object> action) {
        if (value != null && targetId != null
                && !action.messageBodyEquals("init")) {
            comp.getContext().send(targetId, value);
        }
    }

    /**
     * Executes post handle method in application main thread. The result value
     * of handle method (from worker thread) is Input for the postHandle Method.
     * The return value or the handleReturnValue are the root node of this
     * component.
     *
     * @param handleReturnValue the UI return value after "handle(message)" {@link org.jacpfx.api.component.ComponentHandle#handle(org.jacpfx.api.message.Message)} was executed
     * @param component,        a component
     * @param message,          the current message
     * @throws java.lang.Exception when an Exception occures while execute {@link org.jacpfx.api.component.ComponentView#postHandle(Object, org.jacpfx.api.message.Message)}
     */
    public static void executeComponentViewPostHandle(final Node handleReturnValue,
                                                      final EmbeddedFXComponent component, final Message<Event, Object> message) throws Exception {

        Node potsHandleReturnValue = component.getComponentViewHandle().postHandle(handleReturnValue,
                message);
        if (potsHandleReturnValue == null) {
            potsHandleReturnValue = handleReturnValue;
        } else if (component.getType().equals(UIType.DECLARATIVE)) {
            throw new UnsupportedOperationException(
                    "declarative component should not have a return value in postHandle method, otherwise you would overwrite the FXML root node.");
        }
        if (potsHandleReturnValue != null
                && component.getType().equals(UIType.PROGRAMMATIC)) {
            component.setRoot(potsHandleReturnValue);
        }
    }

    /**
     * Executes post handle method in application main thread. The result value
     * of handle method (from worker thread) is Input for the postHandle Method.
     * The return value or the handleReturnValue are the root node of this
     * component.
     *
     * @param handleReturnValue the UI return value after "handle(message)" {@link org.jacpfx.api.component.ComponentHandle#handle(org.jacpfx.api.message.Message)} was executed
     * @param component,        a component
     * @param message,          the current message
     * @throws java.lang.Exception when an Exception occures while execute {@link org.jacpfx.api.component.ComponentView#postHandle(Object, org.jacpfx.api.message.Message)}
     */
    public static void executeTypedComponentViewPostHandle(final Object handleReturnValue,
                                                      final EmbeddedFXComponent component, final Message<Event, Object> message, final Method method) throws Exception {

        final ComponentView<Node, Event, Object> componentViewHandle = component.getComponentViewHandle();
        FXUtil.invokeMethod(OnMessage.class,method,componentViewHandle,handleReturnValue,message);

    }

    /**
     * Move component to new target in perspective.
     *
     * @param delegateQueue, the component delegate queue
     * @param component,     the component
     */
    public static void changeComponentTarget(
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        // delegate to perspective observer
        final Thread t = Thread.currentThread();
        try {
            delegateQueue.put(component);
        } catch (InterruptedException e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }
    }

    /**
     * Runs the handle method of a componentView.
     *
     * @param component, the component
     * @param action,    the current message
     * @return a returned node from component execution {@link org.jacpfx.api.component.ComponentHandle#handle(org.jacpfx.api.message.Message)}
     * @throws java.lang.Exception when an Exception occures while execute {@link org.jacpfx.api.component.ComponentHandle#handle(org.jacpfx.api.message.Message)}
     */
    public static Node prepareAndRunHandleMethod(
            final UIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Message<Event, Object> action) throws Exception {
        return component.getComponentViewHandle().handle(action);
    }

    /**
     * Runs the handle method of a componentView.
     *
     * @param component, the component
     * @param message,    the current message
     * @return a returned node from component execution {@link org.jacpfx.api.component.ComponentHandle#handle(org.jacpfx.api.message.Message)}
     * @throws java.lang.Exception when an Exception occures while execute {@link org.jacpfx.api.component.ComponentHandle#handle(org.jacpfx.api.message.Message)}
     */
    public static Node prepareAndRunTypedHandleMethod(
            final UIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Message<Event, Object> message, BiConsumer<Object,Method> runOnFXThread) throws Exception {

        final ComponentView<Node, Event, Object> componentViewHandle = component.getComponentViewHandle();
        final Optional<Method> asnc = Stream.of(componentViewHandle.getClass().getMethods()).filter(method -> method.isAnnotationPresent(OnMessageAsync.class)).filter(method1 -> message.getMessageBody().getClass().isAssignableFrom(method1.getAnnotation(OnMessageAsync.class).value())).findFirst();
        final Optional<Method> sync = Stream.of(componentViewHandle.getClass().getMethods()).
                filter(method -> method.isAnnotationPresent(OnMessage.class)).
                filter(method1 -> message.getMessageBody().getClass().isAssignableFrom(method1.getAnnotation(OnMessage.class).value())).findFirst();
        asnc.ifPresent(method -> {
            final Object handleReturnValue = FXUtil.invokeMethod(OnMessageAsync.class,method,componentViewHandle,message);
            sync.ifPresent(methodSync -> {
                runOnFXThread.accept(handleReturnValue,methodSync);
            });
            System.out.println("invoke");
        });
        if(!asnc.isPresent()) {
            sync.ifPresent(methodSync -> {
                runOnFXThread.accept(null,methodSync);
            });
        }
        System.out.println("handle");
        return componentViewHandle.handle(message);
    }

}
