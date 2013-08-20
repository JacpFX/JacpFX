package org.jacp.javafx.rcp.perspective;

import javafx.event.Event;
import org.jacp.api.action.IAction;
import org.jacp.api.component.Injectable;
import org.jacp.javafx.rcp.componentLayout.PerspectiveLayout;

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
     * @param action            ; the action triggering the method
     * @param perspectiveLayout ,  the layout handler defining the perspective
     */
    void handlePerspective(IAction<Event, Object> action,
                                              final PerspectiveLayout perspectiveLayout);
}
