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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Component;
import org.jacp.api.annotations.PostConstruct;
import org.jacp.api.annotations.Resource;
import org.jacp.api.context.Context;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.demo.entity.Contact;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.component.FXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.components.toolBar.JACPToolBar;
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.util.FXUtil.MessageUtil;

/**
 * The ContactTreeViewComponent displays the contact category on the left side
 * of the application; It creates a "add category" button to add new categories
 * to view
 * 
 * @author Andy Moncsek Patrick Symmangk
 */
@Component(defaultExecutionTarget = "PleftMenu", id = "id001", name = "contactDemoTreeView", active = true)
public class ContactTreeViewComponent implements FXComponent {
	private final static Log LOGGER = LogFactory
			.getLog(ContactTreeViewComponent.class);
	private ContactTreeView pane;
	private ObservableList<Contact> contactList;
    @Resource
    private JACPContext context;

	@Override
	/**
	 * handle the component in worker thread
	 */
	public Node handle(final IAction<Event, Object> action) throws Exception {
		// on initial message create the layout outside the FXApplication thread
		if (action.isMessage(MessageUtil.INIT)) {
			return this.createInitialLayout();
		}
		LOGGER.debug("ContactTreeViewComponent handleAction message: "+action.getMessage());
		return null;
	}

	@Override
	/**
	 * handle the component in FX application thread
	 */
	public Node postHandle(final Node node,
			final IAction<Event, Object> action) throws Exception {
		// add a new contact in FXApplication thread
		if (action.isMessageType(Contact.class)) {
			final Contact contact = action.getTypedMessage(Contact.class);
			this.addNewContact(contact);
		}
		return this.pane;
	}

	

	/**
	 * handle menu an toolbar entries on component start up
	 */
	@PostConstruct
	public void PostConstructComponent(final FXComponentLayout layout) {
		final JACPToolBar north = layout
				.getRegisteredToolBar(ToolbarPosition.NORTH);
		final Button add = new Button("add category");
		add.getStyleClass().add("first");
		final ContactTreeViewComponent component = this;
		add.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {
				new ContactAddDialog(component);
			}

		});
		north.add(context.getId(), add);
	}

	private ContactTreeView createInitialLayout() {
		this.contactList = this.getCategoryList();
		this.pane = new ContactTreeView(this.contactList, this);
		return this.pane;
	}

	/**
	 * create dummy category list
	 * 
	 * @return
	 */
	private ObservableList<Contact> getCategoryList() {
		final ObservableList<Contact> categories = FXCollections
				.<Contact> observableArrayList();
		final Contact privateContact = new Contact();
		privateContact.setFirstName("private");
		final Contact publicContact = new Contact();
		publicContact.setFirstName("public");
		final Contact officeContact = new Contact();
		officeContact.setFirstName("office");
		categories.add(privateContact);
		categories.add(publicContact);
		categories.add(officeContact);
		return categories;
	}
	
	private void addNewContact(final Contact contact) {
		this.contactList.add(contact);
	}

    public JACPContext getContext(){
        return this.context;
    }
}
