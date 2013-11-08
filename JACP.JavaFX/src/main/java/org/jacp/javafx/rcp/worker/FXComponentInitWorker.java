/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [FX2ComponentInitWorker.java]
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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.lifecycle.PostConstruct;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.exceptions.AnnotationMissconfigurationException;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.component.AComponent;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.WorkerUtil;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * Background Worker to execute components; handle method to init component.
 *
 * @author Andy Moncsek
 */
public class FXComponentInitWorker extends AFXComponentWorker<AFXComponent> {

	private final Map<String, Node> targetComponents;
	private final AFXComponent component;
	private final IAction<Event, Object> action;
    private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;

	/**
	 * The workers constructor.
	 *
	 * @param targetComponents
	 *            ; a map with all targets provided by perspective
	 * @param component
	 *            ; the UI component to init
	 * @param action
	 *            ; the init action
	 */
	public FXComponentInitWorker(final Map<String, Node> targetComponents,
			final AFXComponent component, final IAction<Event, Object> action,final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue) {
		this.targetComponents = targetComponents;
		this.component = component;
		this.action = action;
        this.componentDelegateQueue = componentDelegateQueue;
	}

	/**
	 * Run all methods that need to be invoked before worker thread start to
	 * run. Programmatic components runs PostConstruct; declarative components init
	 * the FXML and set the value to root node.
	 *
	 * @throws InterruptedException
	 */
	private void runPreInitMethods() throws InterruptedException, ExecutionException {
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            final FXComponentLayout layout = JACPContextImpl.class.cast(component.getContext()).getComponentLayout();
            if (component.getType().equals(UIType.DECLARATIVE)) {
                final URL url = getClass().getResource(
                        component.getViewLocation());
                initLocalization(url, component);
                component.setRoot(FXUtil.loadFXMLandSetController(component.getComponent(), component.getContext().getResourceBundle(), url));
                performContextInjection(component);
                runComponentOnStartupSequence(component, layout,
                        component.getDocumentURL(),
                        component.getContext().getResourceBundle());
                return;

            }
            initLocalization(null, component);
            performContextInjection(component);
            runComponentOnStartupSequence(component, layout,
                    component.getContext().getResourceBundle());

        });
	}

    /**
     * Inject Context object.
     * @param component, the component where to inject the context
     */
    private void performContextInjection(AFXComponent component) {
        IComponentHandle<?, EventHandler<Event>, Event, Object> handler = component.getComponent();
        FXUtil.performResourceInjection(handler,component.getContext());
    }



	@Override
	protected AFXComponent call() throws Exception {
			this.component.lock();
			runPreInitMethods();
			try {
                final String name = this.component.getContext().getName();
				this.log("3.4.4.2.1: subcomponent handle init START: "
						+ name);
				final Node handleReturnValue = WorkerUtil.prepareAndRunHandleMethod(
						this.component, this.action);
				this.log("3.4.4.2.2: subcomponent handle init get valid container: "
						+ name);
				this.log("3.4.4.2.3: subcomponent handle init add component by type: "
						+ name);
				this.addComponent(handleReturnValue,
						this.component, this.action,this.targetComponents);
				this.log("3.4.4.2.4: subcomponent handle init END: "
						+ name);
                this.component.initWorker(new EmbeddedFXComponentWorker(this.targetComponents,this.componentDelegateQueue,this.component));
                FXUtil.setPrivateMemberValue(AComponent.class, this.component,
                        FXUtil.ACOMPONENT_STARTED, true);
			} finally {

				this.component.release();
			}

			return this.component;
	}

    /**
     * Set Resource Bundle
     * @param url, the FXML url
     * @param component, the component
     */
	private void initLocalization(final URL url, final AFXComponent component) {
		final String bundleLocation = component.getResourceBundleLocation();
		if (bundleLocation.equals(""))
			return;
		final String localeID = component.getLocaleID();
		component.initialize(url, ResourceBundle.getBundle(bundleLocation,
				FXUtil.getCorrectLocale(localeID)));

	}

	/**
	 * Run at startup method in perspective.
	 *
	 * @param component, the component
     * @param param, all parameters
	 */
	private void runComponentOnStartupSequence(AFXComponent component,
			Object... param) {
		FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponent(), param);
	}



	/**
	 * Handles "component add" in EDT must be called outside EDT.
	 *
	 * @param targetComponents
	 *            , possible targets in perspective
	 * @param myComponent
	 *            , the ui component
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	private void addComponent(
			final Node handleReturnValue, final AFXComponent myComponent,
			final IAction<Event, Object> myAction,final Map<String, Node> targetComponents) throws Exception {
        final Thread t = Thread.currentThread();
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            try {
                WorkerUtil.executeComponentViewPostHandle(
                        handleReturnValue, myComponent, myAction);
            } catch (Exception e) {
                t.getUncaughtExceptionHandler().uncaughtException(t, e);
            }
            final String targetLayout = JACPContextImpl.class.cast(this.component.getContext()).getTargetLayout();
            final Node validContainer = this.getValidContainerById(targetComponents,targetLayout);
            if(validContainer==null && myComponent.getRoot()!=null) throw new AnnotationMissconfigurationException("no targetLayout for layoutID: "+targetLayout+" found");
            if (validContainer == null || myComponent.getRoot() == null) {
                return;
            }
            WorkerUtil.addComponentByType(validContainer,
                    myComponent);
        });
	}

	@Override
	public final void done() {
			try {
				this.get();
			} catch (final InterruptedException e) {
				this.log("Exception in CallbackComponent INIT Worker, Thread interrupted: "
						+ e.getMessage());
				// TODO add to error queue and restart thread if
				// messages in
				// queue
				e.printStackTrace();
			} catch (final ExecutionException e) {
				this.log("Exception in CallbackComponent INIT Worker, Thread Excecution Exception: "
						+ e.getMessage());
				// TODO add to error queue and restart thread if
				// messages in
				// queue
				e.printStackTrace();
			} catch (final UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (final Exception e) {
				this.log("Exception in CallbackComponent INIT Worker, Thread Exception: "
						+ e.getMessage());
				// TODO add to error queue and restart thread if
				// messages in
				// queue
				e.printStackTrace();
			} finally {
				FXUtil.setPrivateMemberValue(AComponent.class, this.component,
						FXUtil.ACOMPONENT_STARTED, true);
			}


	}

}
