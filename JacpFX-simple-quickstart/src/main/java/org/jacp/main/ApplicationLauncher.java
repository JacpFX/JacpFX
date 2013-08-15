/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ApplicationLauncher.java]
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
package org.jacp.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.jacp.project.launcher.AFXSpringLauncher;
/**
 * The application launcher containing the main method
 * @author Andy Moncsek
 *
 */
public class ApplicationLauncher extends AFXSpringLauncher {


	public ApplicationLauncher() {
		super("main.xml");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void postInit(Stage stage) {
		Scene scene = stage.getScene();
		// add style sheet
		scene.getStylesheets().addAll(
				ApplicationLauncher.class.getResource("/styles/style.css")
						.toExternalForm());
	}

}
