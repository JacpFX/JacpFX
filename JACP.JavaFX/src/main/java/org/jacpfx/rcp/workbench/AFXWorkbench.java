/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [AFX2Workbench.java]
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
package org.jacpfx.rcp.workbench;

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
import org.jacpfx.api.coordinator.ICoordinator;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.IRootComponent;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.componentLayout.IWorkbenchLayout;
import org.jacpfx.api.context.Context;
import org.jacpfx.api.delegator.IComponentDelegator;
import org.jacpfx.api.delegator.IMessageDelegator;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.util.OS;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.api.workbench.IBase;
import org.jacpfx.rcp.coordinator.MessageCoordinator;
import org.jacpfx.rcp.delegator.ComponentDelegator;
import org.jacpfx.rcp.delegator.MessageDelegator;
import org.jacpfx.rcp.message.FXMessage;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.components.managedDialog.JACPManagedDialog;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.context.JACPContext;
import org.jacpfx.rcp.context.JACPContextImpl;
import org.jacpfx.rcp.handler.PerspectiveHandler;
import org.jacpfx.rcp.perspective.AFXPerspective;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        IBase<EventHandler<Event>, Event, Object>,
        IRootComponent<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> {

    private List<IPerspective<EventHandler<Event>, Event, Object>> perspectives;

    private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private ICoordinator<EventHandler<Event>, Event, Object> messageCoordinator;
    private final IComponentDelegator<EventHandler<Event>, Event, Object> componentDelegator = new ComponentDelegator();
    private final IMessageDelegator<EventHandler<Event>, Event, Object> messageDelegator = new MessageDelegator();
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
    private JACPContext context;
    private FXWorkbench handle;

    /**
     * JavaFX specific start sequence
     *
     * @param stage, The JavaFX stage
     */
    private void start(final Stage stage) {
        this.stage = stage;
        this.registerTeardownActions();
        this.log("1: init workbench");

        initWorkbenchHandle(stage);

        this.log("3: handle initialisation sequence");
        this.perspectives = WorkbenchUtil.getInstance(launcher).createPerspectiveInstances(getWorkbenchAnnotation());
        if (perspectives == null) return;

        this.initSubsystem();
        this.handleInitialisationSequence();
    }

    private void initWorkbenchHandle(final Stage stage) {
        // init user defined workspace
        getWorkbenchHandle().handleInitialLayout(new FXMessage("TODO", "init"),
                this.getWorkbenchLayout(), stage);
        this.setBasicLayout(stage);

        getWorkbenchHandle().postHandle(new FXComponentLayout(this.getWorkbenchLayout()
                .getMenu(), this.getWorkbenchLayout().getRegisteredToolbars(),
                this.glassPane));
    }

    private void registerTeardownActions() {
        TearDownHandler.registerBase(this);
        stage.setOnCloseRequest(arg0 -> {
            ShutdownThreadsHandler.shutdowAll();
            TearDownHandler.handleGlobalTearDown();
            Platform.exit();
        });
    }

    private void initSubsystem() {
        this.componentHandler = new PerspectiveHandler(this.launcher,
                this.workbenchLayout, this.root);
        this.messageCoordinator.setPerspectiveHandler(this.componentHandler);
        this.componentDelegator.setPerspectiveHandler(this.componentHandler);
        this.messageDelegator.setPerspectiveHandler(this.componentHandler);
    }


    @Override
    /**
     * {@inheritDoc}
     */
    public void init(final Launcher<?> launcher, Object root) {
        this.launcher = launcher;
        JACPManagedDialog.initManagedDialog(launcher);
        final Workbench annotation = getWorkbenchAnnotation();
        this.messageCoordinator = new MessageCoordinator(annotation.id(),this.launcher);
        this.messageCoordinator.setDelegateQueue(this.messageDelegator.getMessageDelegateQueue());
        this.context = new JACPContextImpl(annotation.id(), annotation.name(), this.messageCoordinator.getMessageQueue());
        FXUtil.performResourceInjection(this.getWorkbenchHandle(), this.context);
        start(Stage.class.cast(root));
    }


    private Workbench getWorkbenchAnnotation() {
        FXWorkbench handler = this.getWorkbenchHandle();
        return handler.getClass().getAnnotation(Workbench.class);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final void initComponents(final Message<Event, Object> action) {
        this.perspectives.forEach(this::initPerspective);
    }

    private void initPerspective(IPerspective<EventHandler<Event>, Event, Object> perspective) {
        this.registerComponent(perspective);
        this.log("3.4.1: register component: " + perspective.getContext().getName());
        // TODO what if component removed an initialized later
        // again?
        this.log("3.4.2: create perspective menu");
        if (perspective.getContext().isActive()) {
            final Runnable r = () -> AFXWorkbench.this.componentHandler.initComponent(
                    new FXMessage(perspective.getContext().getId(), perspective
                            .getContext().getId(), "init", null), perspective);
            if (Platform.isFxApplicationThread()) {
                r.run();
            } else {
                Platform.runLater(r);

            }

        }
    }

    /**
     * handles sequence for workbench size, menu bar, tool bar and perspective
     * initialisation
     */
    private void handleInitialisationSequence() {
        AFXWorkbench.this.stage.show();
        // start perspective Observer worker thread
        // TODO create status daemon which observes
        // thread component on
        // failure and restarts if needed!!
        ((MessageCoordinator) AFXWorkbench.this.messageCoordinator)
                .start();
        ((ComponentDelegator) AFXWorkbench.this.componentDelegator)
                .start();
        ((MessageDelegator) AFXWorkbench.this.messageDelegator)
                .start();
        // handle perspectives
        AFXWorkbench.this.log("3.3: workbench init perspectives");
        AFXWorkbench.this.initComponents(null);
    }


    @Override
    /**
     * {@inheritDoc}
     */
    public final void registerComponent(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final String perspectiveId = WorkbenchUtil.getPerspectiveIdFromAnnotation(perspective);
        final MessageCoordinator messageCoordinator = new MessageCoordinator(perspectiveId,this.launcher);
        messageCoordinator.setDelegateQueue(this.messageDelegator.getMessageDelegateQueue());
        messageCoordinator.setPerspectiveHandler(this.componentHandler);
        // use compleatableFuture
        perspective.init(this.componentDelegator.getComponentDelegateQueue(),
                this.messageDelegator.getMessageDelegateQueue(),
                messageCoordinator, this.launcher);
        messageCoordinator.start();
        WorkbenchUtil.handleMetaAnnotation(perspective, this.getWorkbenchAnnotation().id());
        addComponent(perspective);
    }

    @Override
    public final void addComponent(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        PerspectiveRegistry.registerPerspective(perspective);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    // TODO remove this!!
    public final void unregisterComponent(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        FXUtil.setPrivateMemberValue(AFXPerspective.class, perspective,
                FXUtil.APERSPECTIVE_MQUEUE, null);
        PerspectiveRegistry.removePerspective(perspective);
    }

    @Override
    public final void removeAllCompnents() {
        this.perspectives.forEach(this::unregisterComponent);
        this.perspectives.clear();
    }


    /**
     * {@inheritDoc}
     */
    private FXWorkbenchLayout getWorkbenchLayout() {
        return (FXWorkbenchLayout) this.workbenchLayout;
    }

    @Override
    public IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> getComponentHandler() {
        return this.componentHandler;
    }


    @Override
    /**
     * {@inheritDoc}
     */
    public final List<IPerspective<EventHandler<Event>, Event, Object>> getPerspectives() {
        return this.perspectives;
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
            stage.initStyle(this.getWorkbenchLayout().getStyle());
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
        SceneUtil.setScene(this.stage.getScene());

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

            for (Iterator<Entry<ToolbarPosition, JACPToolBar>> iterator = registeredToolbars
                    .entrySet().iterator(); iterator.hasNext(); ) {
                Entry<ToolbarPosition, JACPToolBar> entry = iterator.next();
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
     * @param position, The toolbar position
     * @param bar, the affected toolbar
     * @param pane, the root pane
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

    @Override
    public Context<EventHandler<Event>, Object> getContext() {
        return context;
    }

    private FXWorkbench getWorkbenchHandle() {
        return handle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FXWorkbench getComponentHandle() {
        return this.handle;
    }

    @Override
    public <X extends Injectable> void setComponentHandle(X handle) {
        this.handle = (FXWorkbench) handle;
    }
}
