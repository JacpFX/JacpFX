/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
 *
 * [ManagedDialogHandler.java]
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

import javafx.scene.Node;

/**
 * A managedDialogHandler is the return value of a JACP managed dialog. It wraps
 * access to the managed controller class and the JavaFX root Node of the
 * dialog. A controller must be annotated with @Dialog and can either extend a
 * JavaFX node or specify the url to fxml in the @Dialog annotation. In both
 * cases the result is a controller and a JavaFX Node. if not using fxml the
 * controller and the root node are the same.
 * 
 * @author Andy Moncsek
 * 
 * @param <T>
 */
public class ManagedDialogHandler<T> {

	private final T controller;
	private final Node dialogNode;
	private final String id;

	public ManagedDialogHandler(T controller, Node dialogNode,final String id) {
		this.controller = controller;
		this.dialogNode = dialogNode;
		this.id = id;
	}
	/**
	 * Returns the controller instance.
	 * @return
	 */
	public T getController() {
		return this.controller;
	}
	/**
	 * Returns the JavaFX Node
	 * @return
	 */
	public Node getDialogNode() {
		return this.dialogNode;
	}
	/**
	 * Returns the Id.
	 * @return
	 */
	public String getId() {
		return id;
	}

}
