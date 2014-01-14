/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2014
 *
 *  [Component.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx.rcp.components.errorDialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.util.DimensionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick Symmangk Andy Moncsek on 13.01.14.
 */
public class DefaultErrorDialog extends VBox implements EventHandler<ActionEvent> {

    /** Drag offsets for window dragging. */
    private final String message;

    /** The title. */
    private final String title;

    /** The bottom bar. */
    private HBox bottomBar;

    /** The BUTTO n_ size. */
    private final static int BUTTON_SIZE = 74;


    /** The explanation. */
    private TextArea explanation;

    /** The title label. */
    private Label titleLabel;

    /**
     * Instantiates a new JACP option dialog v2.
     *
     * @param title
     *            the title
     * @param message
     *            the message
     */
    public DefaultErrorDialog(final String title, final String message) {
        this.message = message;
        this.title = title;
        this.initDialog();
    }


    /**
     * Inits the dialog.
     */
    private void initDialog() {
        DimensionUtil dUtil = DimensionUtil.getInstance();
        this.getStyleClass().add("jacp-option-pane");
        this.setSpacing(10);
        this.maxHeightProperty().bind(dUtil.getStageHeightProperty().multiply(.6));
        this.maxWidthProperty().bind(dUtil.getStageHeightProperty().multiply(.8));
        this.explanation = new TextArea(this.message);
        this.explanation.setEditable(false);
        this.explanation.getStyleClass().add("jacp-option-pane-message");
        VBox.setMargin(this.explanation, new Insets(1, 1, 1, 1));
        VBox.setVgrow(this.explanation,Priority.ALWAYS);

        // create title
        this.titleLabel = new Label(this.title);
        this.titleLabel.getStyleClass().add("jacp-error-pane-title");
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
        this.getChildren().addAll(this.explanation, this.bottomBar);
        this.createButton("OK");
    }





    /**
     * Creates the button.
     *
     * @param label
     *            the button
     * @return the button
     */
    private Button createButton(final String label) {
        final Button but = new Button(label);
        but.setId(label.toLowerCase() + "Button");
        but.addEventHandler(ActionEvent.ACTION, this);
        HBox.setMargin(but, new Insets(0, 8, 0, 0));
        but.setDefaultButton(true);
        but.requestFocus();
        this.bottomBar.getChildren().add(but);
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
        this.maxHeightProperty().unbind();
        this.maxWidthProperty().unbind();
    }





}
