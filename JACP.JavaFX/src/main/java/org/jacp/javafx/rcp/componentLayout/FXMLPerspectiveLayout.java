/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FX2PerspectiveLayout.java]
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
package org.jacp.javafx.rcp.componentLayout;


import javafx.scene.Node;


/**
 * Configuration handler for perspective components, used in handle method for
 * configuration and registration of layout 'leaves' where subcomponents can
 * live in. Create your own complex layout, the root node is defined by FXML
 * definition. Mark your leaf nodes as privet @FXML components and register
 * them.
 * 
 * @author Andy Moncsek
 */
public class FXMLPerspectiveLayout extends PerspectiveLayout{

	public FXMLPerspectiveLayout(final Node rootComponent) {
		super(rootComponent);
	}
	
	@Override
	public final void registerRootComponent(final Node comp) {
		super.checkPolicy(this.rootComponent, "Do Not Set rootComponent manually in FXML components");
	}

}

