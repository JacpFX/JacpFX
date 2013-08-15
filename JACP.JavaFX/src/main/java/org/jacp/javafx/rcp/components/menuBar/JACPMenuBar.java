/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [JACPMenuBar.java]
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
package org.jacp.javafx.rcp.components.menuBar;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jacp.api.util.OS;
import org.jacp.javafx.rcp.util.CSSUtil;
import org.jacp.javafx.rcp.util.ShutdownThreadsHandler;
import org.jacp.javafx.rcp.util.TearDownHandler;

/**
 * The Class JACPMenuBar.
 * 
 * @author Patrick Symmangk
 * 
 */
public class JACPMenuBar extends HBox {

    /** The left bar. */
    private ToolBar leftBar;

    /** The right bar. */
    private ToolBar rightBar;

    /** The main bar. */
    private MenuBar mainBar;

    /** The last x. */
    private double lastW;
    private double lastX;

    /** The last y. */
    private double lastH;
    private double lastY;

    private Stage stage;

    private Button minimize;
    private Button maximize;
    private Button close;

    private boolean maximized = false;

    private boolean unregistered = false;

    /**
     * Instantiates a new jACP menu bar.
     */
    public JACPMenuBar() {
        this.initMenuBar();
    }

    /**
     * Inits the menu bar.
     */
    private void initMenuBar() {
        this.leftBar = new ToolBar();
        this.rightBar = new ToolBar();
        this.mainBar = new MenuBar();

        if (OS.MAC.equals(OS.getOS())) {
            mainBar.setUseSystemMenuBar(true);
            this.getChildren().addAll(mainBar);
        } else {
            this.leftBar.setMinWidth(0);
            this.rightBar.setMinWidth(0);
            this.mainBar.setPrefHeight(22);
            this.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(this.mainBar, Priority.ALWAYS);
            HBox.setHgrow(this.leftBar, Priority.NEVER);
            HBox.setHgrow(this.rightBar, Priority.NEVER);

            this.getStyleClass().addAll(this.mainBar.getStyleClass());
            this.clearBackground(this.leftBar, this.mainBar, this.rightBar);

            this.bind(this.rightBar);
            this.getChildren().addAll(this.leftBar, this.mainBar, this.rightBar);
        }
    }

    public void setTitle(final String title) {
    }

    /**
     * Clear background.
     * 
     * @param node
     *            the node
     */
    private void clearBackground(final Node... node) {
        if (node != null) {
            for (final Node n : node) {
                n.setStyle("-fx-background-color: transparent;");
            }
        }
    }

    /**
     * Bind.
     * 
     * @param bar
     *            the bar
     */
    private void bind(final ToolBar bar) {
        bar.maxHeightProperty().bind(this.mainBar.heightProperty());
        bar.prefHeightProperty().bind(this.mainBar.heightProperty());
    }

    /**
     * Gets the menus.
     * 
     * @return the menus
     */
    public ObservableList<Menu> getMenus() {
        return this.mainBar.getMenus();
    }

    /**
     * Adds the node.
     * 
     * @param orientation
     *            the orientation
     * @param node
     *            the node
     */
    public void addNode(final JACPMenuBarButtonOrientation orientation, final Node... node) {
        if (JACPMenuBarButtonOrientation.LEFT.equals(orientation)) {
            this.leftBar.getItems().addAll(node);
        } else {
            this.rightBar.getItems().addAll(node);
        }
    }

    public void removeNode(final JACPMenuBarButtonOrientation orientation, final Node... node) {
        if (JACPMenuBarButtonOrientation.LEFT.equals(orientation)) {
            this.leftBar.getItems().removeAll(node);
        } else {
            this.rightBar.getItems().removeAll(node);
        }
    }

    /** The mouse drag offset x. */
    private double mouseDragOffsetX = 0;

    /** The mouse drag offset y. */
    private double mouseDragOffsetY = 0;

    /**
     * Sets the menu drag enabled.
     * 
     * @param stage
     *            the new menu drag enabled
     */
    public void setMenuDragEnabled(final Stage stage) {
        this.stage = stage;
        this.mainBar.setOnMousePressed(event -> {
            JACPMenuBar.this.mouseDragOffsetX = event.getSceneX();
            JACPMenuBar.this.mouseDragOffsetY = event.getSceneY();
        });

        this.mainBar.setOnMouseDragged(event -> {
            // if (!windowButtons.isMaximized()) {
            stage.setX(event.getScreenX() - JACPMenuBar.this.mouseDragOffsetX);
            stage.setY(event.getScreenY() - JACPMenuBar.this.mouseDragOffsetY);
            // }
        });
    }

    public void minimize() {
        this.stage.setIconified(true);
    }

    /**
     * Maximize.
     */
    public void maximize() {
        if (this.stage != null) {
            if (!this.maximized) {
                final Screen screen = Screen.getPrimary();
                this.lastW = this.stage.getWidth();
                this.lastH = this.stage.getHeight();
                this.lastX = this.stage.getX();
                this.lastY = this.stage.getY();

                this.stage.setWidth(screen.getBounds().getWidth());
                this.stage.setHeight(screen.getBounds().getHeight());
                this.stage.setX(0);
                this.stage.setY(0);

            } else {
                this.stage.setWidth(this.lastW);
                this.stage.setHeight(this.lastH);
                this.stage.setX(this.lastX);
                this.stage.setY(this.lastY);
            }
            this.maximized = !this.maximized;
        }
    }

    public void registerWindowButtons() {
        this.initWindowButtons();
    }

    private void initWindowButtons() {
        if (!unregistered) {
            minimize = new Button("_");
            maximize = new Button("-");
            close = new Button("x");
            minimize.getStyleClass().add(CSSUtil.CSSConstants.CLASS_WINDOW_BUTTONS);
            maximize.getStyleClass().add(CSSUtil.CSSConstants.CLASS_WINDOW_BUTTONS);
            close.getStyleClass().add(CSSUtil.CSSConstants.CLASS_WINDOW_BUTTONS);
            minimize.setId(CSSUtil.CSSConstants.ID_WINDOW_MIN);
            minimize.setOnAction(arg0 -> minimize());

            maximize.setId(CSSUtil.CSSConstants.ID_WINDOW_MAX);
            maximize.setOnAction(arg0 -> maximize());
            close.setId(CSSUtil.CSSConstants.ID_WINDOW_CLOSE);
            close.setOnAction(arg0 -> {
                ShutdownThreadsHandler.shutdowAll();
                TearDownHandler.handleGlobalTearDown();
                Platform.exit();
            });

            this.addNode(JACPMenuBarButtonOrientation.RIGHT, minimize, maximize, close);
        } else {
            handleDeregistration();
        }
    }

    private void handleDeregistration() {
        this.removeNode(JACPMenuBarButtonOrientation.RIGHT, minimize, maximize, close);
    }

    public void deregisterWindowButtons() {
        this.unregistered = true;
    }

}
