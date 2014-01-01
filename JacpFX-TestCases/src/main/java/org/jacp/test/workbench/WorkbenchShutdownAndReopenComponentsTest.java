/************************************************************************
 *
 * Copyright (C) 2010 - 2014
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
import org.jacpfx.api.message.Message;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.componentLayout.IWorkbenchLayout;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.components.menuBar.JACPMenuBar;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.context.JACPContext;
import org.jacpfx.controls.optionPane.JACPDialogButton;
import org.jacpfx.controls.optionPane.JACPDialogUtil;
import org.jacpfx.controls.optionPane.JACPOptionPane;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.jacp.test.main.ApplicationLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple JacpFX workbench. Define basic UI settings like size, menus and
 * toolbars here.
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */
@org.jacpfx.api.annotations.workbench.Workbench(id = "id1", name = "workbench", perspectives = {"id19"})
public class WorkbenchShutdownAndReopenComponentsTest implements FXWorkbench {
    private Stage stage;
    @Resource
    static JACPContext context;

    @Override
    public void handleInitialLayout(final Message<Event, Object> action,
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
        final Menu menuClose = new Menu("Close Perspectives");
        final Menu menuStyles = new Menu("Styles");
        menuFile.getItems().add(getHelpItem());
        // add style selection
        for (int i = 0; i < ApplicationLauncher.STYLES.length; i++) {
            menuStyles.getItems().add(getStyle(i));
        }
        menuTests.getItems().addAll(getTestMenuItems());
        menuClose.getItems().addAll(getCloseMenuItems());
        menu.getMenus().addAll(menuFile, menuTests, menuClose,menuStyles);


        // show windowButtons
        menu.registerWindowButtons();
    }


    private MenuItem getStyle(final int count) {
        final MenuItem itemHelp = new MenuItem(count == 0 ? "Light" : "Dark");
        itemHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                final Scene scene = WorkbenchShutdownAndReopenComponentsTest.this.stage.getScene();
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
        final MenuItem test1 = new MenuItem("Test1: P1");
        test1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                // create a modal dialog
                context.send("id19", "show");

            }
        });

        result.add(test1);
        return result;
    }

    private List<MenuItem> getCloseMenuItems() {
        List<MenuItem> result = new ArrayList<>();
        final MenuItem test1 = new MenuItem("Test1: P1");
        test1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                // create a modal dialog
                context.send("id19", "stop");

            }
        });


        result.add(test1);
        return result;
    }

    public static void startPerspective() {
        context.send("id19", FXUtil.MessageUtil.INIT);
    }


}
