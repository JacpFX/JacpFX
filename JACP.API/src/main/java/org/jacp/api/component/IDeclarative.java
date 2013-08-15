/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IVComponent.java]
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
package org.jacp.api.component;

import org.jacp.api.util.UIType;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Declarative Components always have a document URL and should have an resourceBundle.
 * 
 * @author Andy Moncsek
 */
public interface IDeclarative {
	/**
	 * Contains the document url describing the UI.
	 * 
	 * @return the document url
	 */
	String getViewLocation();

	/**
	 * Set the viewLocation location on component start.
	 * 
	 * @param documentURL , the url of the FXML document
	 */
	void setViewLocation(String documentURL);

	/**
	 * The document URL describing the UI.
	 * 
	 * @return the document url
	 */
	URL getDocumentURL();
	
	/**
	 * Contains locale-specific objects.
	 *
	 * @return the resource bundle for the UI document
	 */
    @Deprecated // TODO migrate Perspective
	ResourceBundle getResourceBundle();


	/**
	 * Distinguish component types.
	 * @return the type of the component.
	 */
	UIType getType();

}
