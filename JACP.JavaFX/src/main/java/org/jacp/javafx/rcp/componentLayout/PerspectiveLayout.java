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
import org.jacp.api.componentLayout.IPerspectiveLayout;
import org.jacp.javafx.rcp.util.Checkable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * The basic perspective layout to set the root and the child nodes of a perspective. Use registerChildNodes to define and register containers where subcomponents can be added.
 * 
 * @author Andy Moncsek
 */
public abstract class PerspectiveLayout extends Checkable implements IPerspectiveLayout<Node, Node> {
	Node rootComponent;
	private volatile  Map<String, Node> targetComponents = new ConcurrentHashMap<>();
	
	public PerspectiveLayout() {
		
	}
	
	public PerspectiveLayout(final Node rootComponent) {
		super.started=true;
		this.rootComponent = rootComponent;
	}	

	@Override
	public final Node getRootComponent() {
		return this.rootComponent;
	}

	@Override
	public final Map<String, Node> getTargetLayoutComponents() {
		return this.targetComponents;
	}

	@Override
	public final void registerTargetLayoutComponent(final String id, final Node target) {
		this.targetComponents.put(id, target);
	}
}
