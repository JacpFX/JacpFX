/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [Workbench.java]
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
package org.jacp.workbench;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jacp.api.action.IAction;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.components.menuBar.JACPMenuBar;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;
import org.jacp.javafx.rcp.controls.optionPane.JACPDialogButton;
import org.jacp.javafx.rcp.controls.optionPane.JACPDialogUtil;
import org.jacp.javafx.rcp.controls.optionPane.JACPOptionPane;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;

/**
 * A simple JacpFX workbench
 * 
 * @author Andy Moncsek
 * 
 */
public class Workbench extends AFXWorkbench {

	@Override
	public void handleInitialLayout(IAction<Event, Object> action, IWorkbenchLayout<Node> layout, Stage stage) {
		layout.setWorkbenchXYSize(1024, 768);
		layout.registerToolBar(ToolbarPosition.NORTH);
		layout.setStyle(StageStyle.DECORATED);
		layout.setMenuEnabled(true);

	}

	@Override
	public void postHandle(FXComponentLayout layout) {
		final JACPMenuBar menu = layout.getMenu();
		final Menu menuFile = new Menu("File");
		final MenuItem itemHelp = new MenuItem("Help");
		itemHelp.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// create a modal dialog
				JACPOptionPane dialog = JACPDialogUtil.createOptionPane("Help", "Add some help text ");
				dialog.setDefaultButton(JACPDialogButton.NO);
				dialog.setDefaultCloseButtonOrientation(Pos.CENTER_RIGHT);
				dialog.setOnYesAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						JACPModalDialog.getInstance().hideModalMessage();
					}
				});
				JACPModalDialog.getInstance().showModalMessage(dialog);

			}
		});
		menuFile.getItems().add(itemHelp);
		menu.getMenus().addAll(menuFile);
	}

}
