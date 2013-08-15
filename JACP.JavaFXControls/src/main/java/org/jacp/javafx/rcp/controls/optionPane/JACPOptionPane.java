/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
 *
 * [JACPOptionPane.java]
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
package org.jacp.javafx.rcp.controls.optionPane;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;

/**
 * The Class JACPoptionDialogV2.
 * 
 * @author Patrick Symmangk
 */
public class JACPOptionPane extends VBox implements EventHandler<ActionEvent> {

	/** Drag offsets for window dragging. */
	private final String message;

	/** The title. */
	private final String title;

	/** The default button. */
	private JACPDialogButton defaultButton;

	/** The ok button. */
	private Button okButton;

	/** The cancel button. */
	private Button cancelButton;

	/** The yes button. */
	private Button yesButton;

	/** The no button. */
	private Button noButton;

	/** The bottom bar. */
	private HBox bottomBar;

	/** The BUTTO n_ size. */
	private final static int BUTTON_SIZE = 74;

	/** The buttons. */
	private List<Button> buttons;

	/** The explanation. */
	private Text explanation;

	/** The title label. */
	private Label titleLabel;

	/** The top box. */
	private HBox topBox;
	
	/** The auto hide. */
	private boolean autoHide = true;

	/**
	 * Instantiates a new JACP option dialog v2.
	 * 
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 */
	public JACPOptionPane(final String title, final String message) {
		this.message = message;
		this.title = title;
		this.initDialog();
	}
	

