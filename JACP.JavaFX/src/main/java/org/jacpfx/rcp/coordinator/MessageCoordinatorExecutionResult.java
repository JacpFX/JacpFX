package org.jacpfx.rcp.coordinator;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.delegator.DelegateDTOImpl;

/**
 * Created by Andy Moncsek on 12.12.13.
 * This DTO contains the result object and state of message handling.
 */
public class MessageCoordinatorExecutionResult {

    private final SubComponent<EventHandler<Event>, Event, Object> targetComponent;
    private final DelegateDTOImpl dto;
    private final String targetId;
    private final Message<Event, Object> message;
    private final State state;
    private final Perspective<EventHandler<Event>, Event, Object> parentPerspective;

    public enum State {
        HANDLE_ACTIVE, HANDLE_INACTIVE, DELEGATE, HANDLE_CURRENT_PERSPECTIVE, ERROR
    }

    private MessageCoordinatorExecutionResult(final SubComponent<EventHandler<Event>, Event, Object> targetComponent, final Perspective<EventHandler<Event>, Event, Object> parentPerspective, final DelegateDTOImpl dto, final String targetId, final Message<Event, Object> message, State state) {
        this.targetComponent = targetComponent;
        this.parentPerspective = parentPerspective;
        this.dto = dto;
        this.targetId = targetId;
        this.message = message;
        this.state = state;
    }

    public MessageCoordinatorExecutionResult(final SubComponent<EventHandler<Event>, Event, Object> targetComponent, final Message<Event, Object> message, final State state) {
        this(targetComponent, null, null, null, message, state);
    }

    public MessageCoordinatorExecutionResult(final SubComponent<EventHandler<Event>, Event, Object> targetComponent, final Perspective<EventHandler<Event>, Event, Object> parentPerspective, final Message<Event, Object> message, final State state) {
        this(targetComponent, parentPerspective, null, null, message, state);
    }

    public MessageCoordinatorExecutionResult(final DelegateDTOImpl dto, State state) {
        this(null, null, dto, null, null, state);
    }

    public MessageCoordinatorExecutionResult(final String targetId, final Message<Event, Object> message, final State state) {
        this(null, null, null, targetId, message, state);
    }

    public MessageCoordinatorExecutionResult(final State state) {
        this(null, null, null, null, null, state);
    }

    public SubComponent<EventHandler<Event>, Event, Object> getTargetComponent() {
        return targetComponent;
    }

    public DelegateDTOImpl getDto() {
        return dto;
    }

    public String getTargetId() {
        return targetId;
    }

    public Message<Event, Object> getMessage() {
        return message;
    }

    public State getState() {
        return state;
    }

    public Perspective<EventHandler<Event>, Event, Object> getParentPerspective() { return parentPerspective; }
}
