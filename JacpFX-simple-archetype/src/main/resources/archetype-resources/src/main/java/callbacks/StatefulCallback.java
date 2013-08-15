#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.callbacks;

import java.util.logging.Logger;

import javafx.event.Event;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.CallbackComponent;
import org.jacp.javafx.rcp.component.AStatefulCallbackComponent;

/**
 * A StatefulCallback component is a simple background component whose result
 * will be always send back to the caller component. Unlike a Steteless
 * Component you can use it like a "normal" bean.
 * 
 * @author Andy Moncsek
 * 
 */
@CallbackComponent(id = "id003", name = "statefulCallback", active = true)
public class StatefulCallback extends AStatefulCallbackComponent {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public Object handleAction(IAction<Event, Object> arg0) {
		// runs in worker thread
		logger.info(arg0.getLastMessage().toString());
		return "StatefulCallback - hello";
	}

}
