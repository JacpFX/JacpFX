package org.jacp.javafx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.component.IComponentView;


/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 25.06.13
 * Time: 11:07
 * Interface o implement a UI CallbackComponent in Jacp.
 */
public interface FXComponent extends IComponentView<Node, EventHandler<Event>, Event, Object> {


}
