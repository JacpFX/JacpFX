package org.jacp.javafx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IComponentHandle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 16.07.13
 * Time: 15:20
 * This Interface represents a non ui callback component. Annotate it with @CallbackComponent and add @Stateless if you need it stateless
 */
public interface CallbackComponent extends IComponentHandle<Object,EventHandler<Event>, Event, Object> {
}
