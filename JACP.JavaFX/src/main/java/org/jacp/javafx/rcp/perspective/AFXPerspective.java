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
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.api.component.IPerspectiveView;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.Injectable;
import org.jacp.api.componentLayout.IPerspectiveLayout;
import org.jacp.api.context.Context;
import org.jacp.api.coordinator.IComponentCoordinator;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.component.AComponent;
import org.jacp.javafx.rcp.componentLayout.PerspectiveLayout;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.coordinator.FXComponentCoordinator;
import org.jacp.javafx.rcp.registry.ComponentRegistry;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.PerspectiveUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    /**
     * {@inheritDoc}
     */
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
        this.subcomponents = createAllDeclaredSubcomponents();
        if (this.getSubcomponents() != null) this.registerSubcomponents(this.getSubcomponents());
    }

    /**
     * Create an returns all declared subcomponents by Perspective annotation.
     * @return all declared subcomponents
     */
   private List<ISubComponent<EventHandler<Event>, Event, Object>> createAllDeclaredSubcomponents() {
       final Injectable handler =  this.getPerspectiveHandle();
       if(handler==null) throw new IllegalArgumentException("No perspective annotatation found");
       final Perspective perspectiveAnnotation = handler.getClass()
               .getAnnotation(Perspective.class);
       return PerspectiveUtil.getInstance(this.launcher).createSubcomponents(perspectiveAnnotation);

   }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePerspective(final IAction<Event, Object> action) {
        getFXPerspectiveHandler().handlePerspective(action,
                (PerspectiveLayout) this.getIPerspectiveLayout());

    }

    //TODO extract metadata initialisation to perform this on parallel stream!!!
    @Override
    public final void registerComponent(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        synchronized (lock) {
            component.initEnv(this.getContext().getId(),
                    this.componentCoordinator.getMessageQueue());
            final JACPContextImpl context =  JACPContextImpl.class.cast(component.getContext());
            context.setParentId(this.getContext().getId());
            context.setFXComponentLayout(JACPContextImpl.class.cast(this.getContext()).getComponentLayout());
            PerspectiveUtil.handleComponentMetaAnnotation(component);
            this.log("register component: " + component.getContext().getId());
            ComponentRegistry.registerComponent(component);
            if (!this.getSubcomponents().contains(component)) {
                this.getSubcomponents().add(component);
            }
        }

    }


    @Override
    public final void unregisterComponent(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        synchronized (lock) {
            this.log("unregister component: " + component.getContext().getId());
            component.initEnv(null, null);
            ComponentRegistry.removeComponent(component);
            if (this.getSubcomponents().contains(component)) {
                this.getSubcomponents().remove(component);
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
                this.getComponentHandler().initComponent(action, component);
            } // else END
            else if (component.getContext().isActive() && !component.isStarted()) {
                this.log("3.4.4.2: subcomponent init with default action");
                this.getComponentHandler().initComponent(
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
    public final IPerspectiveLayout<Node, Node> getIPerspectiveLayout() {
        return this.perspectiveLayout;
    }

    @Override
    public final void setIPerspectiveLayout(final IPerspectiveLayout<Node, Node> layout){
        this.perspectiveLayout = layout;
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
    public void setUIType(UIType type){
        this.type = type;
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
