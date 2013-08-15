/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ContentGenerator.java]
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
package org.jacp.demo.callbacks;

import java.util.Random;

import org.jacp.demo.common.GenderType;
import org.jacp.demo.entity.Contact;
import org.jacp.demo.entity.ContactDTO;

public class ContentGenerator {
	private static final Random rnd = new Random();

	private static final String[] genderNames = { GenderType.MALE.getLabel(),
			GenderType.MALE.getLabel(), GenderType.MALE.getLabel(),
			GenderType.FEMALE.getLabel(), GenderType.MALE.getLabel(),
			GenderType.MALE.getLabel(), GenderType.MALE.getLabel(),
			GenderType.MALE.getLabel(), GenderType.MALE.getLabel(),
			GenderType.MALE.getLabel(), GenderType.MALE.getLabel(),
			GenderType.FEMALE.getLabel() };
	private static final String[] firstNames = { "Walter", "Hans", "Peter",
			"Maria", "Heinrich", "Ray", "Richard", "Ulysses", "Robert",
			"Daniel", "Robin", "Johana" };
	private static final String[] lastNames = { "Jung", "Matt", "D. Steinberg",
			"Roth", "Dittrich", "Peterson", "P. Phillips", "Holz", "Boll",
			"E. Morris", "J. Smith", "Hopp" };
	private static final String[] address = { "Concord Street",
			"Trails End Road 3", "Hillside Drive", "Columbia Boulevard",
			"Teresien Str. 22", "Leisure Lane 4", "Simpson Street 1",
			"Primrose Lane 3", "Lewis Street", "Schlierenbch 10",
			"Marcus Street 55", "Zuerichsee 3" };
	private static final String[] zip = { "0234", "2343", "3345", "2346",
			"2342", "22334", "4432", "2344", "4432", "2342", "234223", "2342" };
	private static final String[] phones = { "34535453453", "345345345",
			"45645654", "47645634", "23452342", "23423423", "234234234",
			"234234211", "446564", "475457433", "2343413", "4645456456" };
	private static final String[] country = { "DE", "US", "RU", "BE", "CH",
			"GB", "FR", "DE", "US", "RU", "BE", "FR" };

	/**
	 * create a contact entry
	 * 
	 * @param dto
	 * @return
	 */
	public static ContactDTO createEntries(final ContactDTO dto) {
		final int amount = dto.getAmount();
		for (int i = 0; i < amount; i++) {
			final Contact contact = new Contact();
			int rdm = random();
			contact.setGender(genderNames[rdm]);
			contact.setFirstName(firstNames[rdm]);
			contact.setLastName(lastNames[random()]);
			contact.setAddress(address[random()]);
			contact.setZip(zip[random()]);
			contact.setPhoneNumber(phones[random()]);
			contact.setCountry(country[random()]);
			dto.getContacts().add(contact);
		}

		return dto;
	}

	// Common but flawed!
	private static int random() {
		return Math.abs(rnd.nextInt()) % firstNames.length;
	}

}
