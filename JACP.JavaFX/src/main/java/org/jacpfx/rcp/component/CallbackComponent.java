package org.jacpfx.rcp.component;

import javafx.event.Event;
import org.jacpfx.api.component.ComponentHandle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 16.07.13
 * Time: 15:20
 * This Interface represents a non ui callback component. Annotate it with @CallbackComponent and add @Stateless if you need it stateless
 */
public interface CallbackComponent extends ComponentHandle<Object, Event, Object> {
}
