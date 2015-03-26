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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.jacpfx.api.annotations.lifecycle.OnHide;
import org.jacpfx.api.annotations.lifecycle.OnShow;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.UIComponent;
import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
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
import org.jacpfx.rcp.util.TearDownHandler;
import org.jacpfx.rcp.workbench.GlobalMediator;

import java.net.URL;
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

    @Override
    public final void handleAndReplaceComponent(final Message<Event, Object> action,
                                                final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout = perspective
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

    private void handleActivePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        if (componentOld != null) {
            this.handlePerspectiveReassignment(perspective, perspectiveLayout, componentOld);
        } // End outer if
        else {
            this.initPerspectiveUI(perspectiveLayout);
        } // End else
    }

    private void handleInactivePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        // 3 possible variants
        // 1 only one Perspective which is deactivated:  remove...
        // 2 second active perspective available, current perspective is the one which is disabled: find the other perspective, handle OnShow, add not to workbench
        // 3 second perspective is available, other perspective is currently displayed: turn off the perspective
        FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class, perspective.getPerspective(), perspectiveLayout, Context.class.cast(perspective.getContext()).getComponentLayout(), perspective.getContext().getResourceBundle());
        removePerspectiveNodeFromWorkbench(perspectiveLayout, componentOld);
        displayNextPossiblePerspective(perspective);
        shutDownAndClearComponents(perspective);
    }

    private void shutDownAndClearComponents(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final List<SubComponent<EventHandler<Event>, Event, Object>> componentsToShutdown = perspective.getSubcomponents();
        componentsToShutdown.stream()
                .filter(c -> c.getContext().isActive())
                .forEach(this::shutDownComponent);
        perspective.removeAllCompnents();
    }

    private void shutDownComponent(final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (EmbeddedFXComponent.class.isAssignableFrom(component.getClass())) {
            TearDownHandler.shutDownFXComponent(EmbeddedFXComponent.class.cast(component), component.getContext().getParentId());
        } else {
            TearDownHandler.shutDownAsyncComponent(ASubComponent.class.cast(component));
        }
    }

    private void displayNextPossiblePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> current) {
        final Perspective<Node, EventHandler<Event>, Event, Object> possiblePerspectiveToShow = PerspectiveRegistry.findNextActivePerspective(current);
        if (possiblePerspectiveToShow != null) {
            final String possiblePerspectiveId = possiblePerspectiveToShow.getContext().getId();
            final String perspectiveIdBefore = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(possiblePerspectiveToShow.getContext().getId());
            if (!possiblePerspectiveId.equals(perspectiveIdBefore)) {
                final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayoutReplacementComponent = possiblePerspectiveToShow
                        .getIPerspectiveLayout();
                final Perspective<Node, EventHandler<Event>, Event, Object> Perspective = possiblePerspectiveToShow;
                // execute OnShow
                FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, possiblePerspectiveToShow.getPerspective(), perspectiveLayoutReplacementComponent,
                        Perspective.getType().equals(UIType.DECLARATIVE) ? Perspective.getDocumentURL() : null, possiblePerspectiveToShow.getContext().getResourceBundle());
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
                                    final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final Thread t = Thread.currentThread();
        try {
            this.log("3.4.3: perspective handle init");

            FXUtil.performResourceInjection(perspective.getPerspective(), perspective.getContext());

            this.handlePerspectiveInitMethod(message, perspective);
            this.log("3.4.5: perspective init bar entries");
            final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout = perspective
                    .getIPerspectiveLayout();
            this.initPerspectiveUI(perspectiveLayout);

            final String currentPerspectiveId = perspective.getContext().getId();
            final String previousPerspectiveId = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getContext().getId());

            this.updateToolbarButtons(currentPerspectiveId, previousPerspectiveId);

            this.log("3.4.4: perspective init subcomponents");
            perspective.initComponents(message);
        } catch (Exception e) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }

    }


    /**
     * check if switch from active to inactive perspective
     *
     * @param currentPerspectiveId
     * @param previousPerspectiveId
     */
    private void updateToolbarButtons(final String currentPerspectiveId, final String previousPerspectiveId) {
        if (previousPerspectiveId != null && !previousPerspectiveId.equals(currentPerspectiveId)) {
            final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective = PerspectiveRegistry.findPerspectiveById(previousPerspectiveId);
            // hide all buttons of the previous perspective
            GlobalMediator.getInstance().handleToolBarButtons(previousPerspective, false);
        }
    }

    /**
     * reassignment can only be done in FX main thread;
     */
    private void handlePerspectiveReassignment(final Perspective<Node, EventHandler<Event>, Event, Object> perspective,
                                               final PerspectiveLayoutInterface<? extends Node, Node> perspectiveLayout, final Node componentOld) {
        // FIXME the reassignment should be avoid when calls to previous perspective is done
        this.reassignChild(perspective, componentOld, getLayoutComponentFromPerspectiveLayout(perspectiveLayout));
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
        final List<SubComponent<EventHandler<Event>, Event, Object>> subcomponents = perspective.getSubcomponents();
        if (subcomponents == null)
            return;
        subcomponents.forEach(subComp -> {
            if (subComp instanceof EmbeddedFXComponent && subComp.getContext().isActive()) {
                final EmbeddedFXComponent subComponent = (EmbeddedFXComponent) subComp;
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
        final String targetLayout = InternalContext.class.cast(component.getContext()).getTargetLayout();
        final Node validContainer = layout.getTargetLayoutComponents().get(targetLayout);
        runInCachedModeSpeed(validContainer,()-> {
            final ObservableList<Node> children = FXUtil.getChildren(validContainer);
            final Node currentRoot = component.getRoot();
            if (children == null || currentRoot == null) return;
            int index = children.indexOf(currentRoot);
            if (index < 0) {
                addNewRoot(children, currentRoot);
            } else {
                bringRootToFront(index, children, currentRoot);
            }
        });
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
    private void reassignChild(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final Node oldComp, final Node newComp) {
        runInCachedModeSpeed(this.root,() -> {
            final ObservableList<Node> children = this.root.getChildren();
            final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective = getPreviousPerspective(perspective);

            hideChildrenAndExecuteOnHide(perspective, previousPerspective, children);
            // hide all buttons of the previous perspective
            GlobalMediator.getInstance().handleToolBarButtons(previousPerspective, false);
            executeOnShow(perspective, previousPerspective);
            replaceRootNodes(children, oldComp, newComp);
            // show all buttons of the new perspective
            GlobalMediator.getInstance().handleToolBarButtons(perspective, true);
            newComp.setVisible(true);
        });

    }


    private void runInCachedModeSpeed(final Node rootNode, final Runnable r) {
        rootNode.setManaged(false);
        if (!rootNode.isCache()) rootNode.setCache(true);
        CacheHint hint = rootNode.getCacheHint();
        rootNode.setCacheHint(CacheHint.SPEED);
        r.run();
        rootNode.setCacheHint(hint);
        rootNode.setManaged(true);
    }

    private void replaceRootNodes(final ObservableList<Node> children, final Node oldComp, final Node newComp) {
        children.set(0, newComp);
    }

    private void executeOnShow(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective) {
        if (!perspective.equals(previousPerspective)) {
            final Perspective<Node, EventHandler<Event>, Event, Object> Perspective = perspective;
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout(), null, perspective.getContext().getId());
            // execute OnShow
            FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, perspective.getPerspective(), layout,
                    Perspective.getType().equals(UIType.DECLARATIVE) ? Perspective.getDocumentURL() : null, Perspective.getContext().getResourceBundle());

        }
    }

    /**
     * returns the previous visible perspective
     *
     * @param perspective, The current perspective
     * @return Returns the previous visible perspective
     */
    private Perspective<Node, EventHandler<Event>, Event, Object> getPreviousPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
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
            children.clear();
            GridPane.setConstraints(comp, 0, 0);
            children.add(comp);
        }
    }

    private void handlePerspectiveInitMethod(final Message<Event, Object> action,
                                             final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        handlePerspective(perspective);

        // set perspective to active
        InternalContext.class.cast(perspective.getContext()).updateActiveState(true);
        if (FXUtil.getTargetPerspectiveId(action.getTargetId()).equals(perspective.getContext().getId())) {
            this.log("3.4.3.1: perspective handle with custom message");
            perspective.handlePerspective(action);
        } // End if
        else {
            this.log("3.4.3.1: perspective handle with default >>init<< message");
            perspective.handlePerspective(new MessageImpl(perspective.getContext().getId(), perspective.getContext().getId(), "init", null));
        } // End else
    }

    private void handlePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        if (perspective instanceof Perspective) {
            final InternalContext context = InternalContext.class.cast(perspective.getContext());
            initFXComponentLayout(context, perspective.getContext().getId());

            final Perspective<Node, EventHandler<Event>, Event, Object> Perspective = perspective;

            handleUIPerspective(perspective, Perspective, Context.class.cast(perspective.getContext()).getComponentLayout());

            perspective.postInit(new ComponentHandlerImpl(this.launcher, Perspective.getIPerspectiveLayout(), perspective
                    .getComponentDelegateQueue()));
        }
    }

    private void handleUIPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective,
                                     final Perspective<Node, EventHandler<Event>, Event, Object> Perspective,
                                     final FXComponentLayout layout) {
        final AFXPerspective perspectiveImpl = AFXPerspective.class.cast(perspective);
        if (Perspective.getType().equals(UIType.DECLARATIVE)) {
            handleDeclarativePerspective(perspectiveImpl, Perspective);
        } else {
            handleDefaultPerspective(perspectiveImpl);
        }
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class,
                perspective.getPerspective(), perspectiveImpl.getIPerspectiveLayout(), layout, perspective.getContext().getResourceBundle());
    }

    private void initFXComponentLayout(final InternalContext context, final String id) {
        context.setFXComponentLayout(new FXComponentLayout(this.getWorkbenchLayout(), null, id));
    }

    private void handleDefaultPerspective(final AFXPerspective perspective) {
        // init default PerspectiveLayout
        initLocalization(null, perspective);
        perspective.setIPerspectiveLayout(new FXPerspectiveLayout());
    }

    private void handleDeclarativePerspective(final AFXPerspective perspective,
                                              final Perspective<Node, EventHandler<Event>, Event, Object> Perspective) {
        // init PerspectiveLayout for FXML
        final URL url = getClass().getResource(Perspective.getViewLocation());
        initLocalization(url, perspective);
        try {
            perspective.setIPerspectiveLayout(new FXMLPerspectiveLayout(
                    FXUtil.loadFXMLandSetController(Perspective.getPerspective(), Perspective.getContext().getResourceBundle(), url)));
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
    private void hideChildrenAndExecuteOnHide(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final Perspective<Node, EventHandler<Event>, Event, Object> previousPerspective, final ObservableList<Node> children) {
        // hideChildren(children);
        if (previousPerspective != null && !previousPerspective.equals(perspective)) {
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout(), null, previousPerspective.getContext().getId());
            FXUtil.invokeHandleMethodsByAnnotation(OnHide.class, previousPerspective.getPerspective(), layout,
                    perspective.getType().equals(UIType.DECLARATIVE) ? perspective.getDocumentURL() : null, perspective.getContext().getResourceBundle());

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
