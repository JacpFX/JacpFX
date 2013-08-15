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
package org.jacp.javafx.rcp.handler;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.OnHide;
import org.jacp.api.annotations.OnShow;
import org.jacp.api.annotations.PostConstruct;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.IPerspectiveView;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.IUIComponent;
import org.jacp.api.componentLayout.IPerspectiveLayout;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.util.UIType;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.componentLayout.FXMLPerspectiveLayout;
import org.jacp.javafx.rcp.componentLayout.FXPerspectiveLayout;
import org.jacp.javafx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacp.javafx.rcp.perspective.AFXPerspective;
import org.jacp.javafx.rcp.util.FXUtil;
import org.jacp.javafx.rcp.util.PerspectiveRegistry;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Handles initialization and re assignment of perspectives in workbench.
 *
 * @author Andy Moncsek
 */
public class FXWorkbenchHandler implements
        IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final IWorkbenchLayout<Node> workbenchLayout;
    private final Launcher<?> launcher;
    private final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives;
    private final GridPane root;



    public FXWorkbenchHandler(final Launcher<?> launcher, final IWorkbenchLayout<Node> workbenchLayout,
                              final GridPane root, final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives) {
        this.workbenchLayout = workbenchLayout;
        this.root = root;
        this.perspectives = perspectives;
        this.launcher = launcher;
    }

    @Override
    public final void handleAndReplaceComponent(final IAction<Event, Object> action,
                                                final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final IPerspectiveLayout<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                .getIPerspectiveLayout();
        // backup old component
        final Node componentOld = this.getLayoutComponentFromPerspectiveLayout(perspectiveLayout);
        perspective.handlePerspective(action);
        if (componentOld != null) {
            this.handlePerspectiveReassignment(perspective, perspectiveLayout, componentOld);
        } // End outer if
        else {
            this.initPerspectiveUI(perspectiveLayout);
        } // End else

    }

    @Override
    public final void initComponent(final IAction<Event, Object> action,
                                    final IPerspective<EventHandler<Event>, Event, Object> perspective) {

        this.log("3.4.3: perspective handle init");
        this.handlePerspectiveInitMethod(action, perspective);
        this.log("3.4.4: perspective init subcomponents");
        perspective.initComponents(action);
        final IPerspectiveLayout<? extends Node, Node> perspectiveLayout = ((AFXPerspective) perspective)
                .getIPerspectiveLayout();
        this.log("3.4.5: perspective init bar entries");
        this.initPerspectiveUI(perspectiveLayout);
        PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getId());

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
     * add all active subcomponents to replaced perspective
     *
     * @param layout
     * @param perspective
     */
    private void reassignSubcomponents(final IPerspective<EventHandler<Event>, Event, Object> perspective,
                                       final IPerspectiveLayout<? extends Node, Node> layout) {
        this.runReassign(perspective, layout);

    }

    /**
     * call reassign of all components in perspective
     *
     * @param layout
     * @param perspective
     */
    private void runReassign(final IPerspective<EventHandler<Event>, Event, Object> perspective,
                             final IPerspectiveLayout<? extends Node, Node> layout) {
        final List<ISubComponent<EventHandler<Event>, Event, Object>> subcomponents = perspective.getSubcomponents();
        if (subcomponents == null)
            return;
        subcomponents.forEach(subComp -> {
            if (subComp instanceof AFXComponent && subComp.isActive()) {
                final AFXComponent subComponent = (AFXComponent) subComp;
                this.addComponentByType(subComponent, layout);
            } // End outer if
        });

    }

    /**
     * find valid target and add type specific new ui component
     *
     * @param component
     * @param layout
     */
    private void addComponentByType(final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
                                    final IPerspectiveLayout<? extends Node, Node> layout) {
        final Node validContainer = layout.getTargetLayoutComponents().get(component.getExecutionTarget());
        final ObservableList<Node> children = FXUtil.getChildren(validContainer);
        final Node root = component.getRoot();
        if (children == null || root == null) return;
        if (!children.contains(root)) {
            GridPane.setHgrow(root, Priority.ALWAYS);
            GridPane.setVgrow(root, Priority.ALWAYS);
            children.add(root);
        } else {
            int index = children.indexOf(root);
            if (index != 0) {
                GridPane.setHgrow(root, Priority.ALWAYS);
                GridPane.setVgrow(root, Priority.ALWAYS);
                children.set(0, root);
            }

        }

    }

    /**
     * handle reassignment of components in perspective ui
     *
     * @param oldComp
     * @param newComp
     */
    private void reassignChild(final IPerspective<EventHandler<Event>, Event, Object> perspective, final Node oldComp, final Node newComp) {
        final ObservableList<Node> children = this.root.getChildren();
        final IPerspective<EventHandler<Event>, Event, Object> previousPerspective = getPreviousPerspective(perspective);

        hideChildrenAndExecuteOnHide(perspective, previousPerspective, children);

        final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) perspective);
        final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout());
        if (!perspective.equals(previousPerspective)) {
            // execute OnShow
            FXUtil.invokeHandleMethodsByAnnotation(OnShow.class, perspective, layout,
                    perspectiveView.getDocumentURL(), perspectiveView.getResourceBundle());
        }
        if (!oldComp.equals(newComp)) {
            children.remove(oldComp);
            children.add(newComp);
        }
        newComp.setVisible(true);
    }

    /**
     * returns the previous perspective
     *
     * @param perspective
     * @return
     */
    private IPerspective<EventHandler<Event>, Event, Object> getPreviousPerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final String previousId = PerspectiveRegistry.getAndSetCurrentVisiblePerspective(perspective.getId());
        if (previousId == null) return perspective;
        return perspective.getId().equals(previousId) ? perspective : PerspectiveRegistry.findPerspectiveById(previousId);
    }

    /**
     * add perspective UI to workbench root component
     *
     * @param perspectiveLayout
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

    @SuppressWarnings("unchecked")
    private void handlePerspectiveInitMethod(final IAction<Event, Object> action,
                                             final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        if (perspective instanceof IPerspectiveView) {
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout());
            final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) perspective);

            if (perspectiveView.getType().equals(UIType.DECLARATIVE)) {
                handleDeclarativePerspective(perspective, layout, perspectiveView);
            } else {
                handleDefaultPerspective(perspective, layout, perspectiveView);
            }

            final IPerspectiveLayout<Node, Node> perspectiveLayout = (IPerspectiveLayout<Node, Node>) perspectiveView
                    .getIPerspectiveLayout();
            perspective.postInit(new FXPerspectiveHandler(this.launcher, layout, perspectiveLayout, perspective
                    .getComponentDelegateQueue()));
        } else {
            // TODO handle non UI Perspectives (not present 10.04.2012)
        }

        if (FXUtil.getTargetPerspectiveId(action.getTargetId()).equals(perspective.getId())) {
            this.log("3.4.3.1: perspective handle with custom action");
            perspective.handlePerspective(action);
        } // End if
        else {
            this.log("3.4.3.1: perspective handle with default >>init<< action");
            perspective.handlePerspective(new FXAction(perspective.getId(), perspective.getId(), "init", null));
        } // End else
    }

    private void handleDefaultPerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective, final FXComponentLayout layout, final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView) {
        // init default IPerspectiveLayout
        initLocalization(null, (AFXPerspective) perspective);
        FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                FXUtil.AFXPERSPECTIVE_PERSPECTIVE_LAYOUT, new FXPerspectiveLayout());
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, perspective, layout, perspectiveView.getResourceBundle());
    }

    private void handleDeclarativePerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective, final FXComponentLayout layout, final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView) {
        // init IPerspectiveLayout for FXML
        final URL url = getClass().getResource(perspectiveView.getViewLocation());
        initLocalization(url, (AFXPerspective) perspective);
        FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                FXUtil.AFXPERSPECTIVE_PERSPECTIVE_LAYOUT, new FXMLPerspectiveLayout(
                loadFXMLandSetController((AFXPerspective) perspectiveView, url)));
        FXUtil.invokeHandleMethodsByAnnotation(PostConstruct.class, perspective, layout,
                perspectiveView.getDocumentURL(), perspectiveView.getResourceBundle());
    }

    private void initLocalization(final URL url, final AFXPerspective perspective) {
        final String bundleLocation = perspective.getResourceBundleLocation();
        if (bundleLocation.equals("")) return;
        final String localeID = perspective.getLocaleID();
        perspective.initialize(url, ResourceBundle.getBundle(bundleLocation, FXUtil.getCorrectLocale(localeID)));

    }

    private Node loadFXMLandSetController(
            final AFXPerspective perspectiveView, final URL url) {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        if (perspectiveView.getResourceBundle() != null) {
            fxmlLoader.setResources(perspectiveView.getResourceBundle());
        }
        fxmlLoader.setLocation(url);
        fxmlLoader.setController(perspectiveView);
        try {
            return (Node) fxmlLoader.load();
        } catch (IOException e) {
            throw new MissingResourceException(
                    "fxml file not found --  place in resource folder and reference like this: uiDescriptionFile = \"/myUIFile.fxml\"",
                    perspectiveView.getViewLocation(), "");
        }
    }


    /**
     * get perspectives ui root container
     *
     * @param layout
     * @return the root Node
     */
    private Node getLayoutComponentFromPerspectiveLayout(final IPerspectiveLayout<? extends Node, Node> layout) {
        return layout.getRootComponent();
    }

    /**
     * set all child components to invisible
     *
     * @param children
     */
    private void hideChildren(final ObservableList<Node> children) {
        children.forEach(c -> {
            if (c.isVisible()) c.setVisible(false);
        });
    }

    /**
     * set all child components to invisible
     *
     * @param children
     */
    private void hideChildrenAndExecuteOnHide(final IPerspective<EventHandler<Event>, Event, Object> perspective, final IPerspective<EventHandler<Event>, Event, Object> previousPerspective, final ObservableList<Node> children) {
        hideChildren(children);
        if (previousPerspective != null && !previousPerspective.equals(perspective)) {
            final IPerspectiveView<Node, EventHandler<Event>, Event, Object> perspectiveView = ((IPerspectiveView<Node, EventHandler<Event>, Event, Object>) previousPerspective);
            final FXComponentLayout layout = new FXComponentLayout(this.getWorkbenchLayout());
            FXUtil.invokeHandleMethodsByAnnotation(OnHide.class, previousPerspective, layout,
                    perspectiveView.getDocumentURL(), perspectiveView.getResourceBundle());
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

    public final List<IPerspective<EventHandler<Event>, Event, Object>> getPerspectives() {
        return this.perspectives;
    }
}
