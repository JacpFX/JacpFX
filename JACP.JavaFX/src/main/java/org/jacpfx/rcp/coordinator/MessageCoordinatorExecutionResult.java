package org.jacpfx.rcp.coordinator;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.delegator.DelegateDTO;

/**
 * Created by Andy Moncsek on 12.12.13.
 * This DTO contains the result object and state of message handling.
 */
public class MessageCoordinatorExecutionResult {

    private final ISubComponent<EventHandler<Event>, Event, Object> targetComponent;
    private final DelegateDTO dto;
    private final String targetId;
    private final Message<Event, Object> message;
    private final State state;

    public enum State {
        HANDLE_ACTIVE, HANDLE_INACTIVE, DELEGATE, HANDLE_CURRENT_PERSPECTIVE , ERROR
    }

    public MessageCoordinatorExecutionResult(final ISubComponent<EventHandler<Event>, Event, Object> targetComponent,final DelegateDTO dto,final String targetId, final Message<Event, Object> message, State state) {
        this.targetComponent = targetComponent;
        this.dto = dto;
        this.targetId = targetId;
        this.message = message;
        this.state = state;
    }

    public MessageCoordinatorExecutionResult(final ISubComponent<EventHandler<Event>, Event, Object> targetComponent, final Message<Event, Object> message, final State state) {
        this(targetComponent, null, null, message, state);
    }

    public MessageCoordinatorExecutionResult(final DelegateDTO dto, State state) {
        this(null,dto,null,null,state);
    }

    public MessageCoordinatorExecutionResult(final String targetId, final Message<Event, Object> message, final State state) {
        this(null,null,targetId,message,state);
    }
    public MessageCoordinatorExecutionResult(final State state) {
        this(null,null,null,null,state);
    }

    public ISubComponent<EventHandler<Event>, Event, Object> getTargetComponent() {
        return targetComponent;
    }

    public DelegateDTO getDto() {
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
}
