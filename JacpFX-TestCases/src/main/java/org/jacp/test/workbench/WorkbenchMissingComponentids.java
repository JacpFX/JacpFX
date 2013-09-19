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
package org.jacp.test.workbench;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.Resource;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.components.menuBar.JACPMenuBar;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;
import org.jacp.javafx.rcp.context.JACPContext;
import org.jacp.javafx.rcp.controls.optionPane.JACPDialogButton;
import org.jacp.javafx.rcp.controls.optionPane.JACPDialogUtil;
import org.jacp.javafx.rcp.controls.optionPane.JACPOptionPane;
import org.jacp.javafx.rcp.workbench.FXWorkbench;
import org.jacp.test.main.ApplicationLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple JacpFX workbench. Define basic UI settings like size, menus and
 * toolbars here.
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */
@org.jacp.api.annotations.workbench.Workbench(id = "id1", name="workbench",perspectives = {"id06"})
public class WorkbenchMissingComponentids implements FXWorkbench {
    private Stage stage;
    @Resource
    JACPContext context;

    @Override
    public void handleInitialLayout(final IAction<Event, Object> action,
                                    final IWorkbenchLayout<Node> layout, final Stage stage) {
        layout.setWorkbenchXYSize(1024, 600);
        layout.setStyle(StageStyle.DECORATED);
        layout.setMenuEnabled(true);
        layout.registerToolBar(ToolbarPosition.SOUTH);
        this.stage = stage;

    }

    @Override
    public void postHandle(final FXComponentLayout layout) {
        final JACPMenuBar menu = layout.getMenu();
        final Menu menuFile = new Menu("File");
        final Menu menuTests = new Menu("Tests");
        final Menu menuStyles = new Menu("Styles");
        menuFile.getItems().add(getHelpItem());
        // add style selection
        for (int i = 0; i < ApplicationLauncher.STYLES.length; i++) {
            menuStyles.getItems().add(getStyle(i));
        }
         menuTests.getItems().addAll(getTestMenuItems());
        menu.getMenus().addAll(menuFile,menuTests, menuStyles);


        // show windowButtons
        menu.registerWindowButtons();
    }


    private MenuItem getStyle(final int count) {
        final MenuItem itemHelp = new MenuItem(count == 0 ? "Light" : "Dark");
        itemHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                final Scene scene = WorkbenchMissingComponentids.this.stage.getScene();
                // index 0 is always the default JACP style
                scene.getStylesheets().remove(1);
                scene.getStylesheets().add(ApplicationLauncher.STYLES[count]);

            }
        });
        return itemHelp;
    }

    private MenuItem getHelpItem() {
        final MenuItem itemHelp = new MenuItem("Help");
        itemHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                // create a modal dialog
                final JACPOptionPane dialog = JACPDialogUtil.createOptionPane(
                        "Help", "Add some help text ");
                dialog.setDefaultButton(JACPDialogButton.NO);
                dialog.setDefaultCloseButtonOrientation(Pos.CENTER_RIGHT);
                dialog.setOnYesAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent arg0) {
                        JACPModalDialog.getInstance().hideModalDialog();
                    }
                });
                JACPModalDialog.getInstance().showModalDialog(dialog);

            }
        });
        return itemHelp;
    }

    private List<MenuItem> getTestMenuItems() {
        List<MenuItem> result = new ArrayList<>();
        final MenuItem test1 = new MenuItem("Test1: layoutTargetSwitch");
        test1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                // create a modal dialog
                context.getActionListener("id01","show").performAction(arg0);

            }
        });
        final MenuItem test2 = new MenuItem("Test2: executionTargetSwitch");
        test2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                // create a modal dialog
                context.getActionListener("id02","show").performAction(arg0);

            }
        });


        result.add(test1);
        result.add(test2);
        return result;
    }

}
