package org.jacp.javafx.rcp.util;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.lifecycle.PreDestroy;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.component.AComponent;
import org.jacp.javafx.rcp.component.AFXComponent;

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
     */
    public static final void invokeOnFXThreadAndWait(final Runnable runnable)
            throws InterruptedException,ExecutionException {
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
     * checks if component was deactivated, if so run OnTeardown annotations.
     *
     * @param component, the component
     */
    public static final void runCallbackOnTeardownMethods(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {

        // turn off component
        if (!component.getContext().isActive()) {
            FXUtil.setPrivateMemberValue(AComponent.class, component,
                    FXUtil.ACOMPONENT_STARTED, false);
            // run teardown
            FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, component.getComponentHandle());
        }
    }

    /**
     * Executes post handle method in application main thread. The result value
     * of handle method (from worker thread) is Input for the postHandle Method.
     * The return value or the handleReturnValue are the root node of this
     * component.
     *
     * @param component, a component
     * @param action, the current action
     */
    public static final void executeComponentViewPostHandle(final Node handleReturnValue,
                                        final AFXComponent component, final IAction<Event, Object> action) throws Exception {

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
            potsHandleReturnValue.setVisible(true);
            component.setRoot(potsHandleReturnValue);
        }
    }

}
