/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ComponentRight.java]
 * AHCP Project (http://jacp.googlecode.com)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacp.components;

import java.util.logging.Logger;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Component;
import org.jacp.api.annotations.OnStart;
import org.jacp.api.annotations.OnTearDown;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;
/**
 * A simple JacpFX UI component
 * @author Andy Moncsek
 *
 */
@Component(defaultExecutionTarget = "PMain", id = "id002", name = "componentRight", active = true)
public class ComponentRight extends AFXComponent {
	private ScrollPane pane;
	private Label rightLabel;
	private Logger log = Logger.getLogger(ComponentRight.class.getName());

	@Override
	/**
	 * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
	 */
	public Node handleAction(IAction<Event, Object> action) {
		// runs in worker thread
		if (action.getLastMessage().equals(MessageUtil.INIT)) {
			return createUI();
		}
		return null;
	}

	@Override
	/**
	 * The postHandleAction method runs always in the main application thread.
	 */
	public Node postHandleAction(Node arg0, IAction<Event, Object> action) {
		// runs in FX application thread
		if (action.getLastMessage().equals(MessageUtil.INIT)) {
			this.pane = (ScrollPane) arg0;
		}else {
			rightLabel.setText(action.getLastMessage().toString());
		}
		return this.pane;
	}


	@OnStart
	/**
	 * The @OnStart annotation labels methods executed when the component switch from inactive to active state
	 * @param arg0
	 */
	public void onStartComponent(FXComponentLayout arg0) {
		log.info("run on start of ComponentRight ");

	}

	@OnTearDown
	/**
	 * The @OnTearDown annotations labels methods executed when the component is set to inactive
	 * @param arg0
	 */
	public void onTearDownComponent(FXComponentLayout arg0) {
		log.info("run on tear down of ComponentRight ");

	}
	
	/**
	 * create the UI on first call
	 * 
	 * @return
	 */
	private Node createUI() {
		ScrollPane pane = new ScrollPane();
		pane.setFitToHeight(true);
		pane.setFitToWidth(true);
		GridPane.setHgrow(pane, Priority.ALWAYS);
		GridPane.setVgrow(pane, Priority.ALWAYS);
		final VBox box = new VBox();
		final Button right = new Button("right");
		rightLabel = new Label("");
		right.setOnMouseClicked( getMessage());
		VBox.setMargin(right, new Insets(4, 2, 4, 5));
		box.getChildren().addAll(right,rightLabel);
		pane.setContent(box);
		return pane;
	}

	private EventHandler<Event> getMessage() {
		return new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				getActionListener("id01.id004", "hello stateless component").performAction(arg0);
			}
		};
	}

}

