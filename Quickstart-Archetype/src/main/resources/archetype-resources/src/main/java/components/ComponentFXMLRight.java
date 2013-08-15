#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package ${package}.components;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.DeclarativeComponent;
import org.jacp.api.annotations.OnStart;
import org.jacp.api.annotations.OnTearDown;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;

/**
 * A simple JacpFX FXML UI component
 * 
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 * 
 */
@DeclarativeComponent(defaultExecutionTarget = "PMain", id = "id002", name = "componentRight", active = true, viewLocation = "/fxml/ComponentRightFXML.fxml", resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
public class ComponentFXMLRight extends AFXComponent {

	private final Logger log = Logger.getLogger(ComponentFXMLRight.class
			.getName());
	@FXML
	private TextField message;
	@FXML
	private TextField countryTextField;
	@FXML
	private TextField streetTextField;
	@FXML
	private TextField nameTextField;
	@FXML
	private TextArea descriptionValue;

	private final AtomicInteger counter = new AtomicInteger(0);

	@Override
	/**
	 * The handleAction method always runs outside the main application thread. You can create new nodes, execute long running tasks but you are not allowed to manipulate existing nodes here.
	 */
	public Node handleAction(final IAction<Event, Object> action) {
		// runs in worker thread

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
			// the initial message for all components
		} else {
			final int counterLoc = this.counter.incrementAndGet();
			this.countryTextField.setText("my country" + counterLoc);
			this.streetTextField.setText("my street" + counterLoc);
			this.nameTextField.setText("my name" + counterLoc);
			this.descriptionValue
					.setText(counterLoc
							+ ": Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.");
			this.message.setText(action.getLastMessage().toString());
		}
		return null;
	}

	@OnStart
	/**
	 * The @OnStart annotation labels methods executed when the component switch from inactive to active state
	 * @param arg0
	 * @param resourceBundle
	 */
	public void onStartComponent(final FXComponentLayout arg0,
			final ResourceBundle resourceBundle) {
		this.log.info("run on start of ComponentRight ");

	}

	@OnTearDown
	/**
	 * The @OnTearDown annotations labels methods executed when the component is set to inactive
	 * @param arg0
	 */
	public void onTearDownComponent(final FXComponentLayout arg0) {
		this.log.info("run on tear down of ComponentRight ");

	}

 
	@FXML
	private void handleSend(final ActionEvent event) {
		this.getActionListener("id01.id004", "hello stateless component")
				.performAction(event);
	}

}
