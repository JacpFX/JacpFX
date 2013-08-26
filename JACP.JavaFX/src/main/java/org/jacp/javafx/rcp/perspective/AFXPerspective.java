/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [AFX2Perspective.java]
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

package org.jacp.javafx.rcp.perspective;


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IDelegateDTO;
import org.jacp.api.annotations.component.Component;
import org.jacp.api.annotations.component.Declarative;
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.api.annotations.component.Stateless;
import org.jacp.api.component.*;
import org.jacp.api.componentLayout.IPerspectiveLayout;
import org.jacp.api.context.Context;
import org.jacp.api.coordinator.IComponentCoordinator;
import org.jacp.api.dialog.Scope;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.component.*;
import org.jacp.javafx.rcp.componentLayout.PerspectiveLayout;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.coordinator.FXComponentCoordinator;
import org.jacp.javafx.rcp.util.ClassRegistry;
import org.jacp.javafx.rcp.util.ComponentRegistry;
import org.jacp.javafx.rcp.util.FXUtil;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * represents a basic javafx2 perspective that handles subcomponents,
 * perspectives are not handled in thread so avoid long running tasks in
 * perspectives.
 *
 * @author Andy Moncsek
 */
public abstract class AFXPerspective extends AComponent implements
        IPerspectiveView<Node, EventHandler<Event>, Event, Object>,
        Initializable {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private List<ISubComponent<EventHandler<Event>, Event, Object>> subcomponents;
    private IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> componentHandler;
    private BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
    private BlockingQueue<IDelegateDTO<Event, Object>> messageDelegateQueue;
    private IComponentCoordinator<EventHandler<Event>, Event, Object> componentCoordinator;
    private String viewLocation;
    private URL documentURL;
    private IPerspectiveLayout<Node, Node> perspectiveLayout;
    private UIType type = UIType.PROGRAMMATIC;
    private String localeID = "";
    private final Object lock = new Object();
    private Launcher<?> launcher;

    protected Injectable perspectiveHandler;

    @Override
    public final void init(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
            final BlockingQueue<IDelegateDTO<Event, Object>> messageDelegateQueue,
            final BlockingQueue<IAction<Event, Object>> globalMessageQueue,final Launcher<?> launcher) {
        this.componentDelegateQueue = componentDelegateQueue;
        this.messageDelegateQueue = messageDelegateQueue;
        this.globalMessageQueue = globalMessageQueue;
        this.launcher = launcher;
        this.context = new JACPContextImpl(this.globalMessageQueue);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void postInit(
            final IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> componentHandler) {
        // init component handler
        this.componentHandler = componentHandler;
        this.componentCoordinator = new FXComponentCoordinator();
        ((FXComponentCoordinator) this.componentCoordinator).start();
        this.componentCoordinator.setComponentHandler(this.componentHandler);
        this.componentCoordinator
                .setMessageDelegateQueue(this.messageDelegateQueue);
        this.componentCoordinator.setParentId(this.getContext().getId());
        initSubcomponentsAndHandlers();
        if (this.subcomponents != null) this.registerSubcomponents(this.subcomponents);
    }

    private String[] getComponentIds() {
        final Injectable handler =  this.getPerspectiveHandle();
        if(handler==null) throw new IllegalArgumentException("no perspective annotatation found");
        final Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(Perspective.class);
        if (perspectiveAnnotation != null) {
            return perspectiveAnnotation.components();
        }   else {
              throw new IllegalArgumentException("no perspective annotatation found");
        }
    }

    private List<Injectable> getInjectAbles() {
        final String[] ids = getComponentIds();
        final List<String> componentIds = Arrays.asList(ids);
        return componentIds.parallelStream().map(this::mapToInjectAble).collect(Collectors.toList());
    }

    private Injectable mapToInjectAble(final String id) {
        final Class componentClass = ClassRegistry.getComponentClassById(id);
        if(componentClass==null)throw new InvalidParameterException("Component with id: "+id+" not found");
        final Scope scope = getCorrectScopeOfComponent(componentClass);
        final Object component = launcher.registerAndGetBean(componentClass, id, scope);
        if(Injectable.class.isAssignableFrom(component.getClass())) {
            return Injectable.class.cast(component);
        } else {
            throw new InvalidParameterException("Only Injectable components are allowed");
        }
    }

    private Scope getCorrectScopeOfComponent(final Class componentClass) {
        Scope scope = Scope.SINGLETON;
        if(componentClass.isAnnotationPresent(Stateless.class)) {
            scope = Scope.PROTOTYPE;
        }
        return scope;
    }

    private void initSubcomponentsAndHandlers() {
        final List<? extends Injectable> handlers = getInjectAbles();
        if(handlers==null) return;
        this.subcomponents = handlers.parallelStream().map(this::mapToSubcomponent).collect(Collectors.toList());
    }

      private ISubComponent<EventHandler<Event>, Event, Object> mapToSubcomponent(Injectable handler) {
          if(IComponentView.class.isAssignableFrom(handler.getClass())) {
              return new EmbeddedFXComponent(IComponentView.class.cast(handler));
          } else if(IComponentHandle.class.isAssignableFrom(handler.getClass())) {
              if(handler.getClass().isAnnotationPresent(Stateless.class)){
                  // stateless components
                  return new EmbeddedStatelessCallbackComponent(IComponentHandle.class.cast(handler));
              }else {
                  return new EmbeddedStatefulComponent(IComponentHandle.class.cast(handler));
              }
          }else {
              throw new InvalidParameterException("no useable component interface found");
          }

      }


    @Override
    public void handlePerspective(final IAction<Event, Object> action) {
        getFXPerspectiveHandler().handlePerspective(action,
                (PerspectiveLayout) this.perspectiveLayout);

    }

    @Override
    public final void registerComponent(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        synchronized (lock) {
            component.initEnv(this.getContext().getId(),
                    this.componentCoordinator.getMessageQueue());
            JACPContextImpl.class.cast(component.getContext()).setParentId(this.getContext().getId());
            this.handleMetaAnnotation(component);
            this.log("register component: " + component.getContext().getId());
            ComponentRegistry.registerComponent(component);
            if (!this.subcomponents.contains(component)) {
                this.subcomponents.add(component);
            }
        }

    }

    /**
     * Set meta attributes defined in annotations.
     *
     * @param component ; the component containing metadata.
     */
    private void handleMetaAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IComponentHandle<?,EventHandler<Event>,Event,Object> handler = component.getComponentHandle();
        if(handler==null)return;

        final Declarative declarativeComponent = handler.getClass()
                .getAnnotation(Declarative.class);
        if (declarativeComponent != null && FXComponent.class.isAssignableFrom(handler.getClass())) {
            handleDeclarativeComponentAnnotation(component, declarativeComponent);
        }

        final Component componentAnnotation = handler.getClass().getAnnotation(Component.class);
        if(componentAnnotation==null)return;

        if(CallbackComponent.class.isAssignableFrom(handler.getClass())){
            handleCallbackAnnotation(component, componentAnnotation);
            this.log("register CallbackComponent with annotations : " + componentAnnotation.id());
            return;
        }

        if (FXComponent.class.isAssignableFrom(handler.getClass())) {
            handleComponentAnnotation(component, componentAnnotation);
            this.log("register component with annotations : " + componentAnnotation.id());
            return;
        }

    }

    private void handleDeclarativeComponentAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component, final Declarative declarativeComponent) {
        AFXComponent.class.cast(component).setViewLocation(declarativeComponent.viewLocation());
    }

    private void handleCallbackAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component, final Component callbackAnnotation) {
        handleBaseAttributes(component, callbackAnnotation.id(), callbackAnnotation.active(),
                callbackAnnotation.name());
    }

    private void handleComponentAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component, final Component componentAnnotation) {
        handleBaseAttributes(component, componentAnnotation.id(), componentAnnotation.active(),
                componentAnnotation.name());
        handleComponentAnnotation(componentAnnotation, (AFXComponent) component);
    }

    /**
     * set component members
     *
     * @param componentAnnotation
     * @param component
     */
    private void handleComponentAnnotation(final Component componentAnnotation, final AFXComponent component) {
        setExecutionTarget(component, componentAnnotation.targetLayout());
        setLocale(component, componentAnnotation.localeID());
        setRessourceBundleLocation(component, componentAnnotation.resourceBundleLocation());
        this.log("register component with annotations : " + componentAnnotation.id());
    }

    private void setRessourceBundleLocation(final AFXComponent component, String bundleLocation) {
        if (component.getResourceBundleLocation() != null)
            component.setResourceBundleLocation(bundleLocation);
    }

    private void setLocale(final AFXComponent component, String locale) {
        if (component.getLocaleID() != null)
            component.setLocaleID(locale);
    }


    private void setExecutionTarget(final AFXComponent component, String value) {
        final String targetLayout = JACPContextImpl.class.cast(component.getContext()).getTargetLayout();
        if (targetLayout==null)
            component.getContext().setTargetLayout(value);
    }

    /**
     * set base component members
     *
     * @param component
     * @param id
     * @param active
     * @param name
     */
    private void handleBaseAttributes(final ISubComponent<EventHandler<Event>, Event, Object> component, final String id, final boolean active,
                                      final String name) {
        if (id != null) JACPContextImpl.class.cast(component.getContext()).setId(id);
        component.getContext().setActive(active);
        if (name != null) JACPContextImpl.class.cast(component.getContext()).setName(name);
    }

    @Override
    public final void unregisterComponent(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        synchronized (lock) {
            this.log("unregister component: " + component.getContext().getId());
            component.initEnv(null, null);
            ComponentRegistry.removeComponent(component);
            if (this.subcomponents.contains(component)) {
                this.subcomponents.remove(component);
            }
        }
    }

    @Override
    public final void initComponents(final IAction<Event, Object> action) {
        final String targetId = FXUtil.getTargetComponentId(action
                .getTargetId());
        this.log("3.4.4.1: subcomponent targetId: " + targetId);
        final List<ISubComponent<EventHandler<Event>, Event, Object>> components = this
                .getSubcomponents();
        if (components == null) return;
        components.parallelStream().forEach(component -> {
            if (component.getContext().getId().equals(targetId)) {
                this.log("3.4.4.2: subcomponent init with custom action");
                this.componentHandler.initComponent(action, component);
            } // else END
            else if (component.getContext().isActive() && !component.isStarted()) {
                this.log("3.4.4.2: subcomponent init with default action");
                this.componentHandler.initComponent(
                        new FXAction(component.getContext().getId(), component.getContext().getId(),
                                "init", null), component);
            } // if END
        });
    }

    private void log(final String message) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine(">> " + message);
        }
    }

    /**
     * Register components at componentHandler.
     *
     * @param <M> , a list of components extending ISubComponents
     * @param components, a list of registered components
     */
    private <M extends ISubComponent<EventHandler<Event>, Event, Object>> void registerSubcomponents(
            final List<M> components) {
        components.forEach(this::registerComponent);
    }

    @Override
    public List<ISubComponent<EventHandler<Event>, Event, Object>> getSubcomponents() {
        return this.subcomponents;
    }

    @Override
    public final IPerspectiveLayout<? extends Node, Node> getIPerspectiveLayout() {
        return this.perspectiveLayout;
    }

    @Override
    public final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> getComponentDelegateQueue() {
        return this.componentDelegateQueue;
    }

    @Override
    public final BlockingQueue<IDelegateDTO<Event, Object>> getMessageDelegateQueue() {
        return this.messageDelegateQueue;
    }

    @Override
    public final BlockingQueue<IAction<Event, Object>> getComponentsMessageQueue() {
        return this.componentCoordinator.getMessageQueue();

    }

    @Override
    public final IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, IAction<Event, Object>> getComponentHandler() {
        return this.componentHandler;
    }

    @Override
    public final String getViewLocation() {
        if (type.equals(UIType.PROGRAMMATIC))
            throw new UnsupportedOperationException("Only supported when @Declarative annotation is used");
        return this.viewLocation;
    }

    @Override
    public final void setViewLocation(String documentURL) {
        this.viewLocation = documentURL;
        this.type = UIType.DECLARATIVE;
    }

    @Override
    public final void initialize(URL url, ResourceBundle resourceBundle) {
        this.documentURL = url;
        JACPContextImpl.class.cast(context).setResourceBundle(resourceBundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final URL getDocumentURL() {
        if (type.equals(UIType.PROGRAMMATIC))
            throw new UnsupportedOperationException("Only supported when @Declarative annotation is used");
        return documentURL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final UIType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLocaleID() {
        return localeID;
    }

    public final void setLocaleID(String localeID) {
        this.localeID = localeID;
    }


    public FXPerspective getFXPerspectiveHandler() {
        return FXPerspective.class.cast(getPerspectiveHandle());
    }

    @Override
    public Injectable getPerspectiveHandle(){
        return this.perspectiveHandler;
    }

    @Override
    public Context<EventHandler<Event>, Event, Object> getContext() {
        return this.context;
    }

}
