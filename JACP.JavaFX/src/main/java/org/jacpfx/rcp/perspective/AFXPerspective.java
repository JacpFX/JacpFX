/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AFXPerspective.java]
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

package org.jacpfx.rcp.perspective;


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.jacpfx.api.component.ComponentBase;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.coordinator.Coordinator;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.PerspectiveUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * represents a basic javafx perspective that handles subcomponents,
 * perspective are not handled in thread so avoid long running tasks in
 * perspective.
 *
 * @author Andy Moncsek
 */
public abstract class AFXPerspective implements
        Perspective<Node, EventHandler<Event>, Event, Object>, ComponentBase<EventHandler<Event>, Object>,
        Initializable {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final AtomicBoolean started = new AtomicBoolean(false);
    private String resourceBundleLocation = "";
    private JacpContext context;
    private TransferQueue<Message<Event, Object>> globalMessageQueue;
    private ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
    private BlockingQueue<DelegateDTO<Event, Object>> messageDelegateQueue;
    private Coordinator<EventHandler<Event>, Event, Object> messageCoordinator;
    private String viewLocation;
    private URL documentURL;
    private PerspectiveLayoutInterface<Node, Node> perspectiveLayout;
    private UIType type = UIType.PROGRAMMATIC;
    private String localeID = "";
    private final Object lock = new Object();
    private Launcher<?> launcher;

    protected Injectable perspective;

    private AtomicInteger pos =new AtomicInteger(-1);
    private AtomicInteger of = new AtomicInteger(0);


    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isStarted() {
        return this.started.get();
    }

    @Override
    public final void setStarted(boolean started) {
        this.started.set(started);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLocaleID() {
        return localeID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setLocaleID(String localeID) {
        this.localeID = localeID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getResourceBundleLocation() {
        return resourceBundleLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setResourceBundleLocation(String resourceBundleLocation) {
        this.resourceBundleLocation = resourceBundleLocation;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(
            final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
            final BlockingQueue<DelegateDTO<Event, Object>> messageDelegateQueue,
            final Coordinator<EventHandler<Event>, Event, Object> messageCoordinator, final Launcher<?> launcher) {

        this.messageCoordinator = messageCoordinator;
        this.componentDelegateQueue = componentDelegateQueue;
        this.messageDelegateQueue = messageDelegateQueue;
        this.globalMessageQueue = getMessageQueue();
        this.launcher = launcher;
        this.context = new JacpContextImpl(this.globalMessageQueue);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final void postInit(
            final ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler) {
        // init component handler
        this.componentHandler = componentHandler;
        this.messageCoordinator.setComponentHandler(this.componentHandler);
        final List<SubComponent<EventHandler<Event>, Event, Object>> subcomponents = createAllDeclaredSubcomponents();
        if (subcomponents != null) this.registerSubcomponents(subcomponents);
    }

    /**
     * Create an returns all declared subcomponents by Perspective annotation.
     *
     * @return all declared subcomponents
     */
    private List<SubComponent<EventHandler<Event>, Event, Object>> createAllDeclaredSubcomponents() {
        final Injectable handler = this.perspective;
        if (handler == null) throw new IllegalArgumentException("No perspective annotatation found");
        final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
        return PerspectiveUtil.getInstance(this.launcher).createSubcomponents(perspectiveAnnotation);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handlePerspective(final Message<Event, Object> action) {
        getFXPerspectiveHandler().handlePerspective(action,
                (PerspectiveLayout) this.perspectiveLayout);

    }

    @Override
    public final void registerComponent(
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        component.initEnv(this.context.getId(),
                getMessageQueue());
        final JacpContextImpl currentContext = JacpContextImpl.class.cast(component.getContext());
        PerspectiveUtil.handleComponentMetaAnnotation(component);
        currentContext.setFXComponentLayout(getFXComponentLayoutInstance(currentContext));
        if (currentContext.isActive()) {
            addComponent(component);
        }

    }

    private FXComponentLayout getFXComponentLayoutInstance(final Context currentContext) {
        final FXComponentLayout currentLayout = Context.class.cast(this.context).getComponentLayout();
        return new FXComponentLayout(currentLayout.getMenu(), currentLayout.getGlassPane(), currentContext.getParentId(), currentContext.getId());

    }

    @Override
    public final void addComponent(
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        synchronized (lock) {
            this.log("register component: " + component.getContext().getId());
            ComponentRegistry.registerComponent(component);
        }
    }


    @Override
    public final void initComponents(final Message<Event, Object> action) {
        final String targetId = FXUtil.getTargetComponentId(action
                .getTargetId());
        this.log("3.4.4.1: subcomponent targetId: " + targetId);
        final List<SubComponent<EventHandler<Event>, Event, Object>> components = getSubcomponents();
        if (components == null) return;
        components.parallelStream().forEach(component -> initComponent(component, action, targetId));
    }

    private void initComponent(final SubComponent<EventHandler<Event>, Event, Object> component, final Message<Event, Object> action, final String targetId) {
        if (component.getContext().getId().equals(targetId)) {
            this.log("3.4.4.2: subcomponent init with custom message");
            this.componentHandler.initComponent(action, component);
        } // else END
        else if (component.getContext().isActive() && !component.isStarted()) {
            this.log("3.4.4.2: subcomponent init with default message");
            this.componentHandler.initComponent(
                    new MessageImpl(component.getContext().getId(), component.getContext().getId(),
                            "init", null), component);
        } // if END
    }

    private void log(final String message) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.finest(">> " + message);
        }
    }

    /**
     * Register component at componentHandler.
     *
     * @param <M>         , a list of component extending ISubComponents
     * @param components, a list of registered component
     */
    private <M extends SubComponent<EventHandler<Event>, Event, Object>> void registerSubcomponents(
            final List<M> components) {
        components.forEach(this::registerComponent);
    }

    @Override
    public final List<SubComponent<EventHandler<Event>, Event, Object>> getSubcomponents() {
        return ComponentRegistry.findComponentsByParentId(this.context.getId());
    }

    @Override
    public final PerspectiveLayoutInterface<Node, Node> getIPerspectiveLayout() {
        return this.perspectiveLayout;
    }

    @Override
    public final void setIPerspectiveLayout(final PerspectiveLayoutInterface<Node, Node> layout) {
        this.perspectiveLayout = layout;
    }

    @Override
    public final BlockingQueue<SubComponent<EventHandler<Event>, Event, Object>> getComponentDelegateQueue() {
        return this.componentDelegateQueue;
    }

    @Override
    public final BlockingQueue<DelegateDTO<Event, Object>> getMessageDelegateQueue() {
        return this.messageDelegateQueue;
    }

    @Override
    public final TransferQueue<Message<Event, Object>> getMessageQueue() {
        return this.messageCoordinator.getMessageQueue();

    }

    @Override
    public final ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> getComponentHandler() {
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
        InternalContext.class.cast(context).setResourceBundle(resourceBundle);
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
    public final void setUIType(UIType type) {
        this.type = type;
    }


    final FXPerspective getFXPerspectiveHandler() {
        return FXPerspective.class.cast(perspective);
    }

    @Override
    public final Injectable getPerspective() {
        return this.perspective;
    }

    @Override
    public final JacpContext getContext() {
        return this.context;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final int compareTo(ComponentBase o) {
        return this.context.getId().compareTo(o.getContext().getId());
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void updatePositions(int pos, int of){
        this.pos.set(pos);
        this.of.set(of);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public  boolean isLast(){
       return this.pos.get()==this.of.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AFXPerspective that = (AFXPerspective) o;

        if (started.get() != that.started.get()) return false;
        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        return !(globalMessageQueue != null ? !globalMessageQueue.equals(that.globalMessageQueue) : that.globalMessageQueue != null) && !(localeID != null ? !localeID.equals(that.localeID) : that.localeID != null) && !(resourceBundleLocation != null ? !resourceBundleLocation.equals(that.resourceBundleLocation) : that.resourceBundleLocation != null);

    }

    @Override
    public int hashCode() {
        int result = (started.get() ? 1 : 0);
        result = 31 * result + (localeID != null ? localeID.hashCode() : 0);
        result = 31 * result + (resourceBundleLocation != null ? resourceBundleLocation.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (globalMessageQueue != null ? globalMessageQueue.hashCode() : 0);
        return result;
    }
}
