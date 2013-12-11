package org.jacpfx.rcp.coordinator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.coordinator.ICoordinator;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.delegator.DelegateDTO;
import org.jacpfx.rcp.message.FXMessage;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.PerspectiveUtil;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Andy Moncsek on 09.12.13.
 */
public class MessageCoordinator extends AFXCoordinator implements
        ICoordinator<EventHandler<Event>, Event, Object> {
    private IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private final BlockingQueue<IDelegateDTO<Event, Object>> delegateQueue;
    private final String parentId;
    private final Launcher<?> launcher;

    public MessageCoordinator(final BlockingQueue<IDelegateDTO<Event, Object>> delegateQueue,
                              final String parentId,
                              final Launcher<?> launcher) {
        super("MessageCoordinator");
        this.delegateQueue = delegateQueue;
        this.parentId = parentId;
        this.launcher = launcher;
    }

    @Override
    public void handleMessage(final String targetId, final Message<Event, Object> message) {
        if (!FXUtil.isLocalMessage(targetId)) {
            // this must be a component message
            handleGlobalComponentMessage(targetId, message);
        } else {
            // unclear if component or perspective message
            findCorrectTargetAndProceed(targetId, message);
        }
    }

    private void findCorrectTargetAndProceed(final String targetId, final Message<Event, Object> message) {
        // 1. test if perspective itself
        if (parentId.equalsIgnoreCase(targetId)) {
            // this is a message to current perspective
            handleCurrentPerspective(targetId, message);
            return;
        }

        // 2. check if it is an active component in registry, active components must have active perspectives
        final ISubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentById(targetId);
        if (component != null) {
            // found an active component
            // check if is in current perspective
            if (parentId.equalsIgnoreCase(component.getParentId())) {
                handleActive(component, message);
            } else {
                // convert to global message and delegate to correct perspective
                final String globalTarget = component.getParentId().concat(targetId);
                delegateMessageToCorrectPerspective(new DelegateDTO(globalTarget, new FXMessage(message.getSourceId(), globalTarget, message.getMessageBody(), message.getSourceEvent())));
            }

            return;
        }

        // 3. check if it is a perspective
        final IPerspective<EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        if (perspective != null) {
            // this is a perspective
            // delegate message to perspective, mark in dto that it is a perspective
            delegateMessageToCorrectPerspective(new DelegateDTO(targetId, true, message));
            return;
        }

        // 4. check if it is an inactive component in perspective
        // first find possible parent perspective
        final IPerspective<EventHandler<Event>, Event, Object> parentPerspectiveByComponentId = PerspectiveRegistry.findParentPerspectiveByComponentId(targetId);
        if (parentPerspectiveByComponentId != null) {
            // invoke perspective init with default message and wait
            // create global id
            final String globalTarget = parentPerspectiveByComponentId.getContext().getId().concat(targetId);
            delegateMessageToCorrectPerspective(new DelegateDTO(globalTarget, new FXMessage(message.getSourceId(), globalTarget, message.getMessageBody(), message.getSourceEvent())));
            return;
        }

    }

    private void handleCurrentPerspective(final String targetId, final Message<Event, Object> message) {
        final IPerspective<EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        Platform.runLater(() -> this.perspectiveHandler
                .handleAndReplaceComponent(
                        message, perspective) // End runnable
        ); // End runlater
    }


    private void handleGlobalComponentMessage(final String targetId, final Message<Event, Object> message) {
        final String parentMessageId = FXUtil.getParentFromId(targetId);
        if (parentId.equalsIgnoreCase(parentMessageId)) {
            // this is a message to local component in current perspective
            handleMessageToComponentInCurrentPerspective(targetId, message);
        } else {
            // this must be a message in different perspective
            delegateMessageToCorrectPerspective(new DelegateDTO(targetId, message));
        }
    }

    private void handleMessageToComponentInCurrentPerspective(final String targetId, final Message<Event, Object> message) {
        final ISubComponent<EventHandler<Event>, Event, Object> targetComponent = getTargetComponentInCurrentPerspective(targetId, message);
        if (targetComponent.getContext().isActive()) {
            // this is an active component
            handleActive(targetComponent, message);
        } else {
            // this component must be activated
            handleInActive(targetComponent, message);
        }

    }

    private void delegateMessageToCorrectPerspective(final DelegateDTO dto) {
        try {
            this.delegateQueue.put(dto);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }
    }

    /**
     * Returns the target component by targetId specified in message.
     *
     * @param targetId
     * @param action
     * @return
     */
    private ISubComponent<EventHandler<Event>, Event, Object> getTargetComponentInCurrentPerspective(final String targetId,
                                                                                                     final Message<Event, Object> action) {
        ISubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentById(targetId);
        if (component != null) {
            // component is active
            return component;
        } else {
            // start inactive component
            component = PerspectiveUtil.getInstance(this.launcher).createSubcomponentById(targetId);
        }
        if (component == null) throw new ComponentNotFoundException(
                "invalid component id. Source: "
                        + action.getSourceId() + " target: "
                        + action.getTargetId());
        return component;
    }

    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void handleActive(P component, Message<Event, Object> action) {
        this.componentHandler.handleAndReplaceComponent(action,
                (ISubComponent<EventHandler<Event>, Event, Object>) component);
    }

    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void handleInActive(P component, Message<Event, Object> action) {
        component.getContext().setActive(true);
        component.setStarted(true);
        this.componentHandler.initComponent(action,
                (ISubComponent<EventHandler<Event>, Event, Object>) component);
    }

    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> IComponentHandler<P, Message<Event, Object>> getComponentHandler() {
        return null;
    }

    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(IComponentHandler<P, Message<Event, Object>> handler) {
        this.componentHandler = (IComponentHandler<ISubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

    }

    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void setPerspectiveHandler(IComponentHandler<P, Message<Event, Object>> handler) {
        this.perspectiveHandler = (IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;
    }
}
