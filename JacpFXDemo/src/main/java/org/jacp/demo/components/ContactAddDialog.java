/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ContactAddDialog.java]
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
package org.jacp.demo.components;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.jacp.api.action.IActionListener;
import org.jacp.demo.constants.GlobalConstants;
import org.jacp.demo.entity.Contact;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;
import org.springframework.util.StringUtils;

/**
 * Creates the "add new contact" dialog
 * @author Andy Moncsek
 *
 */
public class ContactAddDialog {
    final ContactTreeViewComponent parent;

    public ContactAddDialog(final ContactTreeViewComponent parent) {
        this.parent = parent;
        createAddContactDialog();
    }

    private void createAddContactDialog() {
        final VBox box = new VBox();
        box.getStyleClass().add("jacp-option-pane");
        box.setMaxSize(300, Region.USE_PREF_SIZE);
        // the title
        final Label title = new Label("Add new category");
        title.setId(GlobalConstants.CSSConstants.ID_JACP_CUSTOM_TITLE);
        VBox.setMargin(title, new Insets(2, 2, 10, 2));

        final HBox hboxInput = new HBox();
        final Label nameLabel = new Label("category name:");
        HBox.setMargin(nameLabel, new Insets(2));
        final TextField nameInput = new TextField();
        HBox.setMargin(nameInput, new Insets(0, 0, 0, 5));
        HBox.setHgrow(nameInput, Priority.ALWAYS);
        hboxInput.getChildren().addAll(nameLabel, nameInput);

        final HBox hboxButtons = new HBox();
        hboxButtons.setAlignment(Pos.CENTER_RIGHT);
        final Button ok = new Button("OK");
        HBox.setMargin(ok, new Insets(6, 5, 4, 2));
        final Button cancel = new Button("Cancel");
        HBox.setMargin(cancel, new Insets(6, 2, 4, 5));
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                JACPModalDialog.getInstance().hideModalDialog();
            }
        });

        ok.setDefaultButton(true);
        ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent actionEvent) {
                final String catName = nameInput.getText();
                if (catName != null && StringUtils.hasText(catName)) {
                    // contacts
                    final Contact contact = new Contact();
                    contact.setFirstName(catName);
                    final IActionListener<EventHandler<Event>, Event, Object> listener = parent.getContext().getActionListener(contact);
                    listener.performAction(actionEvent);
                    JACPModalDialog.getInstance().hideModalDialog();
                }
            }
        });

        hboxButtons.getChildren().addAll(ok, cancel);
        box.getChildren().addAll(title, hboxInput, hboxButtons);
        JACPModalDialog.getInstance().showModalDialog(box);
    }

}
