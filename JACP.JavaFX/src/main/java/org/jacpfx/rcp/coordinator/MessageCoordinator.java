package org.jacpfx.rcp.coordinator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.Component;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.coordinator.Coordinator;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.delegator.DelegateDTOImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.PerspectiveUtil;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Andy Moncsek on 09.12.13.
 */
public class MessageCoordinator extends ACoordinator implements
        Coordinator<EventHandler<Event>, Event, Object> {
    private ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private ComponentHandler<Perspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private BlockingQueue<DelegateDTO<Event, Object>> delegateQueue;
    private final String parentId;
    private final Launcher<?> launcher;
    private final String seperator = ".";

    public MessageCoordinator(final String parentId,
                              final Launcher<?> launcher) {
        super("MessageCoordinator");
        this.parentId = parentId;
        this.launcher = launcher;
    }

    @Override
    public void handleMessage(final String targetId, final Message<Event, Object> message) {
        final MessageCoordinatorExecutionResult result = executeMessageHandling(targetId, message);
        switch (result.getState()) {
            case HANDLE_ACTIVE:
                handleActive(result.getTargetComponent(), result.getMessage());
                break;
            case HANDLE_INACTIVE:
                handleInActive(result.getTargetComponent(), result.getParentPerspective(), result.getMessage());
                break;
            case HANDLE_CURRENT_PERSPECTIVE:
                handleCurrentPerspective(result.getTargetId(), result.getMessage());
                break;
            case DELEGATE:
                delegateMessageToCorrectPerspective(result.getDto());
                break;
            default:
                throw new ComponentNotFoundException("no valid component found"); // TODO handle exception correctly
        }
    }

    private void handleCurrentPerspective(final String targetId, final Message<Event, Object> message) {
        final Perspective<EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        Platform.runLater(() -> this.perspectiveHandler
                .handleAndReplaceComponent(
                        message, perspective) // End runnable
        ); // End runlater
    }

    private <P extends Component<EventHandler<Event>, Object>> void handleActive(P component, Message<Event, Object> message) {
        this.componentHandler.handleAndReplaceComponent(message,
                (SubComponent<EventHandler<Event>, Event, Object>) component);
    }

    private <P extends Component<EventHandler<Event>,Object>> void handleInActive(P component, final Perspective<EventHandler<Event>, Event, Object> parentPerspective, Message<Event, Object> message) {
        component.getContext().setActive(true);
        component.setStarted(true);
        parentPerspective.addComponent((SubComponent<EventHandler<Event>, Event, Object>) component);
        this.componentHandler.initComponent(message,
                (SubComponent<EventHandler<Event>, Event, Object>) component);
    }

    private void delegateMessageToCorrectPerspective(final DelegateDTOImpl dto) {
        try {
            this.delegateQueue.put(dto);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //TODO handle exception global
        }
    }

    private MessageCoordinatorExecutionResult executeMessageHandling(final String targetId, final Message<Event, Object> message) {
        if (!FXUtil.isLocalMessage(targetId)) {
            // this must be a component message
            return handleGlobalComponentMessage(targetId, message);
        } else {
            // unclear if component or perspective message
            return findCorrectTargetAndProceed(targetId, message);
        }
    }

    private MessageCoordinatorExecutionResult findCorrectTargetAndProceed(final String targetId, final Message<Event, Object> message) {
        // 1. test if perspective itself
        if (parentId.equalsIgnoreCase(targetId)) {
            // this is a message to current perspective
            return new MessageCoordinatorExecutionResult(targetId, message, MessageCoordinatorExecutionResult.State.HANDLE_CURRENT_PERSPECTIVE);

        }

        // 2. check if it is an active component in registry, active components must have active perspectives
        final SubComponent<EventHandler<Event>, Event, Object> targetComponent = ComponentRegistry.findComponentById(targetId);
        if (targetComponent != null) {
            // found an active component
            // check if is in current perspective
            if (parentId.equalsIgnoreCase(targetComponent.getParentId())) {
                return new MessageCoordinatorExecutionResult(targetComponent, message, MessageCoordinatorExecutionResult.State.HANDLE_ACTIVE);
            } else {
                // convert to global message and delegate to correct perspective
                final String globalTarget = targetComponent.getParentId().concat(seperator).concat(targetId);
                return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(globalTarget, new MessageImpl(message.getSourceId(), globalTarget, message.getMessageBody(), message.getSourceEvent())), MessageCoordinatorExecutionResult.State.DELEGATE);
            }

        }

        // 3. check if it is a perspective
        final Perspective<EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        if (perspective != null) {
            // this is a perspective
            // delegate message to perspective, mark in dto that it is a perspective
            return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(targetId, true, message), MessageCoordinatorExecutionResult.State.DELEGATE);

        }

        // 4. check if it is an inactive component in perspective
        // first find possible parent perspective
        final Perspective<EventHandler<Event>, Event, Object> parentPerspectiveByComponentId = PerspectiveRegistry.findParentPerspectiveByComponentId(targetId);
        if (parentPerspectiveByComponentId != null) {
            // invoke perspective init with default message and wait
            // create global id
            final String globalTarget = parentPerspectiveByComponentId.getContext().getId().concat(seperator).concat(targetId);
            return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(globalTarget, new MessageImpl(message.getSourceId(), globalTarget, message.getMessageBody(), message.getSourceEvent())), MessageCoordinatorExecutionResult.State.DELEGATE);

        }
        return new MessageCoordinatorExecutionResult(MessageCoordinatorExecutionResult.State.ERROR);
    }

    private MessageCoordinatorExecutionResult handleGlobalComponentMessage(final String targetId, final Message<Event, Object> message) {
        final String parentMessageId = FXUtil.getParentFromId(targetId);
        if (parentId.equalsIgnoreCase(parentMessageId)) {
            // this is a message to local component in current perspective
            return getTargetComponentInCurrentPerspective(targetId, message);
        } else {
            // this must be a message in different perspective
            return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(targetId, message), MessageCoordinatorExecutionResult.State.DELEGATE);
        }
    }


    /**
     * Returns the target component by targetId specified in message.
     *
     * @param targetId
     * @param message
     * @return
     */
    private MessageCoordinatorExecutionResult getTargetComponentInCurrentPerspective(final String targetId,
                                                                                     final Message<Event, Object> message) {
        SubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentById(targetId);
        if (component != null) {
            // component is active
            return new MessageCoordinatorExecutionResult(component, message, MessageCoordinatorExecutionResult.State.HANDLE_ACTIVE);
        } else {
            // start inactive component
            component = PerspectiveUtil.getInstance(this.launcher).createSubcomponentById(targetId);
        }
        if (component == null) throw new ComponentNotFoundException(
                "invalid component id. Source: "
                        + message.getSourceId() + " target: "
                        + message.getTargetId());

        return findParentPerspectiveAndRegisterComponent(component, message, targetId);
    }

    private MessageCoordinatorExecutionResult findParentPerspectiveAndRegisterComponent(final SubComponent<EventHandler<Event>, Event, Object> component, final Message<Event, Object> message, final String targetId) {
        final Perspective<EventHandler<Event>, Event, Object> parentPerspective = PerspectiveRegistry.findParentPerspectiveByComponentId(FXUtil.getTargetComponentId(targetId));
        if (parentPerspective == null)
            throw new ComponentNotFoundException("no valid perspective for component " + targetId + " found");
        parentPerspective.registerComponent(component);
        return new MessageCoordinatorExecutionResult(component, parentPerspective, message, MessageCoordinatorExecutionResult.State.HANDLE_INACTIVE);
    }


    @Override
    public <P extends Component<EventHandler<Event>,Object>> void setComponentHandler(ComponentHandler<P, Message<Event, Object>> handler) {
        this.componentHandler = (ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

    }

    @Override
    public <P extends Component<EventHandler<Event>,Object>> void setPerspectiveHandler(ComponentHandler<P, Message<Event, Object>> handler) {
        this.perspectiveHandler = (ComponentHandler<Perspective<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;
    }

    @Override
    public void setDelegateQueue(BlockingQueue<DelegateDTO<Event, Object>> delegateQueue) {
        this.delegateQueue = delegateQueue;
    }
}
