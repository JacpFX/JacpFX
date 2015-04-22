package org.jacpfx.rcp.delegator;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.Component;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.ComponentHandler;
import org.jacpfx.api.message.DelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;
import org.jacpfx.rcp.util.WorkerUtil;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by Andy Moncsek on 10.12.13.
 */
public class MessageDelegatorImpl extends Thread implements
        org.jacpfx.api.delegator.MessageDelegator<EventHandler<Event>, Event, Object> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private final TransferQueue<DelegateDTO<Event, Object>> messageDelegateQueue = new LinkedTransferQueue<>();

    public MessageDelegatorImpl() {
        super("MessageDelegatorImpl");
        ShutdownThreadsHandler.registerThread(this);
    }

    @Override
    public final void run() {
        while (!Thread.interrupted()) {
            try {
                final DelegateDTO<Event, Object> dto = this.messageDelegateQueue.take();
                this.handleCall(dto.getTarget(), dto.getMessage());
            } catch (final InterruptedException e) {
                logger.info("queue in ComponentDelegator interrupted");
                break;
            } catch (final ExecutionException e) {
                logger.info("queue in ComponentDelegator interrupted");
                e.printStackTrace();
            }

        }
    }



    private void handleCall(final String targetId,
                                     final Message<Event, Object> message) throws ExecutionException, InterruptedException {
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        if(perspective==null) throw new ComponentNotFoundException("no perspective for message : "+targetId+ " found");
        checkPerspectiveAndInit(perspective);
        perspective.getMessageQueue().put(message);
    }

    private void checkPerspectiveAndInit(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) throws ExecutionException, InterruptedException {
        if(perspective.getContext().isActive()) return;
        initPerspective(perspective);
    }

    private void initPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> perspective) throws ExecutionException, InterruptedException {
        WorkerUtil.invokeOnFXThreadAndWait(()->this.perspectiveHandler.initComponent(new MessageImpl(perspective.getContext().getId(), perspective
                .getContext().getId(), "init", null),perspective));
    }


    @Override
    public TransferQueue<DelegateDTO<Event, Object>> getMessageDelegateQueue() {
        return this.messageDelegateQueue;
    }

    @Override
    public <P extends Component<EventHandler<Event>, Object>> void setPerspectiveHandler(ComponentHandler<P, Message<Event, Object>> handler) {
              this.perspectiveHandler = (ComponentHandler<Perspective<Node, EventHandler<Event>, Event, Object>, Message<Event, Object>>) handler;
    }
}
