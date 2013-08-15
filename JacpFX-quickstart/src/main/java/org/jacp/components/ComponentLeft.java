/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ComponentLeft.java]
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

import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.AnchorPaneBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Component;
import org.jacp.api.annotations.OnStart;
import org.jacp.api.annotations.OnTearDown;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;

/**
 * A simple JacpFX UI component
 * 
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 * 
 */
@Component(defaultExecutionTarget = "Pleft", id = "id001", name = "componentLeft", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
public class ComponentLeft extends AFXComponent {
	private AnchorPane pane;
	private TextField textField;
	private final Logger log = Logger.getLogger(ComponentLeft.class.getName());

	@Override
	/**
	 * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
	 */
	public Node handleAction(final IAction<Event, Object> action) {
		// runs in worker thread
		if (action.getLastMessage().equals(MessageUtil.INIT)) {
			return this.createUI();
		}
		return null;
	}

	@Override
	/**
	 * The postHandleAction method runs always in the main application thread.
	 */
	public Node postHandleAction(final Node arg0,
			final IAction<Event, Object> action) {
		// runs in FX application thread
		if (action.getLastMessage().equals(MessageUtil.INIT)) {
			this.pane = (AnchorPane) arg0;
		} else {
			this.textField.setText(action.getLastMessage().toString());
		}
		return this.pane;
	}

	@OnStart
	/**
	 * The @OnStart annotation labels methods executed when the component switch from inactive to active state
	 * @param arg0
	 */
	public void onStartComponent(final FXComponentLayout arg0,
			final ResourceBundle resourceBundle) {
		this.log.info("run on start of ComponentLeft ");
	}

	@OnTearDown
	/**
	 * The @OnTearDown annotations labels methods executed when the component is set to inactive
	 * @param arg0
	 */
	public void onTearDownComponent(final FXComponentLayout arg0) {
		this.log.info("run on tear down of ComponentLeft ");

	}

	/**
	 * create the UI on first call
	 * 
	 * @return
	 */
	private Node createUI() {
		final AnchorPane anchor = AnchorPaneBuilder.create()
				.styleClass("roundedAnchorPaneFX").build();
		final Label heading = LabelBuilder.create()
				.text(this.getResourceBundle().getString("javafxComp"))
				.alignment(Pos.CENTER_RIGHT).styleClass("propLabelBig").build();

		final Button left = ButtonBuilder
				.create()
				.text(this.getResourceBundle().getString("send"))
				.layoutX(120)
				.onMouseClicked(
						this.getActionListener("id01.id003",
								"hello stateful component").getListener())
				.alignment(Pos.CENTER).build();

		this.textField = TextFieldBuilder.create().text("")
				.styleClass("propTextField").alignment(Pos.CENTER).build();

		AnchorPane.setRightAnchor(heading, 25.0);
		AnchorPane.setTopAnchor(heading, 15.0);

		AnchorPane.setTopAnchor(left, 80.0);
		AnchorPane.setRightAnchor(left, 25.0);

		AnchorPane.setTopAnchor(this.textField, 50.0);
		AnchorPane.setRightAnchor(this.textField, 25.0);
		AnchorPane.setLeftAnchor(this.textField, 25.0);

		anchor.getChildren().addAll(heading, left, this.textField);

		GridPane.setHgrow(anchor, Priority.ALWAYS);
		GridPane.setVgrow(anchor, Priority.ALWAYS);

		return anchor;
	}

}
