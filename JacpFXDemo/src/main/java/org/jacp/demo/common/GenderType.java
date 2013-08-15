/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [GenderType.java]
 * AHCP Project http://jacp.googlecode.com
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
package org.jacp.demo.common;

public enum GenderType {

	MALE("Mr."), FEMALE("Mrs.");

	private final String label;

	private GenderType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
