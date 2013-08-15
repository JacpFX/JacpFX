/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [JACPMenuStyle.java]
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
package org.jacp.api.util;

public enum JACPMenuStyle {

	DECORATED(1, "decorated"), UNDECORATED(2, "undecorated"), CUSTOM(3,
			"custom");

	private int id;

	private String description;

	private JACPMenuStyle(final int id, final String description) {

		this.id = id;
		this.description = description;
	}

	public int getId() {
		return this.id;
	}

	public String getDescription() {
		return this.description;
	}

}
