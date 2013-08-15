/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ContactTreeView.java]
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

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import org.jacp.api.action.IActionListener;
import org.jacp.demo.constants.GlobalConstants;
import org.jacp.demo.entity.Contact;
import org.jacp.demo.enums.BarChartAction;

public class ContactTreeView extends ScrollPane {
    final ObservableList<Contact> contactList;
    final ContactTreeViewComponent parent;

    public ContactTreeView() {
        this.contactList = null;
        this.parent = null;
    }

    public ContactTreeView(final ObservableList<Contact> contactList, final ContactTreeViewComponent parent) {
        this.contactList = contactList;
        this.parent = parent;
        final GridPane gridPane = new GridPane();
        gridPane.getStyleClass().addAll("dark", "dark-border");
        this.getStyleClass().addAll("dark-scrollpane");
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        GridPane.setHgrow(this, Priority.ALWAYS);
        GridPane.setVgrow(this, Priority.ALWAYS);
        this.setContent(gridPane);

        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // the label
        final Label categoryLbl = new Label("Category");
        categoryLbl.getStyleClass().addAll("light-label", "list-label");
        GridPane.setHalignment(categoryLbl, HPos.CENTER);
        gridPane.add(categoryLbl, 0, 0);

        final ListView<Contact> categoryListView = this.createList();
        GridPane.setHgrow(categoryListView, Priority.ALWAYS);
        GridPane.setVgrow(categoryListView, Priority.ALWAYS);
        gridPane.add(categoryListView, 0, 1);
        GridPane.setMargin(categoryListView, new Insets(0, 10, 10, 10));
    }

    private ListView<Contact> createList() {
        final ListView<Contact> categoryListView = new ListView<Contact>(this.contactList);
        categoryListView.setCellFactory(new Callback<ListView<Contact>, ListCell<Contact>>() {

            @Override
            public ListCell<Contact> call(final ListView<Contact> arg0) {

                final HBox box = new HBox();

                final ListCell<Contact> cell = new ListCell<Contact>() {
                    @Override
                    public void updateItem(final Contact contact, final boolean emty) {
                        super.updateItem(contact, emty);
                        if (contact != null) {
                            final Label label = new Label();
                            label.getStyleClass().add("dark-text");
                            label.setText(contact.getFirstName());
                            final Pane spacer = new Pane();
                            ContactTreeView.this.configureProgressBar(contact);
                            HBox.setMargin(contact.getProgress(), new Insets(3, 0, 0, 0));
                            HBox.setHgrow(spacer, Priority.ALWAYS);
                            box.getChildren().addAll(label, spacer);// contact.getProgress());

                            this.setGraphic(box);
                            this.setOnMouseClicked(new EventHandler<Event>() {

                                @Override
                                public void handle(final Event event) {
                                    // send contact to TableView
                                    // component to show containing
                                    // contacts
                                    // send event to Table View
                                    final IActionListener<EventHandler<Event>, Event, Object> listener = ContactTreeView.this.parent.getContext().getActionListener(
                                            GlobalConstants.cascade(GlobalConstants.PerspectiveConstants.DEMO_PERSPECTIVE, GlobalConstants.ComponentConstants.COMPONENT_TABLE_VIEW), contact);
                                    listener.performAction(event);
                                    // Send Event to BarChart
                                    final IActionListener<EventHandler<Event>, Event, Object> resetListener = ContactTreeView.this.parent.getContext().getActionListener(
                                            GlobalConstants.cascade(GlobalConstants.PerspectiveConstants.DEMO_PERSPECTIVE, GlobalConstants.ComponentConstants.COMPONENT_CHART_VIEW),
                                            BarChartAction.RESET);
                                    resetListener.performAction(event);
                                }
                            });
                        }

                    }
                }; // ListCell
                return cell;
            }
        });

        return categoryListView;
    }

    private void configureProgressBar(final Contact contact) {
        if (contact.getProgress() == null) {
            final ProgressBar progressBar = new ProgressBar();
            progressBar.getStyleClass().add("jacp-progress-bar");
            contact.setProgress(progressBar);
        }
        if (contact.getContacts().isEmpty()) {
            contact.getProgress().setVisible(false);
        } else {
            contact.getProgress().setVisible(true);
        }
    }

}
