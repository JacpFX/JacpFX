/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [AFX2Workbench.java]
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
package org.jacp.javafx.rcp.workbench;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;
import org.jacp.api.annotations.Perspective;
import org.jacp.api.annotations.Stateless;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.IRootComponent;
import org.jacp.api.component.Injectable;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.api.coordinator.IComponentDelegator;
import org.jacp.api.coordinator.IMessageDelegator;
import org.jacp.api.coordinator.IPerspectiveCoordinator;
import org.jacp.api.dialog.Scope;
import org.jacp.api.handler.IComponentHandler;
import org.jacp.api.launcher.Launcher;
import org.jacp.api.util.OS;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.api.util.UIType;
import org.jacp.api.workbench.IWorkbench;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.action.FXActionListener;
import org.jacp.javafx.rcp.component.AComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacp.javafx.rcp.components.managedDialog.JACPManagedDialog;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;
import org.jacp.javafx.rcp.components.toolBar.JACPToolBar;
import org.jacp.javafx.rcp.coordinator.FXComponentDelegator;
import org.jacp.javafx.rcp.coordinator.FXMessageDelegator;
import org.jacp.javafx.rcp.coordinator.FXPerspectiveCoordinator;
import org.jacp.javafx.rcp.handler.FXWorkbenchHandler;
import org.jacp.javafx.rcp.perspective.AFXPerspective;
import org.jacp.javafx.rcp.util.*;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * represents the basic JavaFX2 workbench instance; handles perspectives and
 * components;
 *
 * @author Andy Moncsek, Patrick Symmangk
 */
