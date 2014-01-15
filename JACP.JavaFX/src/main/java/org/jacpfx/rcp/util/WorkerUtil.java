package org.jacpfx.rcp.util;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.UIComponent;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.component.AFXComponent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 11.10.13
 * Time: 14:22
 * This util class contains methods needed in all types  of workers
 */
public class WorkerUtil {

    /**
     * invokes a runnable on application thread and waits until execution is
     * finished
     *
     * @param runnable, a runnable which will be invoked and wait until execution is finished
     * @throws InterruptedException,java.util.concurrent.ExecutionException
     *
     */
    public static void invokeOnFXThreadAndWait(final Runnable runnable)
            throws InterruptedException, ExecutionException {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final AtomicBoolean conditionReady = new AtomicBoolean(false);
        final ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        lock.lock();
        try {
            Platform.runLater(() -> {
                lock.lock();
                try {
                    // prevent execution when application is closed
                    if (ShutdownThreadsHandler.APPLICATION_RUNNING.get())
                        runnable.run();
                } catch (Throwable t) {
                    throwableWrapper.t = t;
                } finally {
                    conditionReady.set(true);
                    condition.signal();
                    lock.unlock();
                }

            });
            // wait until execution is finished and check if application is
            // still running to prevent wait
            while (!conditionReady.get()
                    && ShutdownThreadsHandler.APPLICATION_RUNNING.get())
                condition.await(ShutdownThreadsHandler.WAIT,
                        TimeUnit.MILLISECONDS);
            if (throwableWrapper.t != null) {
                throw new ExecutionException(throwableWrapper.t);
            }
        } finally {
            lock.unlock();
        }
    }



    /**
     * find valid target and add type specific new component. Handles Container,
     * ScrollPanes, Menus and Bar Entries from user
     *
     * @param validContainer, a valid container where components root will be added
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
     * @param validContainer , a valid container where components root will be added
     * @param componentViewNode   , the component
     */
    private static void handleAdd(final Node validContainer, final Node componentViewNode) {
        if (componentViewNode != null) {
            handleViewState(componentViewNode, true);
            final ObservableList<Node> children = FXUtil
                    .getChildren(validContainer);
            if(!children.contains(componentViewNode))children.add(componentViewNode);
        }

    }

    /**
     * set visibility and enable/disable
     *
     * @param componentViewNode, a Node where to set the state
     * @param state,        the boolean value of the state
     */
    public static void handleViewState(final Node componentViewNode,
                                       final boolean state) {
        componentViewNode.setVisible(state);
        componentViewNode.setDisable(!state);
        componentViewNode.setManaged(state);
    }

    /**
     * delegate components handle return value to specified target
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
            comp.getContext().send(targetId,value);
        }
    }

    /**
     * Executes post handle method in application main thread. The result value
     * of handle method (from worker thread) is Input for the postHandle Method.
     * The return value or the handleReturnValue are the root node of this
     * component.
     *
     * @param component, a component
     * @param action,    the current message
     */
    public static void executeComponentViewPostHandle(final Node handleReturnValue,
                                                            final AFXComponent component, final Message<Event, Object> action) throws Exception {

        Node potsHandleReturnValue = component.getComponentViewHandle().postHandle(handleReturnValue,
                action);
        if (potsHandleReturnValue == null) {
            potsHandleReturnValue = handleReturnValue;
        } else if (component.getType().equals(UIType.DECLARATIVE)) {
            throw new UnsupportedOperationException(
                    "declarative components should not have a return value in postHandle method, otherwise you would overwrite the FXML root node.");
        }
        if (potsHandleReturnValue != null
                && component.getType().equals(UIType.PROGRAMMATIC)) {
            component.setRoot(potsHandleReturnValue);
        }
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
        try {
            delegateQueue.put(component);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }
    }

    /**
     * Runs the handle method of a componentView.
     *
     * @param component, the component
     * @param action,    the current message
     * @return a returned node from component execution
     */
    public static Node prepareAndRunHandleMethod(
            final UIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Message<Event, Object> action) throws Exception {
        return component.getComponentViewHandle().handle(action);

    }

}
