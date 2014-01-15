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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.util.DimensionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy Moncsek on 13.01.14.
 */
public class DefaultErrorDialog extends VBox implements EventHandler<ActionEvent> {

    /**
     * Drag offsets for window dragging.
     */
    private final String message;

    /**
     * The title.
     */
    private final String title;

    private GridPane titlePane;

    private HBox buttonBox;

    /**
     * Instantiates a new JACP option dialog v2.
     *
     * @param title   the title
     * @param message the message
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
        this.getStyleClass().add("jacp-option-pane");
        final DimensionUtil dUtil = DimensionUtil.getInstance();

        this.maxHeightProperty().bind(dUtil.getStageHeightProperty().multiply(.6));
        this.maxWidthProperty().bind(dUtil.getStageHeightProperty().multiply(.8));

        titlePane = createTitleBar();
        buttonBox = createButtonBar();
        this.getChildren().addAll(titlePane, createErrorMessageField(), buttonBox);


    }

    private TextArea createErrorMessageField() {
        final TextArea explanation = new TextArea(this.message);
        explanation.setEditable(false);
        explanation.getStyleClass().add("jacp-option-pane-message");
        VBox.setMargin(explanation, new Insets(1, 1, 1, 1));
        VBox.setVgrow(explanation,Priority.ALWAYS);
        return explanation;
    }

    private GridPane createTitleBar() {
        final GridPane box = new GridPane();
        box.setVgap(10);
        box.setHgap(20);
        VBox.setVgrow(box, Priority.ALWAYS);
        box.getChildren().addAll(this.createErrorIcon(), this.createInfoLabel());

        return box;
    }


    private HBox createButtonBar() {
        final HBox box = new HBox();
        box.maxHeightProperty().bind(this.maxHeightProperty().multiply(.1));
        VBox.setVgrow(box, Priority.ALWAYS);


        final Pane fillPane = new Pane();
        fillPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fillPane, Priority.ALWAYS);

        final Button but = new Button("OK");
        but.addEventHandler(ActionEvent.ACTION, this);
        HBox.setMargin(but, new Insets(16, 8, 8, 8));
        but.setDefaultButton(true);
        but.requestFocus();
        but.getStyleClass().add("jacp-option-pane-button");
        box.getChildren().addAll(fillPane, but);
        return box;

    }

    private Label createInfoLabel() {
        final Label label = new Label(this.title);
        GridPane.setColumnIndex(label, 1);
        GridPane.setRowIndex(label, 0);
        return label;
    }

    private ImageView createErrorIcon() {
        final ImageView view = new ImageView(new Image("images/error.png"));
        GridPane.setColumnIndex(view, 0);
        GridPane.setRowIndex(view, 0);
        GridPane.setHalignment(view, HPos.CENTER);
        GridPane.setValignment(view, VPos.TOP);
        return view;
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
        buttonBox.maxHeightProperty().unbind();
    }
}
