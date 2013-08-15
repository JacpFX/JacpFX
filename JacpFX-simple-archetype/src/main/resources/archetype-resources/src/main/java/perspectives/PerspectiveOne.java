#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.perspectives;

import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.OnStart;
import org.jacp.api.annotations.OnTearDown;
import org.jacp.api.annotations.Perspective;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.componentLayout.FXPerspectiveLayout;
import org.jacp.javafx.rcp.perspective.AFXPerspective;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;
/**
 * A simple perspective defining a split pane
 * @author Andy Moncsek
 *
 */
@Perspective(id = "id01", name = "perspectiveOne")
public class PerspectiveOne extends AFXPerspective {

	@Override
	public void handlePerspective(IAction<Event, Object> action,
			FXPerspectiveLayout perspectiveLayout) {
		if (action.getLastMessage().equals(MessageUtil.INIT)) {
			SplitPane mainLayout = new SplitPane();
			mainLayout.setOrientation(Orientation.HORIZONTAL);
			mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			mainLayout.setDividerPosition(0, 0.55f);

			// create left button menu
			GridPane leftMenu = new GridPane();
			GridPane.setHgrow(leftMenu, Priority.ALWAYS);
			GridPane.setVgrow(leftMenu, Priority.ALWAYS);
		
			// create main content Top
			GridPane mainContent = new GridPane();
			GridPane.setHgrow(mainContent, Priority.ALWAYS);
			GridPane.setVgrow(mainContent, Priority.ALWAYS);
	
			
			GridPane.setVgrow(mainLayout, Priority.ALWAYS);
			GridPane.setHgrow(mainLayout, Priority.ALWAYS);
			mainLayout.getItems().addAll(leftMenu, mainContent);
			// Register root component
			perspectiveLayout.registerRootComponent(mainLayout);
			// register left menu
			perspectiveLayout.registerTargetLayoutComponent("PLeft", leftMenu);
			// register main content 
			perspectiveLayout.registerTargetLayoutComponent("PMain", mainContent);
		}
		
	}

	@OnStart
	public void onStartPerspective(FXComponentLayout layout) {
		// define toolbars and menu entries
		
	}

	@OnTearDown
	public void onTearDownPerspective(FXComponentLayout arg0) {
		// define toolbars and menu entries when close perspective
		
	}

}
