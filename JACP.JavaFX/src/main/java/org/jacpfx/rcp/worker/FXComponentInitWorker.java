/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [FX2ComponentInitWorker.java]
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
package org.jacpfx.rcp.worker;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.exceptions.AnnotationMissconfigurationException;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.component.AFXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.util.WorkerUtil;

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
public class FXComponentInitWorker extends AComponentWorker<AFXComponent> {

    private final Map<String, Node> targetComponents;
    private final AFXComponent component;
    private final Message<Event, Object> message;
    private final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;

    /**
     * The workers constructor.
     *
     * @param targetComponents ; a map with all targets provided by perspective
     * @param component        ; the UI component to init
     * @param message           ; the init message
     * @param componentDelegateQueue ; the delegate queue for components that should be moved to an other perspective
     */
    public FXComponentInitWorker(final Map<String, Node> targetComponents,
                                 final AFXComponent component,
                                 final Message<Event, Object> message,
                                 final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue) {
        this.targetComponents = targetComponents;
        this.component = component;
        this.message = message;
        this.componentDelegateQueue = componentDelegateQueue;
    }

    /**
     * Run all methods that need to be invoked before worker thread start to
     * run. Programmatic components runs PostConstruct; declarative components init
     * the FXML and set the value to root node.
     *
     * @throws InterruptedException , exception when thread was interrupted
     */
    private void runPreInitMethods() throws InterruptedException, ExecutionException {
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            setComponentToActiveAndStarted(component);
            final FXComponentLayout layout = JacpContextImpl.class.cast(component.getContext()).getComponentLayout();
            switch (component.getType()) {
                case DECLARATIVE:
                    runPreInitOnDeclarativeComponent(component, layout);
                    break;
                default:
                    initLocalization(null, component);
                    performContextInjection(component);
                    runComponentOnStartupSequence(component, layout,
                            component.getContext().getResourceBundle());
            }
        });
    }

    private void runPreInitOnDeclarativeComponent(final AFXComponent component, final FXComponentLayout layout) {
        final URL url = getClass().getResource(
                component.getViewLocation());
        initLocalization(url, component);
        component.setRoot(FXUtil.loadFXMLandSetController(component.getComponent(), component.getContext().getResourceBundle(), url));
        performContextInjection(component);
        runComponentOnStartupSequence(component, layout,
                component.getDocumentURL(),
                component.getContext().getResourceBundle());
    }

    /**
     * Run at startup method in perspective.
     *
     * @param component, the component
     * @param param,     all parameters
     */
    private void runComponentOnStartupSequence(final AFXComponent component,
                                               final Object... param) {
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, component.getComponent(), param);
    }

    private void setComponentToActiveAndStarted(final AFXComponent component) {
        component.getContext().setActive(true);
        component.setStarted(true);
    }

    /**
     * Inject JacpContextImpl object.
     *
     * @param component, the component where to inject the context
     */
    private void performContextInjection(final AFXComponent component) {
        ComponentHandle<?, Event, Object> handler = component.getComponent();
        FXUtil.performResourceInjection(handler, component.getContext());
    }

    @Override
    protected AFXComponent call() throws Exception {
        this.component.lock();
        checkValidComponent(this.component);
        runPreInitMethods();
        final String name = this.component.getContext().getName();
        this.log("3.4.4.2.1: subcomponent handle init START: "
                + name);
        final Node handleReturnValue = WorkerUtil.prepareAndRunHandleMethod(
                this.component, this.message);
        this.executePostHandleAndAddComponent(handleReturnValue,
                this.component, this.message, this.targetComponents);
        // check if component was shutdown
        if (!checkIfStartedAndValid(component)) return this.component;
        this.component.initWorker(new EmbeddedFXComponentWorker(this.targetComponents, this.componentDelegateQueue, this.component));
        return this.component;
    }

    private boolean checkIfStartedAndValid(final AFXComponent componentToCheck) {
       return componentToCheck.isStarted();
    }

    /**
     * Set Resource Bundle
     *
     * @param url,       the FXML url
     * @param component, the component
     */
    private void initLocalization(final URL url, final AFXComponent component) {
        final String bundleLocation = component.getResourceBundleLocation();
        if (bundleLocation.isEmpty())
            return;
        final String localeID = component.getLocaleID();
        component.initialize(url, ResourceBundle.getBundle(bundleLocation,
                FXUtil.getCorrectLocale(localeID)));

    }



    /**
     * Handles "component add" in EDT must be called outside EDT.
     *
     * @param targetComponents , possible targets in perspective
     * @param myComponent      , the ui component
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    private void executePostHandleAndAddComponent(
            final Node handleReturnValue, final AFXComponent myComponent,
            final Message<Event, Object> myAction, final Map<String, Node> targetComponents) throws Exception {
        final Thread t = Thread.currentThread();
        WorkerUtil.invokeOnFXThreadAndWait(() -> {
            try {
                WorkerUtil.executeComponentViewPostHandle(
                        handleReturnValue, myComponent, myAction);
            } catch (Exception e) {
                t.getUncaughtExceptionHandler().uncaughtException(t, e);
            }
            if (component.getContext().isActive()) {
                final String targetLayout = JacpContextImpl.class.cast(this.component.getContext()).getTargetLayout();
                final Node validContainer = this.getValidContainerById(targetComponents, targetLayout);
                if (validContainer == null && myComponent.getRoot() != null)
                    throw new AnnotationMissconfigurationException("no targetLayout for layoutID: " + targetLayout + " found");
                if (validContainer == null || myComponent.getRoot() == null) {
                    return;
                }
                WorkerUtil.addComponentByType(validContainer,
                        myComponent);
            } else {
                shutDownComponent(component);
            }
        });
    }

    private void shutDownComponent(final AFXComponent component) {
        // unregister component
        final String parentId = component.getParentId();
        final Perspective<EventHandler<Event>, Event, Object> parentPerspctive = PerspectiveRegistry.findPerspectiveById(parentId);
        if (parentPerspctive != null) parentPerspctive.unregisterComponent(component);
        TearDownHandler.shutDownFXComponent(component);
        component.setStarted(false);
    }

    @Override
    public final void done() {
        final Thread t = Thread.currentThread();
        try {
            this.get();
        } catch (final Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        } finally {
            this.component.release();
        }


    }

}
