package org.jacpfx.rcp.delegator;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.IComponent;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.delegator.IMessageDelegator;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.handler.IComponentHandler;
import org.jacpfx.api.message.IDelegateDTO;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.message.FXMessage;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;
import org.jacpfx.rcp.util.WorkerUtil;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Created by Andy Moncsek on 10.12.13.
 */
public class MessageDelegator extends Thread implements
        IMessageDelegator<EventHandler<Event>, Event, Object> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private IComponentHandler<IPerspective<EventHandler<Event>, Event, Object>, Message<Event, Object>> perspectiveHandler;
    private final BlockingQueue<IDelegateDTO<Event, Object>> messageDelegateQueue = new ArrayBlockingQueue<>(
            10000);

    public MessageDelegator() {
        super("MessageDelegator");
        ShutdownThreadsHandler.registerThread(this);
    }

    @Override
    public final void run() {
        while (!Thread.interrupted()) {
            try {
                final IDelegateDTO<Event, Object> dto = this.messageDelegateQueue.take();
                this.handleCall(dto.getTarget(), dto.getMessage());
            } catch (final InterruptedException e) {
                logger.info("queue in FXComponentDelegator interrupted");
                break;
            } catch (final ExecutionException e) {
                logger.info("queue in FXComponentDelegator interrupted");
                e.printStackTrace();
            }

        }
    }



    private void handleCall(final String targetId,
                                     final Message<Event, Object> message) throws ExecutionException, InterruptedException {
        final IPerspective<EventHandler<Event>, Event, Object> perspective = PerspectiveRegistry.findPerspectiveById(targetId);
        if(perspective==null) throw new ComponentNotFoundException("no perspective for message : "+targetId+ " found");
        checkPerspectiveAndInit(perspective);
        perspective.getMessageQueue().put(message);
    }

    private void checkPerspectiveAndInit(final IPerspective<EventHandler<Event>, Event, Object> perspective) throws ExecutionException, InterruptedException {
        if(perspective.getContext().isActive()) return;
        initPerspective(perspective);
    }

    private void initPerspective(final IPerspective<EventHandler<Event>, Event, Object> perspective) throws ExecutionException, InterruptedException {
        WorkerUtil.invokeOnFXThreadAndWait(()->this.perspectiveHandler.initComponent(new FXMessage(perspective.getContext().getId(), perspective
                .getContext().getId(), "init", null),perspective));
    }

    @Override
    public void delegateMessage(IDelegateDTO<Event, Object> messageDTO) {

    }

    @Override
    public BlockingQueue<IDelegateDTO<Event, Object>> getMessageDelegateQueue() {
        return null;
    }

    @Override
    public <P extends IComponent<EventHandler<Event>, Event, Object>> void setComponentHandler(IComponentHandler<P, Message<Event, Object>> handler) {

    }
}
