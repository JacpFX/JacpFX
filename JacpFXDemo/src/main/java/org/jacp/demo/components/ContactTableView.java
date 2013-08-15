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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.jacp.demo.entity.Contact;
import org.jacp.demo.main.Util;

/**
 * The view object returned in a "TableView"-CallbackComponent; each contact category is
 * associated to an contact view; this view contains the table and all contacts
 * to a parent contact.
 * 
 * @author Andy Moncsek Patrick Symmangk
 */
public class ContactTableView extends GridPane {
	private Contact contact;

	private ObservableList<Contact> lastSubList;
	private int currentPos = 0;
	private int maxPos;
	private Label position;
	private Button left;
	private Button posOne;
	private Button end;
	private Button right;

	private final TableView<Contact> contactTableView = new TableView<Contact>();

	/**
	 * update the "max page" value
	 */
	public void updateMaxValue() {
		if (this.getContact().getContacts().size() < Util.MAX) {
            this.maxPos = (this.getContact().getContacts().size() % Util.MAX)
                    / Util.PARTITION_SIZE;
		} else {
			this.end.disableProperty().bind(this.right.disableProperty());
		}
	}

	/**
	 * returns the next sublist
	 * 
	 * @return javafx.collections.ObservableList
	 */
	private ObservableList<Contact> getNextSublist() {
		final int start = this.getCurrentPos() * Util.PARTITION_SIZE;
		int end = (this.getCurrentPos() + 1) * Util.PARTITION_SIZE;
		if (this.getContact().getContacts().size() < end) {
			end = this.getContact().getContacts().size();
		}
		return this.getSubList(start, end);
	}

	/**
	 * returns the previous sublist
	 * 
	 * @return javafx.collections.ObservableList
	 */
	private ObservableList<Contact> getPrevSublist() {
		int end = (this.getCurrentPos() + 1) * Util.PARTITION_SIZE;
		int start = this.getCurrentPos() * Util.PARTITION_SIZE;
		if (start <= 0) {
			start = 0;
			end = Util.PARTITION_SIZE;
		}
		return this.getSubList(start, end);
	}

	/**
	 * returns a sublist
	 * 
	 * @param start
	 * @param end
	 * @return javafx.collections.ObservableList
	 */
	private ObservableList<Contact> getSubList(final int start, final int end) {
		final ObservableList<Contact> contacts = this.getContact()
				.getContacts();
		final ObservableList<Contact> contactsSublist = FXCollections
				.<Contact> observableArrayList();
		contactsSublist.addAll(contacts.subList(start, end));
		return contactsSublist;
	}

	/**
	 * returns the previous page position
	 * 
	 * @return int
	 */
	private int getPrevCorrectPosition() {
		int currentPos = this.getCurrentPos();
		if (currentPos > 1) {
			this.left.setDisable(false);
            --currentPos;
            return currentPos;
		} else if (currentPos == 1) {
			this.left.setDisable(true);
			return --currentPos;
		} else {
			this.left.setDisable(true);
		}
		return currentPos;
	}

	/**
	 * returns the next page position
	 * 
	 * @return int
	 */
	private int getNextCorrectPosition() {
		int currentPos = this.getCurrentPos();
		final int maxPos = this.getMaxPos();
		if (currentPos <= maxPos) {
			this.right.setDisable(false);
			this.left.setDisable(false);
			return ++currentPos;
		} else {
			this.right.setDisable(true);
		}

		return currentPos;
	}

	/**
	 * update label for navigation
	 */
	public synchronized void updatePositionLabel() {
		this.position.setText(" " + this.getCurrentPos() + " of " + " "
				+ this.getMaxPos() + " ");
		if (!this.getContact().getProgress().isVisible()) {
			this.getContact().getProgress().setVisible(true);
		}
		final Double current = Double.valueOf(String.valueOf(this.getMaxPos())) + 1;
		final Double max = Double.valueOf(Util.MAX + "");
		final Double part = Double.valueOf(Util.PARTITION_SIZE + "");
		final Double result = ((current * part) / max);
		this.getContact().getProgress().setProgress(result);
		if (this.getMaxPos() > 0 && this.getCurrentPos() != this.getMaxPos()) {
			this.right.setDisable(false);
		} else if (this.getCurrentPos() > 0) {
			this.left.setDisable(false);
		} else {
			this.right.setDisable(true);
			this.left.setDisable(true);
		}
	}

