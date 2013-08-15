/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [AFX2ComponentWorker.java]
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
package org.jacp.javafx.rcp.worker;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;
import org.jacp.api.annotations.PostConstruct;
import org.jacp.api.annotations.PreDestroy;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.IUIComponent;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.util.Checkable;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * handles component methods in own thread;
 * 
 * @author Andy Moncsek
 */
public abstract class AFXComponentWorker<T> extends Task<T> {

	private final String componentName;

	public AFXComponentWorker(final String componentName) {
		this.componentName = componentName;
	}

	/**
	 * find valid target component in perspective
	 * 
	 * @param targetComponents
	 * @param id
	 * @return
	 */
	final Node getValidContainerById(
            final Map<String, Node> targetComponents, final String id) {
		return targetComponents.get(id);
	}

	/**
	 * find valid target and add type specific new component. Handles Container,
	 * ScrollPanes, Menus and Bar Entries from user
	 * 
	 * @param validContainer
	 * @param component
	 */
	final void addComponentByType(
			final Node validContainer,
			final IUIComponent<Node, EventHandler<Event>, Event, Object> component) {
		this.handleAdd(validContainer, component.getRoot(), component.getName());
		this.handleViewState(validContainer, true);

	}

	/**
	 * enables component an add to container
	 * 
	 * @param validContainer
	 * @param IUIComponent
	 * @param name
	 */
	private void handleAdd(final Node validContainer, final Node IUIComponent,
			final String name) {
		if (validContainer != null && IUIComponent != null) {
			this.handleViewState(IUIComponent, true);
			final ObservableList<Node> children = FXUtil
					.getChildren(validContainer);
			children.add(IUIComponent);
		}

	}

	/**
	 * removes old ui component of subcomponent form parent ui component
	 * 
	 * @param parent
	 * @param currentContainer
	 */
	final void handleOldComponentRemove(final Node parent,
			final Node currentContainer) {
		this.handleViewState(currentContainer, false);
		final ObservableList<Node> children = FXUtil.getChildren(parent);
		children.remove(currentContainer);
	}

	/**
	 * set visibility and enable/disable
	 * 
	 * @param IUIComponent
	 * @param state
	 */
	final void handleViewState(final Node IUIComponent,
			final boolean state) {
		IUIComponent.setVisible(state);
		IUIComponent.setDisable(!state);
        IUIComponent.setManaged(state);
	}

	/**
	 * delegate components handle return value to specified target
	 * 
	 * @param comp
	 * @param targetId
	 * @param value
	 */
    void delegateReturnValue(
            final ISubComponent<EventHandler<Event>, Event, Object> comp,
            final String targetId, final Object value,
            final IAction<Event, Object> myAction) {
		if (value != null && targetId != null
				&& !myAction.getMessage().equals("init")) {
			final IActionListener<EventHandler<Event>, Event, Object> listener = comp
					.getActionListener(null);
			listener.notifyComponents(new FXAction(comp.getId(), targetId,
					value, null));
		}
	}

	/**
	 * set new ui component to parent ui component, be careful! call this method
	 * only in EDT... never run from separate thread
	 * 
	 * @param component
	 * @param parent
	 * @param currentTaget
	 */
    void handleNewComponentValue(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Map<String, Node> targetComponents, final Node parent,
            final String currentTaget) {
		if (parent == null) {
			final String validId = this.getValidTargetId(currentTaget,
					component.getExecutionTarget());
			this.handleTargetChange(delegateQueue, component, targetComponents,
					validId);
		} else if (currentTaget.equals(component.getExecutionTarget())) {
			this.addComponentByType(parent, component);
		} else {
			final String validId = this.getValidTargetId(currentTaget,
					component.getExecutionTarget());
			this.handleTargetChange(delegateQueue, component, targetComponents,
					validId);
		}
	}

	/**
	 * currentTarget.length < 2 Happens when component changed target from one
	 * perspective to an other
	 * 
	 * @param currentTaget
	 * @param futureTarget
	 * @return
	 */
    String getValidTargetId(final String currentTaget,
                            final String futureTarget) {
		return currentTaget.length() < 2 ? FXUtil
				.getTargetComponentId(futureTarget) : futureTarget;
	}

	/**
	 * Handle component when target has changed
	 * 
	 * @param component
	 * @param targetComponents
	 */
    void handleTargetChange(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Map<String, Node> targetComponents, final String target) {
		final Node validContainer = this.getValidContainerById(
				targetComponents, target);
		if (validContainer != null) {
			this.handleLocalTargetChange(component, targetComponents,
					validContainer);
		} else {
			// handle target outside current perspective
			this.changeComponentTarget(delegateQueue, component);
		}
	}

