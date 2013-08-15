/*
 * Copyright (C) 2010,2011.
 * AHCP Project (http://code.google.com/p/jacp)
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
 */
package org.jacp.demo.workbench;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jacp.api.action.IAction;
import org.jacp.api.componentLayout.IWorkbenchLayout;
import org.jacp.api.util.ToolbarPosition;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.components.menuBar.JACPMenuBar;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;
import org.jacp.javafx.rcp.workbench.AFXWorkbench;

/**
 * Workbench for contact demo with JacpFX (JavaFX2 and Spring). The workbench is
 * the root node of your JacpFX application.
 * 
 * @author Andy Moncsek, Patrick Symmangk
 * 
 */
public class ContactWorkbench extends AFXWorkbench {

    private final String projectURL = "http://code.google.com/p/jacp/wiki/Documentation";
    private final String message = "JacpFX is a Framework to create Rich Clients in MVC style with JavaFX 2, Spring and an Actor like component approach. It provides a simple API to create a workspace, perspectives, components and to compose your Client application easily. More Info see: ";

    @Override
    public void handleInitialLayout(final IAction<Event, Object> action, final IWorkbenchLayout<Node> layout, final Stage stage) {
        layout.setWorkbenchXYSize(1024, 768);
        layout.registerToolBar(ToolbarPosition.NORTH);
        layout.setStyle(StageStyle.UNDECORATED);
        layout.setMenuEnabled(true);
    }

    @Override
    public void postHandle(final FXComponentLayout layout) {
        final JACPMenuBar menu = layout.getMenu();
        final Menu menuFile = new Menu("File");
        menuFile.getItems().addAll(this.createExitEntry(), this.createInfoEntry());
        menu.getMenus().addAll(menuFile);
        menu.registerWindowButtons();
    }

    private MenuItem createInfoEntry() {
        final MenuItem info = new MenuItem("Info");
        info.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                final VBox box = new VBox();
                box.setId("HelpDialog");
                box.setMaxSize(500, Region.USE_PREF_SIZE);

                final Button ok = new Button("OK");
                HBox.setMargin(ok, new Insets(6, 5, 4, 2));
                final HBox hboxButtons = new HBox();
                hboxButtons.setAlignment(Pos.CENTER_RIGHT);
                ok.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent actionEvent) {
                        JACPModalDialog.getInstance().hideModalDialog();
                    }
                });

                hboxButtons.getChildren().addAll(ok);
                box.getChildren().addAll(ContactWorkbench.this.createTitle(), ContactWorkbench.this.createInfoText(), ContactWorkbench.this.createProjectLink(), hboxButtons);
                JACPModalDialog.getInstance().showModalDialog(box);

            }
        });
        return info;
    }

    private MenuItem createExitEntry() {
        final MenuItem itemExit = new MenuItem("Exit");
        itemExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
                System.exit(0);

            }
        });
        return itemExit;
    }

    private Label createTitle() {
        final Label title = new Label("JacpFX project demo");
        title.setId("jacp-custom-title");
        VBox.setMargin(title, new Insets(2, 2, 10, 2));
        return title;
    }

    private Text createInfoText() {
        return TextBuilder.create().layoutY(100).textOrigin(VPos.TOP).textAlignment(TextAlignment.JUSTIFY).wrappingWidth(400).text(this.message).fill(Color.WHITE).build();
    }

    private Hyperlink createProjectLink() {
        final Hyperlink link = new Hyperlink();
        link.setText(this.projectURL);
        link.setStyle("-fx-text-fill: white;");
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                final WebView wv = new WebView();
                wv.getEngine().load(ContactWorkbench.this.projectURL);
                final Scene scene = new Scene(wv, 1024, 768);
                final Stage stage = new Stage();
                stage.setTitle("JacpFX documentation");
                stage.setScene(scene);
                stage.show();

            }
        });
        return link;
    }

}
