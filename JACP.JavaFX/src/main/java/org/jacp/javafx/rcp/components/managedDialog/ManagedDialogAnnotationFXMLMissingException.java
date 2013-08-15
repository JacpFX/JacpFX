/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
 *
 * [ManagedDialogAnnotationFXMLMissingException.java]
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
package org.jacp.javafx.rcp.components.managedDialog;

/**
 * This exception will be thrown when the declared fxml url is invalid.
 * @author Andy Moncsek
 *
 */
public class ManagedDialogAnnotationFXMLMissingException extends IllegalArgumentException {


	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -526138568219746333L;





	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "@Dialog annotation, fxml attribute is missing";
	}


}
