package org.jacpfx.rcp.workbench;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacpfx.api.action.IAction;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.componentLayout.IWorkbenchLayout;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;

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
    void postHandle(final FXComponentLayout layout);

    /**
     * JavaFX2 specific initialization method to create a workbench instance
     *
     * @param action, the initial event
     * @param layout, the workbench layout
     * @param stage, the JavaFX stage
     */
    void handleInitialLayout(
            final IAction<Event, Object> action,
            final IWorkbenchLayout<Node> layout, final Stage stage);

}