public abstract class AFXWorkbench
        implements
        IWorkbench<Node, EventHandler<Event>, Event, Object>,
        IRootComponent<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> {

    private List<IPerspective<EventHandler<Event>, Event, Object>> perspectives;

    private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> componentHandler;
    private final IPerspectiveCoordinator<EventHandler<Event>, Event, Object> perspectiveCoordinator = new FXPerspectiveCoordinator();
    private final IComponentDelegator<EventHandler<Event>, Event, Object> componentDelegator = new FXComponentDelegator();
    private final IMessageDelegator<EventHandler<Event>, Event, Object> messageDelegator = new FXMessageDelegator();
    private final IWorkbenchLayout<Node> workbenchLayout = new FXWorkbenchLayout();
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private Launcher<?> launcher;
    private Stage stage;
    private GridPane root;
    private BorderPane baseLayoutPane;
    private StackPane absoluteRoot;
    private GridPane base;
    private Pane glassPane;
    private JACPModalDialog dimmer;

    /**
     * JavaFX2 specific start sequence
     *
     * @param stage
     * @throws Exception
     */
    public final void start(final Stage stage) throws Exception {
        this.stage = stage;
        TearDownHandler.registerBase(this);
        stage.setOnCloseRequest(arg0 -> {
            ShutdownThreadsHandler.shutdowAll();
            TearDownHandler.handleGlobalTearDown();
            Platform.exit();
        });
        this.log("1: init workbench");
        // init user defined workspace
        this.handleInitialLayout(new FXAction("TODO", "init"),
                this.getWorkbenchLayout());
        this.setBasicLayout(stage);

        this.postHandle(new FXComponentLayout(this.getWorkbenchLayout()
                .getMenu(), this.getWorkbenchLayout().getRegisteredToolbars(),
                this.glassPane));

        this.log("3: handle initialisation sequence");
        this.componentHandler = new FXWorkbenchHandler(this.launcher,
                this.workbenchLayout, this.root, this.perspectives);
        this.perspectiveCoordinator.setComponentHandler(this.componentHandler);
        this.componentDelegator.setComponentHandler(this.componentHandler);
        this.messageDelegator.setComponentHandler(this.componentHandler);
        this.handleInitialisationSequence();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    // TODO init method also defined in perspective!!!!
    public void init(final Launcher<?> launcher) {
        this.launcher = launcher;
        JACPManagedDialog.initManagedDialog(launcher);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final void initComponents(final IAction<Event, Object> action) {
        this.perspectives.forEach(perspective ->
                {
                    this.registerComponent(perspective);
                    this.log("3.4.1: register component: " + perspective.getName());
                    // TODO what if component removed an initialized later
                    // again?
                    this.log("3.4.2: create perspective menu");
                    if (perspective.isActive()) {
                        final Runnable r =   ()-> AFXWorkbench.this.componentHandler.initComponent(
                                new FXAction(perspective.getId(), perspective
                                        .getId(), "init", null), perspective);
                        if(Platform.isFxApplicationThread()) {
                             r.run();
                        } else {
                            Platform.runLater(r);
                                    
                        }
                        
                    }
                });
    }

    /**
     * handles sequence for workbench size, menu bar, tool bar and perspective
     * initialisation
     */
    private void handleInitialisationSequence() {
        Platform.runLater(() -> {
            AFXWorkbench.this.stage.show();
            // start perspective Observer worker thread
            // TODO create status daemon which observes
            // thread component on
            // failure and restarts if needed!!
            ((FXPerspectiveCoordinator) AFXWorkbench.this.perspectiveCoordinator)
                    .start();
            ((FXComponentDelegator) AFXWorkbench.this.componentDelegator)
                    .start();
            ((FXMessageDelegator) AFXWorkbench.this.messageDelegator)
                    .start();
            // handle perspectives
            AFXWorkbench.this.log("3.3: workbench init perspectives");
            AFXWorkbench.this.initComponents(null);

        });
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void handleInitialLayout(final IAction<Event, Object> action,
                                    final IWorkbenchLayout<Node> layout) {
        this.handleInitialLayout(action, layout, this.stage);
    }

    /**
     * JavaFX2 specific initialization method to create a workbench instance
     *
     * @param action, the initial event
     * @param layout, the workbench layout
     * @param stage, the JavaFX stage
     */
    public abstract void handleInitialLayout(
            final IAction<Event, Object> action,
            final IWorkbenchLayout<Node> layout, final Stage stage);

    /**
     * Handle menu and bar entries created in @see
     * {@link org.jacp.javafx.rcp.workbench.AFXWorkbench#handleInitialLayout(IAction, IWorkbenchLayout, Stage)}
     *
     * @param layout, the component layout
     */
    public abstract void postHandle(final FXComponentLayout layout);

    @Override
    /**
     * {@inheritDoc}
     */
    public final void registerComponent(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {

        this.handleMetaAnnotation(perspective);
        perspective.init(this.componentDelegator.getComponentDelegateQueue(),
                this.messageDelegator.getMessageDelegateQueue(),
                this.perspectiveCoordinator.getMessageQueue(),this.launcher);
        this.perspectiveCoordinator.addPerspective(perspective);
        this.componentDelegator.addPerspective(perspective);
        this.messageDelegator.addPerspective(perspective);
        PerspectiveRegistry.registerPerspective(perspective);
    }

    /**
     * set meta attributes defined in annotations
     *
     * @param perspective
     */
    private void handleMetaAnnotation(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final Perspective perspectiveAnnotation = perspective.getClass()
                .getAnnotation(Perspective.class);
        if (perspectiveAnnotation != null) {
            final String id = perspectiveAnnotation.id();
            if (id != null)
                FXUtil.setPrivateMemberValue(AComponent.class, perspective,
                        FXUtil.ACOMPONENT_ID, id);
            FXUtil.setPrivateMemberValue(AComponent.class, perspective,
                    FXUtil.ACOMPONENT_ACTIVE, perspectiveAnnotation.active());
            final String name = perspectiveAnnotation.name();
            if (name != null)
                FXUtil.setPrivateMemberValue(AComponent.class, perspective,
                        FXUtil.ACOMPONENT_NAME, name);
            this.log("register perspective with annotations : "
                    + perspectiveAnnotation.id());
            final String viewLocation = perspectiveAnnotation.viewLocation();
            if (viewLocation.length() > 1)
                FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                        FXUtil.IDECLARATIVECOMPONENT_VIEW_LOCATION,
                        perspectiveAnnotation.viewLocation());
            if (viewLocation.length() > 1)
                FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                        FXUtil.IDECLARATIVECOMPONENT_TYPE, UIType.DECLARATIVE);
            final String localeID = perspectiveAnnotation.localeID();
            if (localeID.length() > 1)
                FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                        FXUtil.IDECLARATIVECOMPONENT_LOCALE, localeID);
            final String resourceBundleLocation = perspectiveAnnotation
                    .resourceBundleLocation();
            if (resourceBundleLocation.length() > 1)
                FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                        FXUtil.IDECLARATIVECOMPONENT_BUNDLE_LOCATION,
                        resourceBundleLocation);
        }
    }



    @Override
    /**
     * {@inheritDoc}
     */
    public final void unregisterComponent(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                FXUtil.APERSPECTIVE_MQUEUE, null);
        this.perspectiveCoordinator.removePerspective(perspective);
        this.componentDelegator.removePerspective(perspective);
        this.messageDelegator.removePerspective(perspective);
        PerspectiveRegistry.removePerspective(perspective);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final FXWorkbenchLayout getWorkbenchLayout() {
        return (FXWorkbenchLayout) this.workbenchLayout;
    }

    @Override
    public IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, IAction<Event, Object>> getComponentHandler() {
        return this.componentHandler;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final void setPerspectives(
            final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives) {
        this.perspectives = perspectives;

    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final List<IPerspective<EventHandler<Event>, Event, Object>> getPerspectives() {
        return this.perspectives;
    }

    @Override
    public final IActionListener<EventHandler<Event>, Event, Object> getActionListener(
            final String targetId, final Object message) {
        return new FXActionListener(
                new FXAction("workbench", targetId, message, null),
                this.perspectiveCoordinator.getMessageQueue());
    }

    /**
     * set basic layout manager for workspace
     *
     * @param stage javafx.stage.Stage
     */

    // TODO: handle the custom decorator
    private void setBasicLayout(final Stage stage) {
        // the top most pane is a Stackpane
        this.base = new GridPane();
        this.absoluteRoot = new StackPane();
        this.baseLayoutPane = new BorderPane();
        this.stage = stage;

        if (OS.MAC.equals(OS.getOS())) {
            // OSX will always be DECORATED due to fullscreen option!
            stage.initStyle(StageStyle.DECORATED);
        } else {
            stage.initStyle((StageStyle) this.getWorkbenchLayout().getStyle());
        }

        this.initBaseLayout();
        this.initMenuLayout();
        this.initToolbarLayout();
        this.completeLayout();
    }

    private void completeLayout() {
        // fetch current workbenchsize
        final int x = this.getWorkbenchLayout().getWorkbenchSize().getX();
        final int y = this.getWorkbenchLayout().getWorkbenchSize().getY();

        this.absoluteRoot.getChildren().add(this.baseLayoutPane);
        this.absoluteRoot.setId(CSSUtil.CSSConstants.ID_ROOT);
        this.base.getChildren().add(absoluteRoot);
        LayoutUtil.GridPaneUtil.setFullGrow(Priority.ALWAYS, this.absoluteRoot);
        this.stage.setScene(new Scene(this.base, x, y));
        this.initCSS(this.stage.getScene());

        // new Layer for Menu Effects
        this.absoluteRoot.getChildren().add(this.glassPane);
        this.absoluteRoot.getChildren().add(this.dimmer);

    }

    private void initMenuLayout() {
        // add the menu if needed
        if (this.getWorkbenchLayout().isMenuEnabled()) {
            this.baseLayoutPane.setTop(this.getWorkbenchLayout().getMenu());
            this.getWorkbenchLayout().getMenu().setMenuDragEnabled(stage);
        }

    }

    private void initToolbarLayout() {
        // add toolbars in a specific order
        if (!this.getWorkbenchLayout().getRegisteredToolbars().isEmpty()) {

            // add another Layer to hold all the toolbars
            final BorderPane toolbarPane = new BorderPane();
            this.baseLayoutPane.setCenter(toolbarPane);

            final Map<ToolbarPosition, JACPToolBar> registeredToolbars = this
                    .getWorkbenchLayout().getRegisteredToolbars();

            for (Entry<ToolbarPosition, JACPToolBar> entry : registeredToolbars
                    .entrySet()) {
                final ToolbarPosition position = entry.getKey();
                final JACPToolBar toolBar = entry.getValue();
                this.assignCorrectToolBarLayout(position, toolBar, toolbarPane);
            }

            // add root to the center
            toolbarPane.setCenter(this.root);
            toolbarPane.getStyleClass().add(
                    CSSUtil.CSSConstants.CLASS_DARK_BORDER);

        } else {
            // no toolbars -> no special Layout needed
            this.baseLayoutPane.setCenter(this.root);
        }

    }

    private void initCSS(final Scene scene) {
        scene.getStylesheets().addAll(
                AFXWorkbench.class.getResource("/styles/jacp-styles.css")
                        .toExternalForm());
    }

    private void initBaseLayout() {
        this.initRootPane();
        this.initDimmer();
        this.initGlassPane();
    }

    private void initRootPane() {
        // root is top most pane
        this.root = new GridPane();
        this.root.setCache(true);
        this.root.setId(CSSUtil.CSSConstants.ID_ROOT_PANE);

    }

    private void initDimmer() {
        JACPModalDialog.initDialog(this.baseLayoutPane);
        this.dimmer = JACPModalDialog.getInstance();
        this.dimmer.setVisible(false);

        // add some FX
        final GaussianBlur blur = new GaussianBlur();
        blur.setRadius(0);
        this.baseLayoutPane.setEffect(blur);
    }

    private void initGlassPane() {
        // Pane for custom elements added to the glasspane
        this.glassPane = this.getWorkbenchLayout().getGlassPane();
        this.glassPane.autosize();
        this.glassPane.setVisible(false);
        this.glassPane.setPrefSize(0, 0);
    }

    /**
     * set toolBars to correct position
     *
     * @param position
     * @param bar
     * @param pane
     */
    private void assignCorrectToolBarLayout(final ToolbarPosition position,
                                            final ToolBar bar, final BorderPane pane) {
        switch (position) {
            case NORTH:
                pane.setTop(bar);
                break;
            case SOUTH:
                pane.setBottom(bar);
                break;
            case EAST:
                bar.setOrientation(Orientation.VERTICAL);
                pane.setRight(bar);
                break;
            case WEST:
                bar.setOrientation(Orientation.VERTICAL);
                pane.setLeft(bar);
                break;
        }
    }

    private void log(final String message) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine(">> " + message);
        }
    }
}
