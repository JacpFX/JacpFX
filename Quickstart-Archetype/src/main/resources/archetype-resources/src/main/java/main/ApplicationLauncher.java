#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package ${package}.main;

import java.net.URL;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.jacp.project.launcher.AFXSpringLauncher;

/**
 * The application launcher containing the main method
 * 
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 * 
 */
public class ApplicationLauncher extends AFXSpringLauncher {
	private static final Logger log = Logger.getLogger(ApplicationLauncher.class
			.getName());
	public static final String[] STYLES= new String[2];
	private static final String[] STYLE_FILES={"/styles/style_light.css","/styles/style_dark.css"};
	/// binary style sheets created while deployment
	private static final String[] BINARY_FILES={"/styles/style_light.bss","/styles/style_dark.bss"};

	public ApplicationLauncher() {
		super("main.xml");
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		Application.launch(args);
	}

	@Override
	public void postInit(final Stage stage) {
		initStyles();
		stage.setMinHeight(580);
		stage.setMinWidth(800);
		final Scene scene = stage.getScene();
		stage.getIcons().add(new Image("images/icons/JACP_512_512.png"));
		// add style sheet
		scene.getStylesheets().add(STYLES[0]);
	}
	
	private static void initStyles() {
		for(int i=0;i<2;i++) {
			URL res = ApplicationLauncher.class.getResource(BINARY_FILES[i]);
			if(res==null)
				res = ApplicationLauncher.class.getResource(STYLE_FILES[i]);			
			STYLES[i] = res.toExternalForm();
			log.info("found: "+ STYLES[i]+" stylesheet");
		}
		
	}
	
	

}