	/**
	 * Inits the dialog.
	 */
	private void initDialog() {
		this.buttons = new ArrayList<Button>();
		this.getStyleClass().add("jacp-option-pane");
		this.setSpacing(10);
		this.setMaxSize(430, Region.USE_PREF_SIZE);
		// block mouse clicks
		this.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent t) {
				t.consume();
			}
		});

		this.explanation = new Text(this.message);
		this.explanation.setWrappingWidth(400);
		this.explanation.getStyleClass().add("jacp-option-pane-message");

		final BorderPane explPane = new BorderPane();
		VBox.setMargin(explPane, new Insets(5, 5, 5, 5));
		explPane.setCenter(this.explanation);
		BorderPane.setMargin(this.explanation, new Insets(5, 5, 5, 5));

		this.topBox = new HBox();
		// topBox.setAlignment(Pos.TOP_RIGHT);
		this.topBox.setAlignment(Pos.TOP_LEFT);
		final Button defaultClose = new Button("x");
		defaultClose.setOnAction(this);
		this.setDefaultCloseButtonOrientation(Pos.CENTER_RIGHT);
		defaultClose.getStyleClass().add("jacp-option-pane-close");
		this.setDefaultCloseButtonVisible(false);

		this.topBox.getChildren().add(defaultClose);
		VBox.setVgrow(this.topBox, Priority.ALWAYS);

		this.getChildren().add(this.topBox);

		// create title
		this.titleLabel = new Label(this.title);
		this.titleLabel.getStyleClass().add("jacp-option-pane-title");
		this.titleLabel.setMinHeight(30);
		this.titleLabel.setMaxHeight(Double.MAX_VALUE);
		this.titleLabel.setPrefHeight(22);
		this.titleLabel.setMaxWidth(Double.MAX_VALUE);
		this.titleLabel.setAlignment(Pos.CENTER);

		this.getChildren().add(this.titleLabel);

		this.bottomBar = new HBox(0);
		this.bottomBar.setAlignment(Pos.BASELINE_RIGHT);

		VBox.setMargin(this.bottomBar, new Insets(20, 5, 5, 5));
		this.setEffect(new DropShadow());
		this.getChildren().addAll(explPane, this.bottomBar);
	}

	/**
	 * Sets the on ok action.
	 * 
	 * @param onOK
	 *            the new on ok action
	 */
	public void setOnOkAction(final EventHandler<ActionEvent> onOK) {
		if (this.okButton == null) {
			this.okButton = this.createButton(JACPDialogButton.OK);
		}
		this.setAction(this.okButton, onOK);
	}

	/**
	 * Sets the on cancel action.
	 * 
	 * @param onCancel
	 *            the new on cancel action
	 */
	public void setOnCancelAction(final EventHandler<ActionEvent> onCancel) {
		if (this.cancelButton == null) {
			this.cancelButton = this.createButton(JACPDialogButton.CANCEL);
		}
		this.setAction(this.cancelButton, onCancel);
	}

	/**
	 * Sets the on yes action.
	 * 
	 * @param onYes
	 *            the new on yes action
	 */
	public void setOnYesAction(final EventHandler<ActionEvent> onYes) {
		if (this.yesButton == null) {
			this.yesButton = this.createButton(JACPDialogButton.YES);
		}
		this.setAction(this.yesButton, onYes);
	}

	/**
	 * Sets the on no action.
	 * 
	 * @param onNo
	 *            the new on no action
	 */
	public void setOnNoAction(final EventHandler<ActionEvent> onNo) {
		if (this.noButton == null) {
			this.noButton = this.createButton(JACPDialogButton.NO);
		}
		this.setAction(this.noButton, onNo);
	}

	/**
	 * Sets the action.
	 * 
	 * @param button
	 *            the button
	 * @param handler
	 *            the handler
	 */
	private void setAction(final Button button,
			final EventHandler<ActionEvent> handler) {
		if (button != null) {
			button.addEventHandler(ActionEvent.ACTION, handler);
		}
	}

	/**
	 * Creates the button.
	 * 
	 * @param button
	 *            the button
	 * @return the button
	 */
	private Button createButton(final JACPDialogButton button) {
		final Button but = new Button(button.getLabel());
		but.setId(button.getLabel().toLowerCase() + "Button");
		but.setMinWidth(JACPOptionPane.BUTTON_SIZE);
		but.setPrefWidth(JACPOptionPane.BUTTON_SIZE);
		if(autoHide)
		{
		    but.addEventHandler(ActionEvent.ACTION, this);
		}
		HBox.setMargin(but, new Insets(0, 8, 0, 0));
		if (this.defaultButton != null
				&& button.getId() == this.defaultButton.getId()) 
		{
			but.setDefaultButton(true);
			but.requestFocus(); 
		}
		this.bottomBar.getChildren().add(but);
		this.buttons.add(but);
		but.getStyleClass().add("jacp-option-pane-button");
		return but;
	}

	/*
	* (non-Javadoc)
	*
	* @see javafx.event.EventHandler#handle(javafx.event.Event)
	*/
	@Override
	public void handle(final ActionEvent actionEvent) {
		JACPModalDialog.getInstance().hideModalDialog();
	}

	/**
	 * Sets the default close button orientation.
	 * 
	 * @param pos
	 *            the new default close button orientation
	 */
	public void setDefaultCloseButtonOrientation(final Pos pos) {
		this.topBox.setAlignment(pos);
	}

	public void setDefaultCloseButtonVisible(final boolean visible) {
		this.topBox.setVisible(visible);
	}
	
	/**
	 * Sets the auto hide.
	 * if autohide is set to true, the dialog will hide automatically if a button was pressed.
	 *
	 * @param autoHide the new auto hide
	 */
	public void setAutoHide(boolean autoHide) {
	    this.autoHide = autoHide;
	}

	/**
	 * Sets the default button.
	 * The highlight button.
	 *
	 * @param defaultButton the new default button
	 */
	public void setDefaultButton(final JACPDialogButton defaultButton) {
		this.defaultButton = defaultButton;
		for (final Button but : this.buttons) {
			if (defaultButton != null
					&& defaultButton.getLabel().equals(but.getText())) {
				but.setDefaultButton(true);
				but.requestFocus();
			} else {
				but.setDefaultButton(false);
			}
		}

	}


}
