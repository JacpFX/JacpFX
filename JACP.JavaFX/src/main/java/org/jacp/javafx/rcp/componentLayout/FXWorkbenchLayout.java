/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FX2WorkbenchLayout.java]
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
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.api.util.Tupel;
import org.jacp.javafx.rcp.components.menuBar.JACPMenuBar;
import org.jacp.javafx.rcp.components.toolBar.JACPToolBar;

import java.util.Map;
import java.util.TreeMap;

/**
 * defines basic layout of workbench; define if menus are enabled; declare tool
 * bars; set workbench size
 * 
 * @author Andy Moncsek
 */
public class FXWorkbenchLayout implements IWorkbenchLayout<Node> {

	private boolean menueEnabled;
	private final Tupel<Integer, Integer> size = new Tupel<>();
	private final Map<ToolbarPosition, JACPToolBar> registeredToolbars = new TreeMap<>();
	private JACPMenuBar menu;
	private Pane glassPane;
	private StageStyle style = StageStyle.DECORATED;

	@Override
	public boolean isMenuEnabled() {
		return this.menueEnabled;
	}

	@Override
	public void setMenuEnabled(final boolean enabled) {
		this.menueEnabled = enabled;
		if (enabled && this.menu == null) {
			this.menu = new JACPMenuBar();
			this.menu.setId("main-menu");
			checkWindowButtons();
		}
	}

	@Override
	public void setWorkbenchXYSize(final int x, final int y) {
		this.size.setX(x);
		this.size.setY(y);
	}

	@Override
	public Tupel<Integer, Integer> getWorkbenchSize() {
		return this.size;
	}

	private JACPToolBar initToolBar(final ToolbarPosition position) {
		final JACPToolBar bar = new JACPToolBar();
		bar.setId(position.getName() + "-bar");
		return bar;
	}

	@Override
	public void registerToolBar(final ToolbarPosition position) {
		this.registeredToolbars.put(position, this.initToolBar(position));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <S extends Enum> void setStyle(final S style) {
		this.style = (StageStyle) style;
		checkWindowButtons();
	}

	private void checkWindowButtons() {
		if (this.menu != null && StageStyle.DECORATED.equals(style))
			this.menu.deregisterWindowButtons();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <S extends Enum> S getStyle() {
		return (S) this.style;
	}

	@Override
	public JACPMenuBar getMenu() {
		return this.menu;
	}

	/**
	 * Gets the registered toolbars.
	 * 
	 * @return the registered toolbars
	 */
	public Map<ToolbarPosition, JACPToolBar> getRegisteredToolbars() {
		return this.registeredToolbars;
	}

	@Override
	public JACPToolBar getRegisteredToolBar(final ToolbarPosition position) {
		return this.registeredToolbars.get(position);
	}

	@Override
	public Pane getGlassPane() {
		if (this.glassPane == null) {
			this.glassPane = new Pane();
		}
		return this.glassPane;
	}

}
