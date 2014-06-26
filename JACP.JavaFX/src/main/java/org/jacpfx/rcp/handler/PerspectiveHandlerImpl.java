/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [FX2WorkbenchHandler.java]
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
package org.jacpfx.rcp.handler;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jacpfx.api.annotations.lifecycle.OnHide;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.PerspectiveView;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.UIComponent;
import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.component.AFXComponent;
import org.jacpfx.rcp.component.ASubComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.FXMLPerspectiveLayout;
import org.jacpfx.rcp.componentLayout.FXPerspectiveLayout;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.perspective.AFXPerspective;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.TearDownHandler;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles initialization and update of perspective in a workbench.
 *
 * @author Andy Moncsek
 */
public class PerspectiveHandlerImpl implements
        org.jacpfx.api.handler.ComponentHandler<Perspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final WorkbenchLayout<Node> workbenchLayout;
    private final Launcher<?> launcher;
    private final GridPane root;


    public PerspectiveHandlerImpl(final Launcher<?> launcher, final WorkbenchLayout<Node> workbenchLayout,
                                  final GridPane root) {
        this.workbenchLayout = workbenchLayout;
        this.root = root;
        this.launcher = launcher;
    }

    @Override
    public final void handleAndReplaceComponent(final Message<Event, Object> action,
                                                final Perspective<EventHandler<Event>, Event, Object> perspective) {
        final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                .getIPerspectiveLayout();
        // backup old component
        final Node componentOld = this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
        perspective.handlePerspective(action);
        if (!perspective.getContext().isActive()) {
            handleInactivePerspective(perspective, perspectiveLayout, componentOld);
        } else {
            handleActivePerspective(perspective, perspectiveLayout, componentOld);
        }

    }

    private void handleActivePerspective(final Perspective<EventHandler<Event>, Event, Object> perspective, final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        if (componentOld != null) {
            this.handlePerspectiveReassignment(perspective, perspectiveLayout, componentOld);
        } // End outer if
        else {
            this.initPerspectiveUI(perspectiveLayout);
        } // End else
    }

    private void handleInactivePerspective(final Perspective<EventHandler<Event>, Event, Object> perspective, final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        // 3 possible variants
        // 1 only one Perspective which is deactivated:  remove...
        // 2 second active perspective available, current perspective is the one which is disabled: find the other perspective, handle OnShow, add not to workbench
        // 3 second perspective is available, other perspective is currently displayed: turn off the perspective

        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, perspective.getPerspective(), perspectiveLayout, JacpContextImpl.class.cast(perspective.getContext()).getComponentLayout(), perspective.getContext().getResourceBundle());
        removePerspectiveNodeFromWorkbench(perspectiveLayout, componentOld);
        displayNextPossiblePerspective(perspective);
        shutDownAndClearComponents(perspective);
    }


    private void shutDownAndClearComponents(final Perspective<EventHandler<Event>, Event, Object> perspective) {
        final List<SubComponent<EventHandler<Event>, Event, Object>> componentsToShutdown = perspective.getSubcomponents();
        componentsToShutdown.stream()
                .filter(c -> c.getContext().isActive())
                .forEach(this::shutDownComponent);
        perspective.removeAllCompnents();
    }

    private void shutDownComponent(final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (AFXComponent.class.isAssignableFrom(component.getClass())) {
            TearDownHandler.shutDownFXComponent(AFXComponent.class.cast(component));
        } else {
            TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(component));
        }
    }

    private void displayNextPossiblePerspective(final Perspective<EventHandler<Event>, Event, Object> current) {
        final Perspective<EventHandler<Event>, Event, Object> possiblePerspectiveToShow = PerspectiveRegistry.findNextActivePerspective(current);
        if (possiblePerspectiveToShow != null) {
            final String possiblePerspectiveId = possiblePerspectiveToShow.getContext().getId();
            final String perspectiveIdBefore = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(possiblePerspectiveToShow.getContext().getId());
            if (!possiblePerspectiveId.equals(perspectiveIdBefore)) {
                final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayoutReplacementComponent = ((AFXPerspective) possiblePerspectiveToShow)
                        .getIPerspectiveLayout();
                final PerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((PerspectiveView<Node, EventHandler<Event>, Event, Object>) possiblePerspectiveToShow);
                // execute OnShow
                FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, possiblePerspectiveToShow.getPerspective(), perspectiveLayoutReplacementComponent,
                        perspectiveView.getType().equals(UIType.DECLARATIVE) ? perspectiveView.getDocumentURL() : null, possiblePerspectiveToShow.getContext().getResourceBundle());
                this.handlePerspectiveReassignment(possiblePerspectiveToShow, perspectiveLayoutReplacementComponent, this.getLayoutComponentFromPerspectiveLayout(perspectiveLayoutReplacementComponent));
            }

        }
    }

    private void removePerspectiveNodeFromWorkbench(final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        this.root.setCacheHint(CacheHint.SPEED);
        final Node nodeToRemove = componentOld != null ? componentOld : this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
        FXUtil.getChildren(nodeToRemove).clear();
        this.root.getChildren().remove(nodeToRemove);
        this.root.setCacheHint(CacheHint.DEFAULT);
    }

    @Override
    public final void initComponent(final Message<Event, Object> message,
                                    final Perspective<EventHandler<Event>, Event, Object> perspective) {
        final Thread t = Thread.currentThread();
        try {
            this.log("3.4.3: perspective handle init");
            FXUtil.performResourceInjection(perspective.getPerspective(), perspective.getContext());

            this.handlePerspectiveInitMethod(message, perspective);
            this.log("3.4.5: perspective init bar entries");
            final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                    .getIPerspectiveLayout();
            this.initPerspectiveUI(perspectiveLayout);
            PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getContext().getId());
            this.log("3.4.4: perspective init subcomponents");
            perspective.initComponents(message);
        } catch (Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }

    }


    /**
     * reassignment can only be done in FX main thread;
     */
    private void handlePerspectiveReassignment(final Perspective<EventHandler<Event>, Event, Object> perspective,
                                               final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
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
    private void reassignSubcomponents(final Perspective<EventHandler<Event>, Event, Object> perspective,
                                       final PerspectiveLayoutInterface<? extends Node, Node> layout) {
        final List<SubComponent<EventHandler<Event>, Event, Object>> subcomponents = perspective.getSubcomponents();
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
    private void addComponentByType(final UIComponent<Node, EventHandler<Event>, Event, Object> component,
                                    final PerspectiveLayoutInterface<? extends Node, Node> layout) {
        final String targetLayout = JacpContextImpl.class.cast(component.getContext()).getTargetLayout();
        final Node validContainer = layout.getTargetLayoutComponents().get(targetLayout);
        final ObservableList<Node> children = FXUtil.getChildren(validContainer);
        final Node currentRoot = component.getRoot();
        if (children == null || currentRoot == null) return;
        int index = children.indexOf(currentRoot);
        if (index < 0) {
            addNewRoot(children, currentRoot);
        } else {
            bringRootToFront(index, children, currentRoot);
        }

    }

    private void bringRootToFront(int index, final ObservableList<Node> children, final Node root) {
        if (index != 0) {
            children.remove(index);
            GridPane.setHgrow(root, Priority.ALWAYS);
            GridPane.setVgrow(root, Priority.ALWAYS);
            children.set(0, root);
        }
    }

    private void addNewRoot(final ObservableList<Node> children, final Node root) {
        GridPane.setHgrow(root, Priority.ALWAYS);
        GridPane.setVgrow(root, Priority.ALWAYS);
        children.add(root);
    }

    /**
     * handle reassignment of component in perspective ui
     *
     * @param perspective, The current perspective
     * @param oldComp,     The old component Node
     * @param newComp,     The new component Node
     */
    private void reassignChild(final Perspective<EventHandler<Event>, Event, Object> perspective, final Node oldComp, final Node newComp) {
        final ObservableList<Node> children = this.root.getChildren();
        final Perspective<EventHandler<Event>, Event, Object> previousPerspective = getPreviousPerspective(perspective);

        hideChildrenAndExecuteOnHide(perspective, previousPerspective, children);
        executeOnShow(perspective, previousPerspective);
        replaceRootNodes(children, oldComp, newComp);

        newComp.setVisible(true);
    }

    private void handleToolBarButtons(final Perspective<EventHandler<Event>, Event, Object> perspective, final boolean visible) {
        log("handleToolBarButtons >" + perspective.getPerspective().getClass().getName() + "<");
        final Collection<? extends Node> values = this.workbenchLayout.getRegisteredToolBars().values();
        values.forEach(node->{
            JACPToolBar toolBar = (JACPToolBar) node;
            if (visible) {
                toolBar.showButtons(perspective);
            } else {
                toolBar.hideButtons(perspective);
            }
        });
    }

    private void replaceRootNodes(final ObservableList<Node> children, final Node oldComp, final Node newComp) {
        if (!oldComp.equals(newComp)) {
            children.remove(oldComp);
            children.add(newComp);
        }
    }

    private void executeOnShow(final Perspective<EventHandler<Event>, Event, Object> perspective, final Perspective<EventHandler<Event>, Event, Object> previousPerspective) {
        if (!perspective.equals(previousPerspective)) {
            final PerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((PerspectiveView<Node, EventHandler<Event>, Event, Object>) perspective);
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout(),perspective.getContext().getParentId(),perspective.getContext().getId());
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
    private Perspective<EventHandler<Event>, Event, Object> getPreviousPerspective(final Perspective<EventHandler<Event>, Event, Object> perspective) {
        final String previousId = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getContext().getId());
        if (previousId == null) return perspective;
        return perspective.getContext().getId().equals(previousId) ? perspective : PerspectiveRegistry.findPerspectiveById(previousId);
    }

    /**
     * add perspective UI to workbench root component
     *
     * @param perspectiveLayout, The perspective layout
     */
    private void initPerspectiveUI(final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout) {
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
                                             final Perspective<EventHandler<Event>, Event, Object> perspective) {
        handlePerspectiveView(perspective);

        // set perspective to active
        JacpContextImpl.class.cast(perspective.getContext()).setActive(true);
        if (FXUtil.getTargetPerspectiveId(action.getTargetId()).equals(perspective.getContext().getId())) {
            this.log("3.4.3.1: perspective handle with custom message");
            perspective.handlePerspective(action);
        } // End if
        else {
            this.log("3.4.3.1: perspective handle with default >>init<< message");
            perspective.handlePerspective(new MessageImpl(perspective.getContext().getId(), perspective.getContext().getId(), "init", null));
        } // End else
    }

    private void handlePerspectiveView(final Perspective<EventHandler<Event>, Event, Object> perspective) {
        if (perspective instanceof PerspectiveView) {
            final JacpContextImpl context = JacpContextImpl.class.cast(perspective.getContext());
            initFXComponentLayout(context);

            final PerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((PerspectiveView<Node, EventHandler<Event>, Event, Object>) perspective);

            handleUIPerspective(perspective, perspectiveView, context);

            perspective.postInit(new ComponentHandlerImpl(this.launcher, perspectiveView.getIPerspectiveLayout(), perspective
                    .getComponentDelegateQueue()));
        }
    }

    private void handleUIPerspective(final Perspective<EventHandler<Event>, Event, Object> perspective,
                                     final PerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView,
                                     final JacpContextImpl context) {
        final AFXPerspective perspectiveImpl = AFXPerspective.class.cast(perspective);
        if (perspectiveView.getType().equals(UIType.DECLARATIVE)) {
            handleDeclarativePerspective(perspectiveImpl, perspectiveView);
        } else {
            handleDefaultPerspective(perspectiveImpl);
        }
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class,
                perspective.getPerspective(), perspectiveImpl.getIPerspectiveLayout(), context.getComponentLayout(), perspective.getContext().getResourceBundle());
    }

    private void initFXComponentLayout(final JacpContextImpl context) {
        context.setFXComponentLayout(new FXComponentLayout(this.getWorkbenchLayout(),context.getParentId(),context.getId()));
    }

    private void handleDefaultPerspective(final AFXPerspective perspective) {
        // init default PerspectiveLayout
        initLocalization(null, perspective);
        perspective.setIPerspectiveLayout(new FXPerspectiveLayout());
    }

    private void handleDeclarativePerspective(final AFXPerspective perspective,
                                              final PerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView) {
        // init PerspectiveLayout for FXML
        final URL url = getClass().getResource(perspectiveView.getViewLocation());
        initLocalization(url, perspective);
        try {
            perspective.setIPerspectiveLayout(new FXMLPerspectiveLayout(
                    FXUtil.loadFXMLandSetController(perspectiveView.getPerspective(), perspectiveView.getContext().getResourceBundle(), url)));
        } catch (IllegalStateException e) {
            throw new MissingResourceException(
                    "fxml file not found --  place in resource folder and reference like this: viewLocation = \"/myUIFile.fxml\"",
                    url.getPath(), "");
        }

    }

    private void initLocalization(final URL url, final AFXPerspective perspective) {
        final String bundleLocation = perspective.getResourceBundleLocation();
        if (bundleLocation.isEmpty()) return;
        final String localeID = perspective.getLocaleID();
        perspective.initialize(url, ResourceBundle.getBundle(bundleLocation, FXUtil.getCorrectLocale(localeID)));

    }

    /**
     * get perspective ui root container
     *
     * @param layout, The perspective layout
     * @return the root Node
     */
    private Node getLayoutComponentFromPerspectiveLayout(final PerspectiveLayoutInterface<? extends Node, Node> layout) {
        return layout.getRootComponent();
    }

    /**
     * set all child component to invisible
     *
     * @param children, The list of children which should be set invisible
     */
    private void hideChildren(final ObservableList<Node> children) {
        children.forEach(c -> {
            if (c.isVisible()) c.setVisible(false);
        });
    }

    /**
     * set all child component to invisible
     *
     * @param perspective,         the current perspective
     * @param previousPerspective, the previous visible perspective
     * @param children,            JavaFX node children
     */
    private void hideChildrenAndExecuteOnHide(final Perspective<EventHandler<Event>, Event, Object> perspective, final Perspective<EventHandler<Event>, Event, Object> previousPerspective, final ObservableList<Node> children) {
        hideChildren(children);
        if (previousPerspective != null && !previousPerspective.equals(perspective)) {
            final PerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((PerspectiveView<Node, EventHandler<Event>, Event, Object>) previousPerspective);
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout(),previousPerspective.getContext().getParentId(),previousPerspective.getContext().getId());
            this.handleToolBarButtons(previousPerspective, false);
            this.handleToolBarButtons(perspective, true);
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
