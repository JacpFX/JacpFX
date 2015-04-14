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
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.RootComponent;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.coordinator.Coordinator;
import org.jacpfx.api.delegator.ComponentDelegator;
import org.jacpfx.api.delegator.MessageDelegator;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.workbench.Base;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.components.managedFragment.ManagedFragment;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.JacpContextImpl;
import org.jacpfx.rcp.coordinator.MessageCoordinator;
import org.jacpfx.rcp.delegator.ComponentDelegatorImpl;
import org.jacpfx.rcp.delegator.MessageDelegatorImpl;
import org.jacpfx.rcp.handler.PerspectiveHandlerImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.perspective.AFXPerspective;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * represents the basic JavaFX workbench instance; handles perspective and
 * component;
 *
 * @author Andy Moncsek, Patrick Symmangk
 */
public abstract class AFXWorkbench
        implements
        Base<Node,EventHandler<Event>, Event, Object>,
        RootComponent<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> {

    private final ComponentDelegator<EventHandler<Event>, Event, Object> componentDelegator = new ComponentDelegatorImpl();
    private final MessageDelegator<EventHandler<Event>, Event, Object> messageDelegator = new MessageDelegatorImpl();
    private final WorkbenchLayout<Node> workbenchLayout = new FXWorkbenchLayout();
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives;
    private ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private Coordinator<EventHandler<Event>, Event, Object> messageCoordinator;
    private WorkbenchDecorator workbenchDecorator;
    private Launcher<?> launcher;
    private Stage stage;
    private Context context;
    private FXWorkbench handle;

    /**
     * JavaFX specific start sequence
     *
     * @param stage, The JavaFX stage
     */
    private void start(final Stage stage) {
        this.stage = stage;
        DimensionUtil.init(stage);
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
        this.handle.handleInitialLayout(new MessageImpl(this.context.getId(), "init"),
                this.getWorkbenchLayout(), stage);
        this.workbenchDecorator = new DefaultWorkbenchDecorator(this.getWorkbenchLayout());
        this.workbenchDecorator.initBasicLayout(stage);

        this.handle.postHandle(new FXComponentLayout(this.getWorkbenchLayout()
                .getMenu(), this.workbenchDecorator.getGlassPane(), null, this.getContext().getId()));
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
        this.componentHandler = new PerspectiveHandlerImpl(this.launcher,
                this.workbenchLayout, this.workbenchDecorator.getRoot());
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
        ManagedFragment.initManagedFragment(launcher);
        final Workbench annotation = getWorkbenchAnnotation();
        this.messageCoordinator = new MessageCoordinator(annotation.id(), this.launcher);
        this.messageCoordinator.setDelegateQueue(this.messageDelegator.getMessageDelegateQueue());
        this.context = new JacpContextImpl(annotation.id(), annotation.name(), this.messageCoordinator.getMessageQueue());
        FXUtil.performResourceInjection(this.handle, this.context);
        start(Stage.class.cast(root));
        GlobalMediator.getInstance().handleWorkbenchToolBarButtons(annotation.id(), true);
        logger.info("INIT");
    }

    private Workbench getWorkbenchAnnotation() {
        return this.handle.getClass().getAnnotation(Workbench.class);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final void initComponents(final Message<Event, Object> action) {
        this.perspectives.forEach(this::initPerspective);
        final List<Perspective<Node, EventHandler<Event>, Event, Object>> activeSequentialPerspectiveList = this.perspectives
                .stream()
                .sequential()
                .filter(p -> p.getContext() != null && p.getContext().isActive())
                .collect(Collectors.toList());
        if (!activeSequentialPerspectiveList.isEmpty()) {
            GlobalMediator.getInstance().handleToolBarButtons(activeSequentialPerspectiveList.get(activeSequentialPerspectiveList.size() - 1), true);
        }

    }

    private void initPerspective(Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        this.registerComponent(perspective);
        this.log("3.4.1: register component: " + perspective.getContext().getName());
        final CountDownLatch waitForInit = new CountDownLatch(1);
        this.log("3.4.2: init perspective");
        if (perspective.getContext().isActive()) {
            final Runnable r = () -> {
                AFXWorkbench.this.componentHandler.initComponent(
                        new MessageImpl(perspective.getContext().getId(), perspective
                                .getContext().getId(), "init", null), perspective);
                waitForInit.countDown();
            };
            if (Platform.isFxApplicationThread()) {
                r.run();
            } else {
                Platform.runLater(r);

            }
            try {
                // wait for possible async execution
                waitForInit.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        ((Thread) AFXWorkbench.this.messageCoordinator)
                .start();
        ((Thread) AFXWorkbench.this.componentDelegator)
                .start();
        ((Thread) AFXWorkbench.this.messageDelegator)
                .start();
        // handle perspective
        AFXWorkbench.this.log("3.3: workbench init perspective");
        AFXWorkbench.this.initComponents(null);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final void registerComponent(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        final String perspectiveId = PerspectiveUtil.getPerspectiveIdFromAnnotation(perspective);
        final MessageCoordinator messageCoordinatorLocal = new MessageCoordinator(perspectiveId, this.launcher);
        messageCoordinatorLocal.setDelegateQueue(this.messageDelegator.getMessageDelegateQueue());
        messageCoordinatorLocal.setPerspectiveHandler(this.componentHandler);
        // use compleatableFuture
        perspective.init(this.componentDelegator.getComponentDelegateQueue(),
                this.messageDelegator.getMessageDelegateQueue(),
                messageCoordinatorLocal, this.launcher);
        messageCoordinatorLocal.start();
        WorkbenchUtil.handleMetaAnnotation(perspective, this.getWorkbenchAnnotation().id());
        addComponent(perspective);
    }

    @Override
    public final void addComponent(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        PerspectiveRegistry.registerPerspective(perspective);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    // TODO remove this!!
    public final void unregisterComponent(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
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
    public ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> getComponentHandler() {
        return this.componentHandler;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public final List<Perspective<Node, EventHandler<Event>, Event, Object>> getPerspectives() {
        return this.perspectives;
    }



    private void log(final String message) {
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine(">> " + message);
        }
    }

    @Override
    public JacpContext getContext() {
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
