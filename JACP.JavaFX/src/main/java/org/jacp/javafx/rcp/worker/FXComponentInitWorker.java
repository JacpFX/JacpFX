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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.PostConstruct;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.util.Checkable;
import org.jacp.javafx.rcp.util.FXUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Background Worker to execute components; handle method to init component.
 * 
 * @author Andy Moncsek
 */
public class FXComponentInitWorker extends AFXComponentWorker<AFXComponent> {

	private final Map<String, Node> targetComponents;
	private final AFXComponent component;
	private final FXComponentLayout layout;
	private final IAction<Event, Object> action;

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
			final AFXComponent component, final IAction<Event, Object> action,
			final FXComponentLayout layout) {
		super(component.getName());
		this.targetComponents = targetComponents;
		this.component = component;
		this.action = action;
		this.layout = layout;
	}

	/**
	 * Run all methods that need to be invoked before worker thread start to
	 * run. Programmatic components runs PostConstruct; declarative components init
	 * the FXML and set the value to root node.
	 * 
	 * @throws InterruptedException
	 */
	private void runPreInitMethods() throws InterruptedException {
		this.invokeOnFXThreadAndWait(()->{
				if (component.getType().equals(UIType.DECLARATIVE)) {
					final URL url = getClass().getResource(
							component.getViewLocation());
					initLocalization(url, component);
                    component.setRoot(loadFXMLandSetController(component, url));
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
     * @param component
     */
    private void performContextInjection(AFXComponent component) {
        IComponentHandle<?, EventHandler<Event>, Event, Object> handler = component.getComponentHandle();
        FXUtil.performResourceInjection(handler,component.getContext());
    }



	@Override
	protected AFXComponent call() throws Exception {
		synchronized (this.component) {
			this.component.lock();
			runPreInitMethods();
			try {
				this.log("3.4.4.2.1: subcomponent handle init START: "
						+ this.component.getName());
				final Node handleReturnValue = this.prepareAndRunHandleMethod(
						this.component, this.action);
				this.log("3.4.4.2.2: subcomponent handle init get valid container: "
						+ this.component.getName());
				// expect always local target id
				this.component.setExecutionTarget(FXUtil
						.getTargetComponentId(this.component
								.getExecutionTarget()));
				final Node validContainer = this.getValidContainerById(
						this.targetComponents,
						this.component.getExecutionTarget());
				this.log("3.4.4.2.3: subcomponent handle init add component by type: "
						+ this.component.getName());
				this.addComponent(validContainer, handleReturnValue,
						this.component, this.action);
				this.log("3.4.4.2.4: subcomponent handle init END: "
						+ this.component.getName());
			} finally {
				this.component.release();
			}

			return this.component;
		}
	}

    /**
     * Set Resource Bundle
     * @param url
     * @param component
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
	 * @param component
	 */
	private void runComponentOnStartupSequence(AFXComponent component,
			Object... param) {
		FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponentHandle(), param);
	}

	/**
	 * Loads the FXML document provided by viewLocation-
	 * 
	 * @param fxmlComponent
	 *            a FXML aware component
	 * @return The components root Node.
	 */
	private Node loadFXMLandSetController(final AFXComponent fxmlComponent,
			final URL url) {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		if (fxmlComponent.getContext().getResourceBundle() != null) {
			fxmlLoader.setResources(fxmlComponent.getContext().getResourceBundle());
		}
		fxmlLoader.setLocation(url);
		fxmlLoader.setController(fxmlComponent.getComponentHandle());
		try {
			return (Node) fxmlLoader.load();
		} catch (IOException e) {
			throw new MissingResourceException(
					"fxml file not found --  place in resource folder and reference like this: viewLocation = \"/myUIFile.fxml\"",
					fxmlComponent.getViewLocation(), "");
		}
	}

	

	/**
	 * Handles "component add" in EDT must be called outside EDT.
	 * 
	 * @param validContainer
	 *            , a valid target where the component ui node is included
	 * @param myComponent
	 *            , the ui component
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	private void addComponent(final Node validContainer,
			final Node handleReturnValue, final AFXComponent myComponent,
			final IAction<Event, Object> myAction) throws Exception {
		invokeOnFXThreadAndWait(() -> {
            try {
                executeComponentViewPostHandle(
                        handleReturnValue, myComponent, myAction);
            } catch (Exception e) {
                e.printStackTrace(); // TODO pass exception
            }
            if (validContainer == null || myComponent.getRoot() == null) {
                return;
            }
            addComponentByType(validContainer,
                    myComponent);
        });
	}

	@Override
	public final void done() {
		synchronized (this.component) {
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
				FXUtil.setPrivateMemberValue(Checkable.class, this.component,
						FXUtil.ACOMPONENT_STARTED, true);
			}

		}
	}

}