	public void createInitialTableViewLayout(final Contact contact) {
		this.contact = contact;
		this.setAlignment(Pos.CENTER);
		this.getStyleClass().add("dark");
		final GridPane gridPane = new GridPane();
		// create the scroll pane an set resize to max

		GridPane.setHgrow(this, Priority.ALWAYS);
		GridPane.setVgrow(this, Priority.ALWAYS);

		// create the gridpane containing the label, the navigation and the
		// table
		gridPane.setPadding(new Insets(10, 8, 10, 18));
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		this.getChildren().add(gridPane);

		// create the table navigation

		final BorderPane mainLayout = new BorderPane();
		final HBox header = new HBox();
		final HBox mainLabel = new HBox();

		// the label
		String labelSuffix = "";
		if (this.contact != null) {
			labelSuffix = this.contact.getFirstName();
		}

		final Label categoryLbl = new Label(new StringBuilder("Contacts ")
				.append(labelSuffix).toString());

		HBox.setHgrow(categoryLbl, Priority.ALWAYS);

		categoryLbl.getStyleClass().add("light-label");
		categoryLbl.setAlignment(Pos.CENTER);

		mainLabel.getChildren().addAll(categoryLbl);

		final HBox nav = this.createTablePageNavigation();
		nav.setAlignment(Pos.CENTER_RIGHT);
		mainLabel.setAlignment(Pos.CENTER);
		HBox.setHgrow(mainLabel, Priority.ALWAYS);

		HBox.setHgrow(nav, Priority.ALWAYS);

		header.setId("header");
		header.getChildren().addAll(mainLabel);

		// create the table
		this.createTable();

		// container for paginate and table
		final VBox div = new VBox(2);

		div.getStyleClass().add("table-content");
		HBox.setHgrow(div, Priority.ALWAYS);
		this.setFullspanConstraint(div);

		GridPane.setHalignment(this.contactTableView, HPos.CENTER);
		div.getChildren().addAll(nav, this.contactTableView);

		gridPane.add(div, 0, 0);

		mainLayout.setTop(header);
		mainLayout.setCenter(gridPane);

		this.setFullspanConstraint(mainLayout);
		this.getChildren().add(mainLayout);

	}