	/**
	 * Handle target change inside perspective.
	 * 
	 * @param component
	 * @param targetComponents
	 * @param validContainer
	 */
    void handleLocalTargetChange(
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Map<String, Node> targetComponents, final Node validContainer) {
		this.addComponentByType(validContainer, component);
	}

	/**
	 * Handle target change to an other perspective. If target component not
	 * found in current perspective, move to an other perspective and run
	 * teardown.
	 * 
	 * @param delegateQueue
	 * @param component
	 * @param layout
	 */
    void handlePerspectiveChange(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final FXComponentLayout layout) {
		if (component instanceof AFXComponent) {
			FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, component,
					layout);
		}
		// handle target outside current perspective
		this.changeComponentTarget(delegateQueue, component);
	}

	/**
	 * Move component to new target in perspective.
	 * 
	 * @param component
	 */
	final void changeComponentTarget(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
		final String targetId = component.getExecutionTarget();
		final String parentIdOld = component.getParentId();
		final String parentId = FXUtil.getTargetParentId(targetId);
		if (!parentIdOld.equals(parentId)) {
			// delegate to perspective observer
			delegateQueue.add(component);

		}
	}

	/**
	 * Runs the handle method of a componentView.
	 * 
	 * @param component
	 * @param action
	 * @return
	 */
	final Node prepareAndRunHandleMethod(
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final IAction<Event, Object> action) throws Exception {
		return component.getComponentViewHandle().handle(action);

	}

	/**
	 * Executes post handle method in application main thread. The result value
	 * of handle method (from worker thread) is Input for the postHandle Method.
	 * The return value or the handleReturnValue are the root node of this
	 * component.
	 * 
	 * @param component
	 * @param action
	 */
    void executeComponentViewPostHandle(final Node handleReturnValue,
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

	/**
	 * checks if component started, if so run PostConstruct annotations
	 * 
	 * @param component
	 */
    void runCallbackOnStartMethods(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
		if (!component.isStarted())   {
            initLocalization(component);
            handleContextInjection(component);
            FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponentHandle());
        }

	}

    /**
     * Set Resource Bundle
     * @param component
     */
    private void initLocalization(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final String bundleLocation = component.getResourceBundleLocation();
        if (bundleLocation.equals(""))
            return;
        final String localeID = component.getLocaleID();
        component.setResourceBundle(ResourceBundle.getBundle(bundleLocation,
                FXUtil.getCorrectLocale(localeID)));

    }

    private void handleContextInjection(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IComponentHandle<?, EventHandler<Event>, Event, Object> handler = component.getComponentHandle();
        FXUtil.performResourceInjection(handler, component.getContext());
    }


    /**
	 * Check if component was not started yet an activate it.
	 * 
	 * @param component
	 */
    void runCallbackPostExecution(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
		if (!component.isStarted())
			FXUtil.setPrivateMemberValue(Checkable.class, component,
					FXUtil.ACOMPONENT_STARTED, true);
	}

	/**
	 * checks if component was deactivated, if so run OnTeardown annotations.
	 * 
	 * @param component
	 */
    void runCallbackOnTeardownMethods(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {

		// turn off component
		if (!component.isActive()) {
			FXUtil.setPrivateMemberValue(Checkable.class, component,
					FXUtil.ACOMPONENT_STARTED, false);
			// run teardown
			FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, component.getComponentHandle());
		}
	}

	void log(final String message) {
		if (Logger.getLogger(AFXComponentWorker.class.getName()).isLoggable(
				Level.FINE)) {
			Logger.getLogger(AFXComponentWorker.class.getName()).fine(
					">> " + message);
		}
	}

	/**
	 * invokes a runnable on application thread and waits until execution is
	 * finished
	 * 
	 * @param runnable
	 * @throws InterruptedException
	 */
    void invokeOnFXThreadAndWait(final Runnable runnable)
			throws InterruptedException {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();
		final AtomicBoolean conditionReady = new AtomicBoolean(false);
		lock.lock();
		try {
			Platform.runLater(() -> {
                lock.lock();
                try {

                    // prevent execution when application is closed
                    if (ShutdownThreadsHandler.APPLICATION_RUNNING.get())
                        runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
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
		} finally {
			lock.unlock();
		}
	}

	public String getComponentName() {
		return componentName;
	}

}
