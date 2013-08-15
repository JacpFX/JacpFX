#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.callbacks;

import java.util.logging.Logger;

import javafx.event.Event;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.CallbackComponent;
import org.jacp.javafx.rcp.component.AStatelessCallbackComponent;

/**
 * a simple stateless callback component. Messages to this type of component
 * will result in many instances, controlled by the JacpFX scheduler. Do not use
 * any private members as you can not be sure to get the same instance twice.
 * Stateless Components must have the spring scope="prototype", otherwise only
 * one instance will run. The Result will allays be send back to caller
 * component or create intermediate messages to other components.
 * 
 * @author Andy Moncsek
 * 
 */
@CallbackComponent(id = "id004", name = "statelessCallback", active = true)
public class StatelessCallback extends AStatelessCallbackComponent {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public Object handleAction(IAction<Event, Object> arg0) {
		// runs in worker thread
		logger.info(arg0.getLastMessage().toString());
		return "StatelessCallback - hello";
	}

}
