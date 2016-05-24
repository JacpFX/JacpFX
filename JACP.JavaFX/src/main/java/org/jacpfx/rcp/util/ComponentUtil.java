package org.jacpfx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.context.InternalContext;

/**
 * Created by Andy Moncsek on 24.05.16.
 */
public class ComponentUtil {

    public static void activateComponent(final SubComponent<EventHandler<Event>, Event, Object> component) {
        component.setStarted(true);
        InternalContext.class.cast(component.getContext()).updateActiveState(true);
    }
}
