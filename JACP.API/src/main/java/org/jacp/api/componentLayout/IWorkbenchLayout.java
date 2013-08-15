/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IWorkbenchLayout.java]
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
package org.jacp.api.componentLayout;

import org.jacp.api.util.ToolbarPosition;
import org.jacp.api.util.Tupel;

/**
 * Defines the base layout of a workbench and the application.
 * 
 * @param <C>
 *            defines the base component where others extend from
 * @author Andy Moncsek
 */
public interface IWorkbenchLayout<C> extends IBaseLayout<C> {

	/**
	 * Check if menus are enabled.
	 * 
	 * @return if menu is enable/disable
	 */
	boolean isMenuEnabled();

	/**
	 * Set menus to enabled state.
	 * 
	 * @param enabled
	 */
	void setMenuEnabled(boolean enabled);

	/**
	 * Set the size of the workbench.
	 * 
	 * @param x
	 * @param y
	 */
	void setWorkbenchXYSize(int x, int y);

	/**
	 * Returns a tuple defining the workbench size.
	 * 
	 * @return the tuple containing the workbench size
	 */
	Tupel<Integer, Integer> getWorkbenchSize();

	/**
	 * Register a toolbar for the workbench
	 * 
	 * All toolbars are added with the same priority, thus the priority is given
	 * by the order of registration.
	 * 
	 * @param position
	 *            - NORTH, WEST, EAST, SOUTH
	 * 
	 */
	void registerToolBar(final ToolbarPosition position);

	/**
	 * Set the workbench style.
	 * 
	 * @param style
	 *            , the style of workbench
	 */
	@SuppressWarnings("rawtypes")
	<S extends Enum> void setStyle(S style);

	/**
	 * Returns the workbench style.
	 * 
	 * @return style
	 */
	@SuppressWarnings("rawtypes")
	<S extends Enum> S getStyle();

}
