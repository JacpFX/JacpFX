/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [PerspectiveTwo.java]
 * AHCP Project (http://jacp.googlecode.com/)
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
package org.jacp.perspectives;

import java.util.ResourceBundle;

import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.scene.layout.Priority;

import org.jacp.api.action.IAction;
import org.jacp.api.annotations.OnStart;
import org.jacp.api.annotations.OnTearDown;
import org.jacp.api.annotations.Perspective;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.componentLayout.PerspectiveLayout;
import org.jacp.javafx.rcp.perspective.AFXPerspective;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;

/**
 * A simple perspective defining a split pane
 * 
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 * 
 */
@Perspective(id = "id02", name = "perspectiveTwo", resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
public class PerspectiveTwo extends AFXPerspective {

	@Override
	public void handlePerspective(final IAction<Event, Object> action,
			final PerspectiveLayout perspectiveLayout) {
		if (action.getLastMessage().equals(MessageUtil.INIT)) {
			final SplitPane mainLayout = SplitPaneBuilder.create()
					.styleClass("vsplitpane").orientation(Orientation.VERTICAL)
					.prefHeight(600).prefWidth(800).build();

			mainLayout.setDividerPosition(0, 0.50f);

			// create left button menu
			final GridPane top = GridPaneBuilder.create()
					.alignment(Pos.TOP_CENTER).build();
			GridPane.setHgrow(top, Priority.ALWAYS);
			GridPane.setVgrow(top, Priority.ALWAYS);

			// create main content Top
			final GridPane bottom = GridPaneBuilder.create()
					.alignment(Pos.BOTTOM_CENTER).build();
			GridPane.setHgrow(bottom, Priority.ALWAYS);
			GridPane.setVgrow(bottom, Priority.ALWAYS);

			GridPane.setVgrow(mainLayout, Priority.ALWAYS);
			GridPane.setHgrow(mainLayout, Priority.ALWAYS);

			mainLayout.getItems().addAll(top, bottom);
			// Register root component
			perspectiveLayout.registerRootComponent(mainLayout);
			// register left menu
			perspectiveLayout.registerTargetLayoutComponent("PTop", top);
			// register main content
			perspectiveLayout.registerTargetLayoutComponent("PBottom", bottom);
		}

	}

	@OnStart
	/**
	 * @OnStart annotated method will be executed when component is activated.
	 * @param layout
	 * @param resourceBundle
	 */
	public void onStartPerspective(final FXComponentLayout layout,
			final ResourceBundle resourceBundle) {
		// define toolbars and menu entries
	}

	@OnTearDown
	/**
	 * @OnTearDown annotated method will be executed when component is deactivated.
	 * @param arg0
	 */
	public void onTearDownPerspective(final FXComponentLayout arg0) {
		// define toolbars and menu entries when close perspective

	}

}
