/*
 * Copyright (C) 2010,2011.
 * AHCP Project (http://code.google.com/p/jacp)
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
 */
package org.jacp.demo.perspectives;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;
import org.jacp.api.annotations.PostConstruct;
import org.jacp.api.annotations.PreDestroy;
import org.jacp.api.annotations.Perspective;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.demo.constants.GlobalConstants;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.componentLayout.PerspectiveLayout;
import org.jacp.javafx.rcp.components.toolBar.JACPToolBar;
import org.jacp.javafx.rcp.perspective.AFXPerspective;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;

/**
 * Contact perspective; here you define the basic layout for your application
 * view and declare targets for your UI components.
 * 
 * @author Andy Moncsek
 * 
 */
@Perspective(id = "id01", name = "contactPerspective",
        components ={
                GlobalConstants.ComponentConstants.COMPONENT_DETAIL_VIEW,
                GlobalConstants.ComponentConstants.COMPONENT_CHART_VIEW,
                GlobalConstants.ComponentConstants.COMPONENT_TABLE_VIEW,
                GlobalConstants.ComponentConstants.COMPONENT_TREE_VIEW,
                GlobalConstants.CallbackConstants.CALLBACK_CREATOR,
                GlobalConstants.CallbackConstants.CALLBACK_COORDINATOR,
                GlobalConstants.CallbackConstants.CALLBACK_ANALYTICS} ,
        viewLocation = "/fxml/contactPerspective.fxml",
        resourceBundleLocation = "bundles.languageBundle" ,
        localeID="en_US")
public class ContactPerspective extends AFXPerspective {
	private final static Log LOGGER = LogFactory
			.getLog(ContactPerspective.class);
	private String topId = "PmainContentTop";
	
	private String bottomId = "PmainContentBottom";
	
	private String detailId = "PdetailComponent";

	@FXML
	private GridPane gridPane1;
	@FXML
	private GridPane gridPane2;
	@FXML
	private GridPane chartView;
	@FXML
	private GridPane detailView;
	@FXML
	private AnchorPane chartAnchor;
	@FXML
	private AnchorPane detailAnchor;

	@PostConstruct
	/**
	 * create buttons in tool bars; menu entries  
	 */
	public void PostConstructPerspective(final FXComponentLayout layout) {
		LOGGER.debug("PostConstructPerspective");
		// create button in toolbar; button should switch top and bottom id's
		final JACPToolBar north = layout
				.getRegisteredToolBar(ToolbarPosition.NORTH);

		final Button custom = new Button("switch");
		custom.setTooltip(new Tooltip("Switch Components"));
		custom.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {
				final IActionListener<EventHandler<Event>, Event, Object> listener = ContactPerspective.this
						.getActionListener("switch");
				listener.getAction().setMessage("switch");
				listener.performAction(null);

			}
		});
		north.addOnEnd(this.getId(), custom);
	}

	@PreDestroy
	public void PreDestroyPerspective(final FXComponentLayout layout) {
		LOGGER.debug("PreDestroyPerspective");
	}

	@Override
	public void handlePerspective(final IAction<Event, Object> action,
			final PerspectiveLayout perspectiveLayout) {
		if (action.getMessage().equals(MessageUtil.INIT)) {
			this.createPerspectiveLayout(perspectiveLayout);
		} else if (action.getMessage().equals("switch")) {
			final String tmp = this.topId;
			this.topId = this.bottomId;
			this.bottomId = tmp;
			this.createPerspectiveLayout(perspectiveLayout);
		}
		LOGGER.debug("handlePerspective message: "+action.getMessage());
	}

	private void createPerspectiveLayout(
			final PerspectiveLayout perspectiveLayout) {

		// bind width and height to ensure that the gridpane is always on
		// fullspan!
		chartView.minWidthProperty().bind(chartAnchor.widthProperty());
		chartView.minHeightProperty().bind(chartAnchor.heightProperty());

		detailView.minWidthProperty().bind(detailAnchor.widthProperty());
		detailView.minHeightProperty().bind(detailAnchor.heightProperty());


		GridPane.setVgrow(perspectiveLayout.getRootComponent(), Priority.ALWAYS);
		GridPane.setHgrow(perspectiveLayout.getRootComponent(), Priority.ALWAYS);
		perspectiveLayout.registerTargetLayoutComponent("PleftMenu", gridPane1);
		// register main content Top
		perspectiveLayout.registerTargetLayoutComponent(this.topId, gridPane2);
		// register main content Bottom
		perspectiveLayout.registerTargetLayoutComponent(this.bottomId,
				chartView);
		perspectiveLayout.registerTargetLayoutComponent(this.detailId,
				detailView);
	}

}