	private HBox createTablePageNavigation() {
		final HBox nav = new HBox();
		this.posOne = new Button("<<");
		this.posOne.setTooltip(new Tooltip("Position One"));
		this.left = new Button("<");
		this.left.setDisable(true);
		this.left.setTooltip(new Tooltip("Pref"));
		this.posOne.disableProperty().bind(this.left.disableProperty());
		this.end = new Button(">>");
		this.end.setDisable(true);
		this.end.setTooltip(new Tooltip("End"));

		this.end.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(final Event arg0) {
				ContactTableView.this.currentPos = ContactTableView.this
						.getMaxPos();
				ContactTableView.this.setLastSubList(ContactTableView.this
						.getNextSublist());
				ContactTableView.this.updateTableContent(ContactTableView.this
						.getLastSubList());
				ContactTableView.this.right.setDisable(true);

			}
		});

		this.right = new Button(">");
		this.right.setTooltip(new Tooltip("Next"));
		this.right.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(final Event arg0) {
				ContactTableView.this.currentPos = ContactTableView.this
						.getNextCorrectPosition();
				ContactTableView.this.setLastSubList(ContactTableView.this
						.getNextSublist());
				ContactTableView.this.updateTableContent(ContactTableView.this
						.getLastSubList());
				ContactTableView.this.right
						.setDisable(ContactTableView.this.currentPos == ContactTableView.this
								.getMaxPos());

			}
		});

		this.posOne.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(final Event arg0) {
				ContactTableView.this.currentPos = 0;
				ContactTableView.this.setLastSubList(ContactTableView.this
						.getNextSublist());
				ContactTableView.this.updateTableContent(ContactTableView.this
						.getLastSubList());
				ContactTableView.this.left.setDisable(true);

			}
		});

		this.left.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(final Event arg0) {
				ContactTableView.this.currentPos = ContactTableView.this
						.getPrevCorrectPosition();
				ContactTableView.this.setLastSubList(ContactTableView.this
						.getPrevSublist());
				ContactTableView.this.updateTableContent(ContactTableView.this
						.getLastSubList());

			}
		});

		this.right.setDisable(true);
		this.position = new Label("0 of 0");
		nav.getStyleClass().add("paginate");
		nav.getChildren().add(this.posOne);
		nav.getChildren().add(this.left);
		nav.getChildren().add(this.position);
		nav.getChildren().add(this.right);
		nav.getChildren().add(this.end);
		nav.setAlignment(Pos.TOP_RIGHT);
		nav.getStyleClass().add("light-label");

		return nav;
	}

	/**
	 * create initial UI
	 * 
	 * @return javafx.scene.layout.GridPane
	 */
	public GridPane getTableViewLayout() {
		return this;
	}

	private void setFullspanConstraint(final Node n) {
		GridPane.setVgrow(n, Priority.ALWAYS);
		GridPane.setHgrow(n, Priority.ALWAYS);
	}

	private void updateTableContent(final ObservableList<Contact> content) {
		this.updatePositionLabel();
		this.contactTableView.setItems(content);
	}

	/**
	 * create the initial table view
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createTable() {
		final ObservableList<Contact> category = FXCollections
				.<Contact> observableArrayList();
		this.contactTableView.autosize();
		this.contactTableView.setItems(category);
		this.contactTableView.setTableMenuButtonVisible(true);
		this.contactTableView.setEditable(true);

		this.contactTableView.setCache(false);

		final TableColumn<Contact, String> genderColumn = this.createColumn(
				"Gender", "gender");
		this.setColumnConstraints(genderColumn,
				TableColumn.SortType.DESCENDING, 30);
		genderColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setGender(t.getNewValue());
					}
				});

		final TableColumn<Contact, String> firstNameColumn = this.createColumn(
				"First name", "firstName");
		this.setColumnConstraints(firstNameColumn,
				TableColumn.SortType.DESCENDING, 120);
		firstNameColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setFirstName(t.getNewValue());
					}
				});

		final TableColumn<Contact, String> lastNameColumn = this.createColumn(
				"Last name", "lastName");
		lastNameColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setLastName(t.getNewValue());
					}
				});
		this.setColumnConstraints(lastNameColumn,
				TableColumn.SortType.DESCENDING, 120);

		final TableColumn nameCol = new TableColumn("Name");
		nameCol.getColumns().addAll(firstNameColumn, lastNameColumn);

		final TableColumn<Contact, String> zipColumn = this.createColumn("Zip",
				"zip");
		zipColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setZip(t.getNewValue());
					}
				});
		this.setColumnConstraints(zipColumn, TableColumn.SortType.DESCENDING,
				60);

		final TableColumn<Contact, String> addressColumn = this.createColumn(
				"Address", "address");
		addressColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setAddress(t.getNewValue());
					}
				});
		this.setColumnConstraints(addressColumn,
				TableColumn.SortType.DESCENDING, 150);

		final TableColumn<Contact, String> countryColumn = this.createColumn(
				"Country", "country");
		countryColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setCountry(t.getNewValue());
					}
				});
		this.setColumnConstraints(countryColumn,
				TableColumn.SortType.DESCENDING, 60);

		final TableColumn<Contact, String> phoneNumberColumn = this
				.createColumn("Phone number", "phoneNumber");
		phoneNumberColumn
				.setOnEditCommit(new EventHandler<CellEditEvent<Contact, String>>() {
					@Override
					public void handle(final CellEditEvent<Contact, String> t) {
						t.getTableView().getItems()
								.get(t.getTablePosition().getRow())
								.setPhoneNumber(t.getNewValue());
					}
				});
		this.setColumnConstraints(phoneNumberColumn,
				TableColumn.SortType.DESCENDING, 150);

		this.contactTableView.getColumns().addAll(genderColumn, nameCol,
				zipColumn, addressColumn, countryColumn, phoneNumberColumn);

	}

	private void setColumnConstraints(
			final TableColumn<Contact, String> column,
			final TableColumn.SortType sortType, final double width) {
		column.setSortType(sortType);
		column.prefWidthProperty().bind(
				this.contactTableView.widthProperty().divide(7).subtract(1));
	}

	/**
	 * create a single table column
	 * 
	 * @param name
	 * @param binding
	 * @return javafx.scene.control.TableColumn
	 */
	private TableColumn<Contact, String> createColumn(final String name,
			final String binding) {
		final Callback<TableColumn<Contact, String>, TableCell<Contact, String>> cellFactory = new Callback<TableColumn<Contact, String>, TableCell<Contact, String>>() {
			@Override
			public TableCell<Contact, String> call(
					final TableColumn<Contact, String> p) {
				return new EditingCell();
			}
		};
		final TableColumn<Contact, String> column = new TableColumn<Contact, String>(
				name);
		column.setCellValueFactory(new PropertyValueFactory<Contact, String>(
				binding));
		column.setCellFactory(cellFactory);
		return column;

	}

	class EditingCell extends TableCell<Contact, String> {
		private TextField textField;

		public EditingCell() {

		}

		@Override
		public void startEdit() {
			super.startEdit();
			if (this.isEmpty()) {
				return;
			}

			if (this.textField == null) {
				this.createTextField();
			} else {
				this.textField.setText(this.getItem());
			}
			this.setGraphic(this.textField);
			this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			this.setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override
		public void updateItem(final String item, final boolean empty) {
			super.updateItem(item, empty);
			if (!this.isEmpty()) {
				if (this.textField != null) {
					this.textField.setText(item);
				}
				this.setText(item);
			}
		}

		private void createTextField() {
			this.textField = new TextField(this.getItem());
			this.textField.setMinWidth(this.getWidth()
					- this.getGraphicTextGap() * 2);
			this.textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(final KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						EditingCell.this.commitEdit(EditingCell.this.textField
								.getText());
					} else if (t.getCode() == KeyCode.ESCAPE) {
						EditingCell.this.cancelEdit();
					}
				}
			});
		}
	}

	/**
	 * get parent contact (category)
	 * 
	 * @return org.jacp.demo.entity.Contact
	 */
	public Contact getContact() {
		return this.contact;
	}

	/**
	 * set parent contact (category)
	 * 
	 * @param current
	 */
	public void setContact(final Contact current) {
		this.contact = current;
	}

	/**
	 * get current page position
	 * 
	 * @return int
	 */
	public int getCurrentPos() {
		return this.currentPos;
	}

	/**
	 * set current page position
	 * 
	 * @param currentPos
	 */
	public void setCurrentPos(final int currentPos) {
		this.currentPos = currentPos;
	}

	/**
	 * get "max pages" in table view
	 * 
	 * @return int
	 */
	public int getMaxPos() {
		return this.maxPos;
	}

	/**
	 * set "max pages" in table view
	 * 
	 * @param maxPos
	 */
	public void setMaxPos(final int maxPos) {
		this.maxPos = maxPos;
	}

	/**
	 * get last selected sublist
	 * 
	 * @return javafx.collections.ObservableList
	 */
	public ObservableList<Contact> getLastSubList() {
		return this.lastSubList;
	}

	/**
	 * set last selected sublist
	 * 
	 * @param lastSubList
	 */
	public void setLastSubList(final ObservableList<Contact> lastSubList) {
		this.lastSubList = lastSubList;
	}

	/**
	 * returns the contact table view
	 * 
	 * @return javafx.scene.control.TableView
	 */
	public TableView<Contact> getContactTableView() {
		return this.contactTableView;
	}

}
