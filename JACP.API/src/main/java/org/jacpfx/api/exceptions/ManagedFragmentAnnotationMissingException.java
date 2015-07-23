/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ManagedFragmentAnnotationMissingException.java]
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
package org.jacpfx.api.exceptions;

/**
 * Exception will be thrown when class without Fragment annotation will be passed
 * to ManagedDialog util.
 * 
 * @author Andy Moncsek
 * 
 */
public class ManagedFragmentAnnotationMissingException extends
		IllegalArgumentException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7005405996574384879L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessageBody()
	 */
	@Override
	public String getMessage() {
		return "@Fragment annotation is missing";
	}

}
