/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [FX2WorkbenchHandler.java]
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
package org.jacpfx.rcp.handler;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.annotations.lifecycle.OnHide;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.IPerspectiveView;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.component.IUIComponent;
import org.jacpfx.api.componentLayout.IPerspectiveLayout;
import org.jacpfx.api.componentLayout.IWorkbenchLayout;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.message.FXMessage;
import org.jacpfx.rcp.component.AFXComponent;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.FXMLPerspectiveLayout;
import org.jacpfx.rcp.componentLayout.FXPerspectiveLayout;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.context.JACPContextImpl;
import org.jacpfx.rcp.perspective.AFXPerspective;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.TearDownHandler;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles initialization and update of perspectives in a workbench.
 *
 * @author Andy Moncsek
 */
public class FXPerspectiveHandler implements
        IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final IWorkbenchLayout<Node> workbenchLayout;
    private final Launcher<?> launcher;
    private final GridPane root;


    public FXPerspectiveHandler(final Launcher<?> launcher, final IWorkbenchLayout<Node> workbenchLayout,
                                final GridPane root) {
        this.workbenchLayout = workbenchLayout;
        this.root = root;
        this.launcher = launcher;
    }

    @Override
    public final void handleAndReplaceComponent(final Message<Event, Object> action,
                                                final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        Platform.runLater(() -> {
            final IPerspectiveLayout<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                    .getIPerspectiveLayout();
            // backup old component
            final Node componentOld = this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
            perspective.handlePerspective(action);
            if (!perspective.getContext().isActive()) {
                 handleInactivePerspective(perspective,perspectiveLayout,componentOld);
                return;
            }
            handleActivePerspective(perspective,perspectiveLayout,componentOld);
        }); // End runlater

    }

    private void handleActivePerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective,final IPerspectiveLayout<? extends Node, Node> perspectiveLayout,final Node componentOld) {
        if (componentOld != null) {
            this.handlePerspectiveReassignment(perspective, perspectiveLayout, componentOld);
        } // End outer if
        else {
            this.initPerspectiveUI(perspectiveLayout);
        } // End else
    }

    private void handleInactivePerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective,final IPerspectiveLayout<? extends Node, Node> perspectiveLayout,final Node componentOld) {
        // 3 possible variants
        // 1 only one Perspective which is deactivated:  remove...
        // 2 second active perspective available, current perspective is the one which is disabled: find the other perspective, handle OnShow, add not to workbench
        // 3 second perspective is available, other perspective is currently displayed: turn off the perspective

        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, perspective.getPerspective(), perspectiveLayout, JACPContextImpl.class.cast(perspective.getContext()).getComponentLayout(),perspective.getContext().getResourceBundle());
        removePerspectiveNodeFromWorkbench(perspectiveLayout, componentOld);
        displayNextPossiblePerspective(perspective);
        shutDownAndClearComponents(perspective);
       // removePerspectiveFromRegistry(perspective);
    }

    private void removePerspectiveFromRegistry(final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        // TODO Perspective must be removed from workbench!!
        PerspectiveRegistry.removePerspective(perspective);
    }

    private void shutDownAndClearComponents(final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final List<ISubComponent<EventHandler<Event>, Event, Object>> componentsToShutdown = perspective.getSubcomponents();
        componentsToShutdown.stream()
                .filter(c->c.getContext().isActive())
                .forEach(this::shutDownComponent);
        perspective.removeAllCompnents();
    }

    private void shutDownComponent(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        if(AFXComponent.class.isAssignableFrom(component.getClass()))  {
            TearDownHandler.shutDownFXComponent(AFXComponent.class.cast(component));
        } else {
            TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(component));
        }
    }

    private void displayNextPossiblePerspective(final IPerspective<EventHandler<Event>, Event, Object> current) {
        final IPerspective<EventHandler<Event>, Event, Object> possiblePerspectiveToShow = PerspectiveRegistry.findNextActivePerspective(current);
        if (possiblePerspectiveToShow != null) {
            final String possiblePerspectiveId = possiblePerspectiveToShow.getContext().getId();
            final String perspectiveIdBefore = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(possiblePerspectiveToShow.getContext().getId());
            if (!possiblePerspectiveId.equals(perspectiveIdBefore)) {
                final IPerspectiveLayout<? extends Node, Node> perspectiveLayoutReplacementComponent = ((AFXPerspective) possiblePerspectiveToShow)
                        .getIPerspectiveLayout();
                final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) possiblePerspectiveToShow);
                // execute OnShow
                FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, possiblePerspectiveToShow.getPerspective(), perspectiveLayoutReplacementComponent,
                        perspectiveView.getType().equals(UIType.DECLARATIVE) ? perspectiveView.getDocumentURL() : null, possiblePerspectiveToShow.getContext().getResourceBundle());
                this.handlePerspectiveReassignment(possiblePerspectiveToShow, perspectiveLayoutReplacementComponent, this.getLayoutComponentFromPerspectiveLayout(perspectiveLayoutReplacementComponent));
            }

        }
    }

    private void removePerspectiveNodeFromWorkbench(final IPerspectiveLayout<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        this.root.setCacheHint(CacheHint.SPEED);
        final Node nodeToRemove = componentOld != null ? componentOld : this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
        FXUtil.getChildren(nodeToRemove).clear();
        this.root.getChildren().remove(nodeToRemove);
        this.root.setCacheHint(CacheHint.DEFAULT);
    }

    @Override
    public final void initComponent(final Message<Event, Object> action,
                                    final IPerspective<EventHandler<Event>, Event, Object> perspective) {

        this.log("3.4.3: perspective handle init");
        FXUtil.performResourceInjection(perspective.getPerspective(), perspective.getContext());

        this.handlePerspectiveInitMethod(action, perspective);
        this.log("3.4.5: perspective init bar entries");
        final IPerspectiveLayout<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                .getIPerspectiveLayout();
        this.initPerspectiveUI(perspectiveLayout);
        PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getContext().getId());
        this.log("3.4.4: perspective init subcomponents");
        perspective.initComponents(action);

    }

    /**
     * reassignment can only be done in FX main thread;
     */
    private void handlePerspectiveReassignment(final IPerspective<EventHandler<Event>, Event, Object> perspective,
                                               final IPerspectiveLayout<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        this.root.setCacheHint(CacheHint.SPEED);
        final Node componentNew = this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
        componentNew.setCache(true);
        this.reassignChild(perspective, componentOld, componentNew);
        // set already active editors to new component
        this.reassignSubcomponents(perspective, perspectiveLayout);
        this.root.setCacheHint(CacheHint.DEFAULT);
    }

    /**
     * add all active subcomponents to current perspective
     *
     * @param perspective, The perspective where to reassign
     * @param layout,      The perspective layout
     */
    private void reassignSubcomponents(final IPerspective<EventHandler<Event>, Event, Object> perspective,
                                       final IPerspectiveLayout<? extends Node, Node> layout) {
        final List<ISubComponent<EventHandler<Event>, Event, Object>> subcomponents = perspective.getSubcomponents();
        if (subcomponents == null)
            return;
        subcomponents.forEach(subComp -> {
            if (subComp instanceof AFXComponent && subComp.getContext().isActive()) {
                final AFXComponent subComponent = (AFXComponent) subComp;
                this.addComponentByType(subComponent, layout);
            } // End outer if
        });

    }


    /**
     * find valid target and add type specific new ui component
     *
     * @param component, The ui component to add
     * @param layout,    The perspective layout
     */
    private void addComponentByType(final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
                                    final IPerspectiveLayout<? extends Node, Node> layout) {
        final String targetLayout = JACPContextImpl.class.cast(component.getContext()).getTargetLayout();
        final Node validContainer = layout.getTargetLayoutComponents().get(targetLayout);
        final ObservableList<Node> children = FXUtil.getChildren(validContainer);
        final Node root = component.getRoot();
        if (children == null || root == null) return;
        if (!children.contains(root)) {
            addNewRoot(children,root);
        } else {
            bringRootToFront(children,root);
        }

    }

    private void bringRootToFront(final ObservableList<Node> children,final Node root) {
        int index = children.indexOf(root);
        if (index != 0) {
            GridPane.setHgrow(root, Priority.ALWAYS);
            GridPane.setVgrow(root, Priority.ALWAYS);
            children.set(0, root);
        }
    }

    private void addNewRoot(final ObservableList<Node> children,final Node root) {
        GridPane.setHgrow(root, Priority.ALWAYS);
        GridPane.setVgrow(root, Priority.ALWAYS);
        children.add(root);
    }

    /**
     * handle reassignment of components in perspective ui
     *
     * @param perspective, The current perspective
     * @param oldComp,     The old component Node
     * @param newComp,     The new component Node
     */
    private void reassignChild(final IPerspective<EventHandler<Event>, Event, Object> perspective, final Node oldComp, final Node newComp) {
        final ObservableList<Node> children = this.root.getChildren();
        final IPerspective<EventHandler<Event>, Event, Object> previousPerspective = getPreviousPerspective(perspective);

        hideChildrenAndExecuteOnHide(perspective, previousPerspective, children);
        executeOnShow(perspective,previousPerspective);
        replaceRootNodes(children,oldComp,newComp);

        newComp.setVisible(true);
    }

    private void replaceRootNodes(final ObservableList<Node> children,final Node oldComp, final Node newComp) {
        if (!oldComp.equals(newComp)) {
            children.remove(oldComp);
            children.add(newComp);
        }
    }

    private void executeOnShow(final IPerspective<EventHandler<Event>, Event, Object> perspective,final IPerspective<EventHandler<Event>, Event, Object> previousPerspective) {
        if (!perspective.equals(previousPerspective)) {
            final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) perspective);
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout());
            // execute OnShow
            FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, perspective.getPerspective(), layout,
                    perspectiveView.getType().equals(UIType.DECLARATIVE) ? perspectiveView.getDocumentURL() : null, perspectiveView.getContext().getResourceBundle());

        }
    }

    /**
     * returns the previous visible perspective
     *
     * @param perspective, The current perspective
     * @return Returns the previous visible perspective
     */
    private IPerspective<EventHandler<Event>, Event, Object> getPreviousPerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final String previousId = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getContext().getId());
        if (previousId == null) return perspective;
        return perspective.getContext().getId().equals(previousId) ? perspective : PerspectiveRegistry.findPerspectiveById(previousId);
    }

    /**
     * add perspective UI to workbench root component
     *
     * @param perspectiveLayout, The perspective layout
     */
    private void initPerspectiveUI(final IPerspectiveLayout<? extends Node, Node> perspectiveLayout) {
        this.log("3.4.6: perspective init SINGLE_PANE");
        final Node comp = this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
        if (comp != null) {
            comp.setVisible(true);
            comp.setCache(true);
            final ObservableList<Node> children = this.root.getChildren();
            this.hideChildren(children);
            GridPane.setConstraints(comp, 0, 0);
            children.add(comp);
        }
    }


    private void handlePerspectiveInitMethod(final Message<Event, Object> action,
                                             final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        handlePerspectiveView(perspective);

        // set perspective to active
        JACPContextImpl.class.cast(perspective.getContext()).setActive(true);
        if (FXUtil.getTargetPerspectiveId(action.getTargetId()).equals(perspective.getContext().getId())) {
            this.log("3.4.3.1: perspective handle with custom message");
            perspective.handlePerspective(action);
        } // End if
        else {
            this.log("3.4.3.1: perspective handle with default >>init<< message");
            perspective.handlePerspective(new FXMessage(perspective.getContext().getId(), perspective.getContext().getId(), "init", null));
        } // End else
    }

    private void handlePerspectiveView(final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        if (perspective instanceof IPerspectiveView) {
            final JACPContextImpl context = JACPContextImpl.class.cast(perspective.getContext());
            initFXComponentLayout(context);

            final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) perspective);

            handleUIPerspective(perspective,perspectiveView,context);

            perspective.postInit(new FXComponentHandler(this.launcher, perspectiveView.getIPerspectiveLayout(), perspective
                    .getComponentDelegateQueue()));
        }
    }

    private void handleUIPerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective,final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView,final JACPContextImpl context) {
        if (perspectiveView.getType().equals(UIType.DECLARATIVE)) {
            handleDeclarativePerspective(perspective, context.getComponentLayout(), perspectiveView);
        } else {
            handleDefaultPerspective(perspective, context.getComponentLayout());
        }
    }

    private void initFXComponentLayout(final JACPContextImpl context) {
        context.setFXComponentLayout(new FXComponentLayout(this.getWorkbenchLayout()));
    }

    private void handleDefaultPerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective, final FXComponentLayout layout) {
        // init default IPerspectiveLayout
        initLocalization(null, AFXPerspective.class.cast(perspective));
        AFXPerspective.class.cast(perspective).setIPerspectiveLayout(new FXPerspectiveLayout());
        final IPerspectiveLayout<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                .getIPerspectiveLayout();
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, perspective.getPerspective(), perspectiveLayout,layout, perspective.getContext().getResourceBundle());
    }

    private void handleDeclarativePerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective, final FXComponentLayout layout, final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView) {
        // init IPerspectiveLayout for FXML
        final URL url = getClass().getResource(perspectiveView.getViewLocation());
        initLocalization(url, (AFXPerspective) perspective);
        AFXPerspective.class.cast(perspective).setIPerspectiveLayout(new FXMLPerspectiveLayout(
                FXUtil.loadFXMLandSetController(perspectiveView.getPerspective(),perspectiveView.getContext().getResourceBundle(),url)));
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, perspective.getPerspective(), layout,
                perspectiveView.getDocumentURL(), perspectiveView.getContext().getResourceBundle());
    }

    private void initLocalization(final URL url, final AFXPerspective perspective) {
        final String bundleLocation = perspective.getResourceBundleLocation();
        if (bundleLocation.equals("")) return;
        final String localeID = perspective.getLocaleID();
        perspective.initialize(url, ResourceBundle.getBundle(bundleLocation, FXUtil.getCorrectLocale(localeID)));

    }

    /**
     * get perspectives ui root container
     *
     * @param layout, The perspective layout
     * @return the root Node
     */
    private Node getLayoutComponentFromPerspectiveLayout(final IPerspectiveLayout<? extends Node, Node> layout) {
        return layout.getRootComponent();
    }

    /**
     * set all child components to invisible
     *
     * @param children, The list of children which should be set invisible
     */
    private void hideChildren(final ObservableList<Node> children) {
        children.forEach(c -> {
            if (c.isVisible()) c.setVisible(false);
        });
    }

    /**
     * set all child components to invisible
     *
     * @param perspective,         the current perspective
     * @param previousPerspective, the previous visible perspective
     * @param children,            JavaFX node children
     */
    private void hideChildrenAndExecuteOnHide(final IPerspective<EventHandler<Event>, Event, Object> perspective, final IPerspective<EventHandler<Event>, Event, Object> previousPerspective, final ObservableList<Node> children) {
        hideChildren(children);
        if (previousPerspective != null && !previousPerspective.equals(perspective)) {
            final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) previousPerspective);
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout());
            FXUtil.invokeHandleMethodsByAnnotation(OnHide.class, previousPerspective.getPerspective(), layout,
                    perspectiveView.getType().equals(UIType.DECLARATIVE) ? perspectiveView.getDocumentURL() : null, perspectiveView.getContext().getResourceBundle());

        }

    }

    private void log(final String message) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine(">> " + message);
        }
    }

    final FXWorkbenchLayout getWorkbenchLayout() {
        return (FXWorkbenchLayout) this.workbenchLayout;
    }

}
