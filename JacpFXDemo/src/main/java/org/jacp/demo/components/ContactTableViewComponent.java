/*
 * Copyright (C) 2010 - 2012.
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
package org.jacp.demo.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;
import org.jacp.api.annotations.Component;
import org.jacp.api.annotations.Resource;
import org.jacp.demo.constants.GlobalConstants;
import org.jacp.demo.entity.Contact;
import org.jacp.demo.entity.ContactDTO;
import org.jacp.demo.main.Util;
import org.jacp.javafx.rcp.component.FXComponent;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.controls.optionPane.JACPDialogButton;
import org.jacp.javafx.rcp.controls.optionPane.JACPDialogUtil;
import org.jacp.javafx.rcp.controls.optionPane.JACPOptionPane;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;

/**
 * The ContactTableViewComponent create the table view for an category
 * 
 * @author Andy Moncsek
 * 
 */
@Component(defaultExecutionTarget = "PmainContentTop", id = GlobalConstants.ComponentConstants.COMPONENT_TABLE_VIEW, name = "contactDemoTableView", active = true)
public class ContactTableViewComponent implements FXComponent {
	private final static Log LOGGER = LogFactory
			.getLog(ContactTableViewComponent.class);
    private final Map<String, ContactTableView> all = Collections.synchronizedMap(new HashMap<String, ContactTableView>());
    private ContactTableView current;
    @Resource
    private JACPContext context;
    @Override
    /**
     * run handleAction in worker Thread
     */
    public Node handle(final IAction<Event, Object> action) throws Exception {
        return null;
    }

    @Override
    /**
     * run postHandle in FX application Thread, use this method to update UI code
     */
    public Node postHandle(final Node node, final IAction<Event, Object> action) throws Exception {
        if (action.getMessage() instanceof Contact) {
            // contact selected
            final Contact contact = (Contact) action.getMessage();
            if (contact.isEmpty()) {
                this.showDialogIfEmpty(contact);
            }
            this.current = this.getView(contact);

        } else if (action.getMessage() instanceof ContactDTO) {
            // contact data received
            final ContactDTO dto = (ContactDTO) action.getMessage();
            final ContactTableView view = this.all.get(dto.getParentName());
            // add first 1000 entries directly to table
            if (view.getContactTableView().getItems().size() < Util.PARTITION_SIZE) {
                view.getContactTableView().getItems().addAll(dto.getContacts());
            } else {
                // all other entries are added to list for paging
                this.updateContactList(view, dto.getContacts());
            }
            view.updatePositionLabel();

        } else if (action.getMessage().equals(MessageUtil.INIT)) {
            return this.getView(null).getTableViewLayout();
        }
        return this.current.getTableViewLayout();
    }

    private Callback<TableView<Contact>, TableRow<Contact>> createRowCallback() {
        return new Callback<TableView<Contact>, TableRow<Contact>>() {

            @Override
            public TableRow<Contact> call(final TableView<Contact> arg0) {
                final TableRow<Contact> row = new TableRow<Contact>() {
                    @Override
                    public void updateItem(final Contact contact, final boolean emty) {
                        super.updateItem(contact, emty);
                        if (contact != null) {
                            this.setOnMouseClicked(new EventHandler<Event>() {
                                @Override
                                public void handle(final Event arg0) {
                                    // send contact to TableView
                                    // component to show containing
                                    // contacts
                                    final IActionListener<EventHandler<Event>, Event, Object> listener =context.getActionListener(
                                            GlobalConstants.cascade(GlobalConstants.PerspectiveConstants.DEMO_PERSPECTIVE, GlobalConstants.CallbackConstants.CALLBACK_ANALYTICS), contact);
                                    listener.performAction(arg0);
                                    final IActionListener<EventHandler<Event>, Event, Object> detailListener = context.getActionListener(
                                            GlobalConstants.cascade(GlobalConstants.PerspectiveConstants.DEMO_PERSPECTIVE, GlobalConstants.ComponentConstants.COMPONENT_DETAIL_VIEW), contact);
                                    detailListener.performAction(arg0);

                                }
                            });
                        }
                    }
                };
                return row;
            }

        };
    }

    private void updateContactList(final ContactTableView view, final ObservableList<Contact> list) {
        // add chunk of contact list to contact
        view.getContact().getContacts().addAll(list);
        view.updateMaxValue();
    }

    private ContactTableView getView(final Contact contact) {
        ContactTableView view = null;
        if (contact == null) {
            view = this.createView(null);
        } else if (!this.all.containsKey(contact.getFirstName())) {
            view = this.createView(contact);
            this.all.put(contact.getFirstName(), view);
        } else if (contact != null) {
            view = this.all.get(contact.getFirstName());
        }
        return view;
    }

    private ContactTableView createView(final Contact contact) {
        final ContactTableView view = new ContactTableView();
        view.createInitialTableViewLayout(contact);
        view.getContactTableView().setRowFactory(this.createRowCallback());
        return view;
    }

    private void showDialogIfEmpty(final Contact contact) {
        // show popup to ask how many contacts to create
        final JACPOptionPane dialog = JACPDialogUtil.createOptionPane("Contact Demo Pane", "Currently are no contact in this category available. Do you want to create " + Util.MAX + " contacts?");
        dialog.setDefaultButton(JACPDialogButton.NO);
        dialog.setDefaultCloseButtonVisible(true);

        dialog.setOnYesAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                contact.setAmount(Util.MAX);
                contact.setEmpty(false);
                // redirect contact to coordinator callback to create
                // contacts
                final IActionListener<EventHandler<Event>, Event, Object> listener = context.getActionListener("id01.id004", contact);
                listener.performAction(arg0);
            }
        });

        dialog.setOnNoAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent arg0) {

            }
        });
        JACPModalDialog.getInstance().showModalDialog(dialog);

    }

}
