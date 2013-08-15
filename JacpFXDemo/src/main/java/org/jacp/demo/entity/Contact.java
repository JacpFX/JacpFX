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
package org.jacp.demo.entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressIndicator;

import org.jacp.demo.common.GenderType;

/**
 * Simple contact entity
 * 
 * @author Andy Moncsek
 * 
 */
public class Contact {
	private final StringProperty firstName = new SimpleStringProperty();
	private final StringProperty lastName = new SimpleStringProperty();
	private final StringProperty zip = new SimpleStringProperty();
	private final StringProperty address = new SimpleStringProperty();
	private final StringProperty country = new SimpleStringProperty();
	private final StringProperty phoneNumber = new SimpleStringProperty();
	private final StringProperty gender = new SimpleStringProperty();
	private int amount = 0;
	private ContactDTO dto;
	private boolean empty = true;
	private ProgressIndicator progress;
	private final ObservableList<Contact> contacts = FXCollections
			.<Contact> observableArrayList();

	public Contact() {
	}

	public Contact(final String firstName, final String lastName,
			final String zip, final String address, final String country,
			final String phoneNumber, final GenderType gender) {
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setZip(zip);
		this.setAddress(address);
		this.setCountry(country);
		this.setPhoneNumber(phoneNumber);
		this.setGender(gender.getLabel());
	}

	public ObservableList<Contact> getContacts() {
		return this.contacts;
	}

	public String getFirstName() {
		return this.firstName.get();
	}

	public void setFirstName(final String firstName) {
		this.firstName.set(firstName);
	}

	public String getLastName() {
		return this.lastName.get();
	}

	public void setLastName(final String lastName) {
		this.lastName.set(lastName);
	}

	public String getZip() {
		return this.zip.get();
	}

	public void setZip(final String zip) {
		this.zip.set(zip);
	}

	public String getGender() {
		return gender.get();
	}

	public void setGender(String gender) {
		this.gender.set(gender);
	}

	public String getAddress() {
		return this.address.get();
	}

	public void setAddress(final String address) {
		this.address.set(address);
	}

	public String getCountry() {
		return this.country.get();
	}

	public void setCountry(final String counry) {
		this.country.set(counry);
	}

	public String getPhoneNumber() {
		return this.phoneNumber.get();
	}

	public void setPhoneNumber(final String phoneNumber) {
		this.phoneNumber.set(phoneNumber);
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(final int amount) {
		this.amount = amount;
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public void setEmpty(final boolean empty) {
		this.empty = empty;
	}

	public ContactDTO getDto() {
		return this.dto;
	}

	public void setDto(final ContactDTO dto) {
		this.dto = dto;
	}

	@Override
	public String toString() {
		return "Contact [firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ", zip=" + this.getZip() + ", address=" + this.getAddress() + ", country=" + this.getCountry() + ", phoneNumber=" + this.getPhoneNumber() + ", gender =" + this.getGender() + "]";
	}

	public ProgressIndicator getProgress() {
		return this.progress;
	}

	public void setProgress(final ProgressIndicator progress) {
		this.progress = progress;
	}

}
