package org.jacpfx.rcp.coordinator;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.Component;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.coordinator.Coordinator;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.delegator.DelegateDTOImpl;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.PerspectiveUtil;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * The Message Coordinator checks the message target and delegates the message to component/perspective for correct handling.
 * Created by Andy Moncsek on 09.12.13.
 */
public class MessageCoordinator extends Thread implements
        Coordinator<EventHandler<Event>, Event, Object> {
    private ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>> componentHandler;
    private ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private BlockingQueue<DelegateDTO<Event, Object>> delegateQueue;
    private final BlockingQueue<Message<Event, Object>> messages = new SynchronousQueue<>();
    private final String parentId;
    private final Launcher<?> launcher;
    private static final String seperator = ".";

    public MessageCoordinator(final String parentId,
                              final Launcher<?> launcher) {
        super("MessageCoordinator");
        ShutdownThreadsHandler.registerThread(this);
        this.parentId = parentId;
        this.launcher = launcher;
    }


    @Override
    public final void run() {
        while (!Thread.interrupted()) {
            try {
                final Message<Event, Object> message = this.messages.take();
                this.handleMessage(message.getTargetId(), message);
            } catch (InterruptedException e) {
                // this can happen on application shutdown
                break;
            }catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }


    @Override
    public final BlockingQueue<Message<Event, Object>> getMessageQueue() {
        return this.messages;
    }

    @Override
    public final void handleMessage(final String targetId, final Message<Event, Object> message) {
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
                throw new ComponentNotFoundException("no valid component found for id: " + targetId + " found");
        }
    }

    private void handleCurrentPerspective(final String targetId, final Message<Event, Object> message) {
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        Platform.runLater(() -> this.perspectiveHandler
                .handleAndReplaceComponent(
                        message, perspective) // End runnable
        ); // End runlater
    }

    private void handleActive(final SubComponent<EventHandler<Event>, Event, Object> component, Message<Event, Object> message) {
        this.componentHandler.handleAndReplaceComponent(message, component);
    }

    private void handleInActive(final SubComponent<EventHandler<Event>, Event, Object> component, final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective, Message<Event, Object> message) {
        final JacpContext<EventHandler<Event>, Object> context = component.getContext();
        context.setActive(true);
        component.setStarted(true);
        parentPerspective.addComponent(component);
        this.componentHandler.initComponent(message, component);
    }

    private void delegateMessageToCorrectPerspective(final DelegateDTOImpl dto) {
        try {
            this.delegateQueue.put(dto);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private MessageCoordinatorExecutionResult executeMessageHandling(final String targetId, final Message<Event, Object> message) {
        if (!FXUtil.isLocalMessage(targetId)) {
            // this must be a component message
            return handleGlobalComponentMessage(targetId, message);
        } else {
            // unclear if it is a component- or a perspective-message
            return findCorrectTargetAndProceed(targetId, message);
        }
    }

    private MessageCoordinatorExecutionResult findCorrectTargetAndProceed(final String targetId, final Message<Event, Object> message) {
        // 1. test if perspective itself
        if (parentId.equalsIgnoreCase(targetId)) {
            // this is a message to current perspective
            return new MessageCoordinatorExecutionResult(targetId, message, MessageCoordinatorExecutionResult.State.HANDLE_CURRENT_PERSPECTIVE);

        }
        // 2. check if it is an active component in registry, active component must have active perspective
        final SubComponent<EventHandler<Event>, Event, Object> targetComponent = ComponentRegistry.findComponentByQualifiedId(FXUtil.getQualifiedComponentId(parentId, targetId));
        if (targetComponent != null) {
            // found an active component
            return new MessageCoordinatorExecutionResult(targetComponent, message, MessageCoordinatorExecutionResult.State.HANDLE_ACTIVE);
        }
        // 3. check if it is a perspective, all perspective (even inactive ones are registerd)
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        if (perspective != null) {
            // this is a perspective
            // delegate message to perspective, mark in dto that it is a perspective
            return new MessageCoordinatorExecutionResult(new DelegateDTOImpl(targetId, true, message), MessageCoordinatorExecutionResult.State.DELEGATE);

        }
        // 4. check if it is an inactive component in perspective
        boolean exists = PerspectiveRegistry.perspectiveContainsComponentIdInAnnotation(this.parentId, targetId);
        if (exists) {
            // create global id
            final String globalTarget = this.parentId.concat(seperator).concat(targetId);
            return createComponentInstanceAndRegister(globalTarget, message);
        }
        return new MessageCoordinatorExecutionResult(MessageCoordinatorExecutionResult.State.ERROR);
    }

    /**
     * Handles messages with fully qualified value like "parentId.componentId"
     *
     * @param targetId the fully qualified id
     * @param message  the message
     * @return a MessageCoordinatorExecutionResult with values "HANDLE_ACTIVE" , "HANDLE_INACTIVE", "DELEGATE", or "ERROR"
     */
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
     * @param targetId the fully qualified id
     * @param message  the message
     * @return a MessageCoordinatorExecutionResult  with value "HANDLE_ACTIVE"  or "HANDLE_INACTIVE"
     */
    private MessageCoordinatorExecutionResult getTargetComponentInCurrentPerspective(final String targetId,
                                                                                     final Message<Event, Object> message) {
        final SubComponent<EventHandler<Event>, Event, Object> component = ComponentRegistry.findComponentByQualifiedId(targetId);
        if (component != null) {
            // component is active
            return new MessageCoordinatorExecutionResult(component, message, MessageCoordinatorExecutionResult.State.HANDLE_ACTIVE);
        } else {
            // start inactive component
            return createComponentInstanceAndRegister(targetId, message);
        }
    }

    /**
     * Creates a new component instance and registers it
     *
     * @param targetId the fully qualified id
     * @param message  the message
     * @return a MessageCoordinatorExecutionResult  with value "HANDLE_INACTIVE"
     */
    private MessageCoordinatorExecutionResult createComponentInstanceAndRegister(final String targetId, final Message<Event, Object> message) {
        final SubComponent<EventHandler<Event>, Event, Object> component = PerspectiveUtil.getInstance(this.launcher).createSubcomponentById(targetId);
        if (component == null) throw new ComponentNotFoundException(
                "invalid component id. Source: "
                        + message.getSourceId() + " target: "
                        + message.getTargetId());

        return findParentPerspectiveAndRegisterComponent(component, message, targetId);
    }

    private MessageCoordinatorExecutionResult findParentPerspectiveAndRegisterComponent(final SubComponent<EventHandler<Event>, Event, Object> component, final Message<Event, Object> message, final String targetId) {
        final Perspective<Node, EventHandler<Event>, Event, Object> parentPerspective = PerspectiveRegistry.findPerspectiveById(FXUtil.getTargetPerspectiveId(targetId));
        if (parentPerspective == null)
            throw new ComponentNotFoundException("no valid perspective for component " + targetId + " found");
        parentPerspective.registerComponent(component);
        return new MessageCoordinatorExecutionResult(component, parentPerspective, message, MessageCoordinatorExecutionResult.State.HANDLE_INACTIVE);
    }


    @Override
    public final <P extends Component<EventHandler<Event>, Object>> void setComponentHandler(ComponentHandler<P, Message<Event, Object>> handler) {
        this.componentHandler = (ComponentHandler<SubComponent<EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;

    }

    @Override
    public final <P extends Component<EventHandler<Event>, Object>> void setPerspectiveHandler(ComponentHandler<P, Message<Event, Object>> handler) {
        this.perspectiveHandler = (ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;
    }

    @Override
    public final void setDelegateQueue(BlockingQueue<DelegateDTO<Event, Object>> delegateQueue) {
        this.delegateQueue = delegateQueue;
    }
}
