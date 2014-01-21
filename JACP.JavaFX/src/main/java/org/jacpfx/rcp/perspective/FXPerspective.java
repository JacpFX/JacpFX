package org.jacpfx.rcp.perspective;

import javafx.event.Event;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.message.Message;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.08.13
 * Time: 10:10
 * Defines a perspective to implement in JacpFX
 */
public interface FXPerspective extends Injectable {

    /**
     * Handle perspective method to initialize the perspective and the layout.
     *
     * @param message            ; the message triggering the method
     * @param perspectiveLayout ,  the layout handler defining the perspective
     */
    void handlePerspective(Message<Event, Object> message,
                                              final PerspectiveLayout perspectiveLayout);
}
