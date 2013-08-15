#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.main;

import javafx.application.Application;
import javafx.stage.Stage;

import org.jacp.project.launcher.AFX2SpringLauncher;
/**
 * The application launcher initializes the JacpFX context
 * @author Andy Moncsek
 *
 */
public class ApplicationLauncher extends AFX2SpringLauncher {


	public ApplicationLauncher() {
		super("main.xml");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void postInit(Stage stage) {
		
	}

}
