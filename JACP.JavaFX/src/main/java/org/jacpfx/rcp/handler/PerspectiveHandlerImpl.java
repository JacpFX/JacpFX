/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [PerspectiveHandlerImpl.java]
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
package org.jacpfx.rcp.handler;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jacpfx.api.annotations.lifecycle.OnHide;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.UIComponent;
import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.component.EmbeddedFXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.FXMLPerspectiveLayout;
import org.jacpfx.rcp.componentLayout.FXPerspectiveLayout;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.perspective.AFXPerspective;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.MessageLoggerService;
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.workbench.GlobalMediator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles initialization and update of perspective in a workbench.
 *
 * @author Andy Moncsek
 */
public class PerspectiveHandlerImpl implements
        org.jacpfx.api.handler.ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final WorkbenchLayout<Node> workbenchLayout;
    private final Launcher<?> launcher;
    private final Pane root;


    public PerspectiveHandlerImpl(final Launcher<?> launcher, final WorkbenchLayout<Node> workbenchLayout,
                                  final Pane root) {
        this.workbenchLayout = workbenchLayout;
        this.root = root;
        this.launcher = launcher;
    }

    private static void shutDownAndClearComponents(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        perspective.getSubcomponents().stream()
                .filter(component -> component.getContext() != null)
                .filter(c -> c.getContext().isActive())
                .forEach(PerspectiveHandlerImpl::shutDownComponent);
    }

    private static void shutDownComponent(final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (EmbeddedFXComponent.class.isAssignableFrom(component.getClass())) {
            TearDownHandler.shutDownFXComponent(EmbeddedFXComponent.class.cast(component), component.getContext().getParentId());
        } else {
            TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(component));
        }
    }

    /**
     * check if switch from active to inactive perspective, hide all buttons of the previous perspective
     *
     * @param previousePerspective
     */
    private static void updateToolbarButtons(final Perspective<Node, EventHandler<Event>, Event, Object> previousePerspective, boolean visible) {
        GlobalMediator.getInstance().handleToolBarButtons(previousePerspective, visible);
    }


    private static void bringRootToFront(final int index, final ObservableList<Node> children, final Node root) {
        if (index != 0) {
            children.remove(index);
            children.set(0, root);
        }
    }

    private static void addNewRoot(final ObservableList<Node> children, final Node root) {
        children.add(root);
    }

    private static void replaceRootNodes(final ObservableList<Node> children, final Node newComp) {
        children.setAll(newComp);
    }

    private static void onShow(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final FXComponentLayout layout = Context.class.cast(perspective.getContext()).getComponentLayout();
        FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, perspective.getPerspective(), perspective.getIPerspectiveLayout(), layout,
                perspective.getType().equals(UIType.DECLARATIVE) ? perspective.getDocumentURL() : null, perspective.getContext().getResourceBundle());
    }

    private static void postConstruct(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final FXComponentLayout layout, final AFXPerspective perspectiveImpl) {
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class,
                perspective.getPerspective(), perspectiveImpl.getIPerspectiveLayout(), layout, perspective.getContext().getResourceBundle());
    }

    /**
     * get perspective ui root container
     *
     * @param layout, The perspective layout
     * @return the root Node
     */
    private static Optional<Node> getLayoutComponentFromPerspectiveLayout(final PerspectiveLayoutInterface<? extends Node, Node> layout) {
        return Optional.ofNullable(layout != null ? layout.getRootComponent() : null);
    }

    @Override
    public final void handleAndReplaceComponent(final Message<Event, Object> action,
                                                final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout = perspective
                .getIPerspectiveLayout();
        // backup old component

        final JacpContext<EventHandler<Event>, Object> context = perspective.getContext();
        final String previousPerspectiveId = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(context.getId());
        preHandlePerspective(perspective, context, previousPerspectiveId);
        perspective.handlePerspective(action);
        getLayoutComponentFromPerspectiveLayout(perspectiveLayout).
                ifPresent(componentOld -> postHandlePerspective(perspective, perspectiveLayout, componentOld, context, previousPerspectiveId));

    }

    private void postHandlePerspective(Perspective<Node, EventHandler<Event>, Event, Object> perspective, PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, Node componentOld, JacpContext<EventHandler<Event>, Object> context, String previousPerspectiveId) {
        if (!context.isActive()) {
            handleInactivePerspective(perspective, perspectiveLayout, componentOld);
        } else {
            handleActivePerspective(perspective, perspectiveLayout, componentOld, previousPerspectiveId);
        }
    }

    private void preHandlePerspective(Perspective<Node, EventHandler<Event>, Event, Object> perspective, JacpContext<EventHandler<Event>, Object> context, String previousPerspectiveId) {
        if (!context.getId().equals(previousPerspectiveId)) {
            final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective = PerspectiveRegistry.findPerspectiveById(previousPerspectiveId);
            onHide(PerspectiveRegistry.findPerspectiveById(previousPerspectiveId));
            // hide all buttons of the previous perspective
            GlobalMediator.getInstance().handleToolBarButtons(previousPerspective, false);

            onShow(perspective);

        }
    }

    private void handleActivePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld, final String previousPerspectiveId) {
        if (componentOld != null) {
            this.handlePerspectiveReassignment(perspective, previousPerspectiveId, perspectiveLayout);
        } else {
            this.initPerspectiveUI(perspectiveLayout);
        }
    }

    private void handleInactivePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        // 3 possible variants
        // 1 only one Perspective which is deactivated:  remove...
        // 2 second active perspective available, current perspective is the one which is disabled: find the other perspective, handle OnShow, add not to workbench
        // 3 second perspective is available, other perspective is currently displayed: turn off the perspective
        final JacpContext<EventHandler<Event>, Object> context = perspective.getContext();
        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, perspective.getPerspective(), perspectiveLayout, Context.class.cast(context).getComponentLayout(), context.getResourceBundle());
        removePerspectiveNodeFromWorkbench(perspectiveLayout, componentOld);
        displayNextPossiblePerspective(perspective);
        shutDownAndClearComponents(perspective);
    }

    private void displayNextPossiblePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> current) {
        final Optional<Perspective<Node, EventHandler<Event>, Event, Object>> possiblePerspectiveToShow = PerspectiveRegistry.findNextActivePerspective(current);
        possiblePerspectiveToShow.ifPresent(perspective -> {
            final String possiblePerspectiveId = perspective.getContext().getId();
            final String previousPerspectiveId = current.getContext().getId();
            if (!possiblePerspectiveId.equals(previousPerspectiveId)) {
                PerspectiveRegistry.getAndSetCurrentVisiblePerspective(possiblePerspectiveId);
                final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayoutReplacementComponent = perspective
                        .getIPerspectiveLayout();
                // execute OnShow
                onShow(perspective);
                handlePerspectiveReassignment(perspective, previousPerspectiveId, perspectiveLayoutReplacementComponent);
            }
        });

    }

    private void removePerspectiveNodeFromWorkbench(final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        final Node nodeToRemove = componentOld != null ? componentOld : PerspectiveHandlerImpl.getLayoutComponentFromPerspectiveLayout(perspectiveLayout).get();
        FXUtil.getChildren(nodeToRemove).ifPresent(nodes -> {
            nodes.clear();
            root.getChildren().remove(nodes);
        });

    }

    @Override
    public final void initComponent(final Message<Event, Object> message,
                                    final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final Thread t = Thread.currentThread();
        try {
            this.log("3.4.3: perspective handle init");

            final JacpContext<EventHandler<Event>, Object> context = perspective.getContext();
            final String currentPerspectiveId = context.getId();
            final String previousePerspectiveId = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(currentPerspectiveId);
            final Perspective<Node, EventHandler<Event>, Event, Object> previousePerspective = previousePerspectiveId != null && !currentPerspectiveId.equals(previousePerspectiveId) ?
                    PerspectiveRegistry.findPerspectiveById(previousePerspectiveId != null ? previousePerspectiveId : "") : null;

            hidePreviousPerspective(previousePerspective);
            handlePerspectiveInitialization(perspective);
            handlePerspective(message, perspective);
            initPerspectiveUI(perspective.getIPerspectiveLayout());
            updateToolbarButtons(previousePerspective, false);
            this.log("3.4.4: perspective init subcomponents");
            perspective.initComponents(message);
            updateToolbarButtons(perspective, true);
        } catch (final Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }

    }

    private void hidePreviousPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> previousePerspective) {

        if (previousePerspective != null) {
            onHide(previousePerspective);
        }
    }

    /**
     * reassignment can only be done in FX main thread;
     */
    private void handlePerspectiveReassignment(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final String perspectiveIdBefore,
                                               final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout) {
        // FIXME the reassignment should be avoid when calls to previous perspective is done
        this.reassignChild(perspective, getLayoutComponentFromPerspectiveLayout(perspectiveLayout), perspectiveIdBefore);
        // set already active editors to new component
        this.reassignSubcomponents(perspective, perspectiveLayout);
    }

    /**
     * add all active subcomponents to current perspective
     *
     * @param perspective, The perspective where to reassign
     * @param layout,      The perspective layout
     */
    private void reassignSubcomponents(final Perspective<Node, EventHandler<Event>, Event, Object> perspective,
                                       final PerspectiveLayoutInterface<? extends Node, Node> layout) {
        Optional.ofNullable(perspective.getSubcomponents()).ifPresent(subcomponents -> subcomponents.stream().
                filter(s -> s instanceof EmbeddedFXComponent && s.getContext().isActive()).
                map(EmbeddedFXComponent.class::cast).
                forEach(subComponent -> addComponentByType(subComponent, layout)));
    }

    /**
     * find valid target and add type specific new ui component
     *
     * @param component, The ui component to add
     * @param layout,    The perspective layout
     */
    private void addComponentByType(final UIComponent<Node, EventHandler<Event>, Event, Object> component,
                                    final PerspectiveLayoutInterface<? extends Node, Node> layout) {
        final String targetLayout = InternalContext.class.cast(component.getContext()).getTargetLayout();
        final Node validContainer = layout.getTargetLayoutComponents().get(targetLayout);
        FXUtil.getChildren(validContainer).ifPresent(children -> Optional.ofNullable(component.getRoot()).ifPresent(currentRoot -> {
            int index = children.indexOf(currentRoot);
            if (index < 0) {
                addNewRoot(children, currentRoot);
            } else {
                bringRootToFront(index, children, currentRoot);
            }
        }));
    }

    /**
     * handle reassignment of component in perspective ui
     *
     * @param perspective, The current perspective
     * @param newComp,     The new component Node
     */
    private void reassignChild(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final Optional<Node> newComp, final String perspectiveIdBefore) {
        final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective = PerspectiveRegistry.findPerspectiveById(perspectiveIdBefore);
        if (!perspective.equals(previousPerspective)) {
            // show all buttons of the new perspective
            GlobalMediator.getInstance().handleToolBarButtons(perspective, true);
            newComp.ifPresent(comp -> {
                replaceRootNodes(root.getChildren(), comp);
                comp.setVisible(true);
            });

        } else {
            // show all buttons of the new perspective
            GlobalMediator.getInstance().handleToolBarButtons(perspective, true);
            newComp.ifPresent(comp -> comp.setVisible(true));

        }

    }


    /**
     * add perspective UI to workbench root component
     *
     * @param perspectiveLayout, The perspective layout
     */
    private void initPerspectiveUI(final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout) {
        this.log("3.4.6: perspective init SINGLE_PANE");
        getLayoutComponentFromPerspectiveLayout(perspectiveLayout).ifPresent(comp -> {
            comp.setVisible(true);
            final ObservableList<Node> children = root.getChildren();
            children.setAll(comp);
        });
    }

    private void handlePerspective(final Message<Event, Object> action,
                                   final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {

        // set perspective to active
        InternalContext.class.cast(perspective.getContext()).updateActiveState(true);
        if (perspective.isLast()) {
            // execute OnShow
            onShow(perspective);
        }
        MessageLoggerService.getInstance().receive(action);
        if (FXUtil.getTargetPerspectiveId(action.getTargetId()).equals(perspective.getContext().getId())) {
            this.log("3.4.3.1: perspective handle with custom message");
            perspective.handlePerspective(action);
        } else {
            this.log("3.4.3.1: perspective handle with default >>init<< message");
            perspective.handlePerspective(new MessageImpl(perspective.getContext().getId(), perspective.getContext().getId(), "init", null));
        }
    }

    private void handlePerspectiveInitialization(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final JacpContext<EventHandler<Event>, Object> context = perspective.getContext();
        final InternalContext internalContext = InternalContext.class.cast(context);
        final FXComponentLayout layout = initFXComponentLayout(internalContext, perspective.getContext().getId());
        handlePerspectiveLayout(perspective);
        FXUtil.performResourceInjection(perspective.getPerspective(), context);
        postConstruct(perspective, layout, AFXPerspective.class.cast(perspective));
        perspective.postInit(new ComponentHandlerImpl(this.launcher, perspective.getIPerspectiveLayout(), perspective
                .getComponentDelegateQueue()));


    }

    private void handlePerspectiveLayout(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {

        if (perspective.getType().equals(UIType.DECLARATIVE)) {
            initDeclarativePerspectiveLayout(perspective);
        } else {
            initDefaultPerspectiveLayout(perspective);
        }

    }

    private FXComponentLayout initFXComponentLayout(final InternalContext context, final String id) {
        final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout(), null, id);
        context.setFXComponentLayout(layout);
        return layout;
    }

    private void initDefaultPerspectiveLayout(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        // init default PerspectiveLayout
        initLocalization(null, AFXPerspective.class.cast(perspective));
        perspective.setIPerspectiveLayout(new FXPerspectiveLayout());
    }

    private void initDeclarativePerspectiveLayout(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        // init PerspectiveLayout for FXML
        final URL url = getClass().getResource(perspective.getViewLocation());
        initLocalization(url, AFXPerspective.class.cast(perspective));
        final Thread t = Thread.currentThread();
        try {
            perspective.setIPerspectiveLayout(new FXMLPerspectiveLayout(
                    FXUtil.loadFXMLandSetController(perspective.getPerspective(), perspective.getContext().getResourceBundle(), url)));
        } catch (final IllegalStateException e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }

    }

    private void initLocalization(final URL url, final AFXPerspective perspective) {
        final String bundleLocation = perspective.getResourceBundleLocation();
        if (bundleLocation.isEmpty()) return;
        final String localeID = perspective.getLocaleID();
        perspective.initialize(url, ResourceBundle.getBundle(bundleLocation, FXUtil.getCorrectLocale(localeID)));

    }

    /**
     * set all child component to invisible
     *
     * @param previousPerspective, the previous visible perspective
     */
    private void onHide(final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective) {
        Optional.ofNullable(previousPerspective.getContext()).ifPresent(context -> {
            final FXComponentLayout layout = Context.class.cast(context).getComponentLayout();
            FXUtil.invokeHandleMethodsByAnnotation(OnHide.class, previousPerspective.getPerspective(), previousPerspective.getIPerspectiveLayout(), layout,
                    previousPerspective.getType().equals(UIType.DECLARATIVE) ? previousPerspective.getDocumentURL() : null, context.getResourceBundle());

        });

    }

    private void log(final String message) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.finest(">> " + message);
        }
    }

    final FXWorkbenchLayout getWorkbenchLayout() {
        return (FXWorkbenchLayout) this.workbenchLayout;
    }

}
