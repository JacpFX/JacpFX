package org.jacp.javafx.rcp.workbench;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.api.action.IAction;
import org.jacp.api.component.Injectable;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 30.08.13
 * Time: 10:00
 * This interface represents a JACP Workbench which should be implemented to create a workbench.
 */
public interface FXWorkbench extends Injectable {

    /**
     * Handle menu and bar entries created
     * @param layout, the component layout
     */
    abstract void postHandle(final FXComponentLayout layout);

    /**
     * JavaFX2 specific initialization method to create a workbench instance
     *
     * @param action, the initial event
     * @param layout, the workbench layout
     * @param stage, the JavaFX stage
     */
    abstract void handleInitialLayout(
            final IAction<Event, Object> action,
            final IWorkbenchLayout<Node> layout, final Stage stage);

}
