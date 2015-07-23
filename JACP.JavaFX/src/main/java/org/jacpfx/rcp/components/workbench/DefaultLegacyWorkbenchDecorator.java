/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [DefaultLegacyWorkbenchDecorator.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx.rcp.components.workbench;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.util.OS;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.util.CSSUtil;
import org.jacpfx.rcp.util.LayoutUtil;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.GlobalMediator;

import java.util.Map;

/**
 * Contains all classes and UI elements needed to decorate the workbench
 * Created by Andy Moncsek on 18.12.14.
 */
public class DefaultLegacyWorkbenchDecorator implements WorkbenchDecorator {
    private WorkbenchLayout<Node> workbenchLayout ;
    private Stage stage;
    private GridPane root;
    private BorderPane baseLayoutPane;
    private StackPane absoluteRoot;
    private GridPane base;
    private Pane glassPane;
    private JACPModalDialog dimmer;



    public DefaultLegacyWorkbenchDecorator(WorkbenchLayout<Node> workbenchLayout) {
        this.workbenchLayout = workbenchLayout;
    }

    public DefaultLegacyWorkbenchDecorator() {
    }

    /**
     * set basic layout manager for workspace
     *
     * @param stage javafx.stage.Stage
     */

    @Override
    public void initBasicLayout(final Stage stage) {
        initStageStyle(stage);
        // the top most pane is a Stackpane
        this.base = new GridPane();
        this.absoluteRoot = new StackPane();
        this.baseLayoutPane = new BorderPane();
        this.stage = stage;


        this.initBaseLayout();
        this.initMenuLayout();
        this.initToolbarLayout();
        this.completeLayout();
    }

    protected void initStageStyle(final Stage stage) {
        if (OS.MAC.equals(OS.getOS())) {
            // OSX will always be DECORATED due to fullscreen option!
            stage.initStyle(StageStyle.DECORATED);
        } else {
            stage.initStyle(getWorkbenchLayout().getStyle());
        }
    }

    private void completeLayout() {
        // fetch current workbenchsize
        final int x = this.getWorkbenchLayout().getWorkbenchSize().getX();
        final int y = this.getWorkbenchLayout().getWorkbenchSize().getY();

        this.absoluteRoot.getChildren().add(this.baseLayoutPane);
        this.absoluteRoot.setId(CSSUtil.CSSIdConstants.ID_ROOT);
        this.base.getChildren().add(absoluteRoot);
        LayoutUtil.GridPaneUtil.setFullGrow(Priority.ALWAYS, this.absoluteRoot);
        this.stage.setScene(new Scene(this.base, x, y));
        this.initCSS(this.stage.getScene());

        this.absoluteRoot.getChildren().add(this.glassPane);
        this.absoluteRoot.getChildren().add(this.dimmer);

        this.initGlobalMouseEvents();

    }

    private void initGlobalMouseEvents() {
        // catch global clicks
        this.stage.getScene().addEventFilter(
                MouseEvent.MOUSE_RELEASED,
                (event) -> GlobalMediator.getInstance().hideAllHideables(event));
    }

    private void initMenuLayout() {
        // add the menu if needed
        if (this.getWorkbenchLayout().isMenuEnabled()) {
            this.baseLayoutPane.setTop(this.getWorkbenchLayout().getMenu());
            this.getWorkbenchLayout().getMenu().setMenuDragEnabled(stage);
        }

    }

    private void initToolbarLayout() {
        // add toolbars in a specific order
        if (!this.getWorkbenchLayout().getRegisteredToolBars().isEmpty()) {

            // add another Layer to hold all the toolbars
            final BorderPane toolbarPane = new BorderPane();
            this.baseLayoutPane.setCenter(toolbarPane);

            final Map<ToolbarPosition, JACPToolBar> registeredToolbars = this
                    .getWorkbenchLayout().getRegisteredToolBars();
            registeredToolbars
                    .entrySet().forEach(entry->this.assignCorrectToolBarLayout(entry.getKey(),  entry.getValue(), toolbarPane)
            );

            // add root to the center
            toolbarPane.setCenter(this.root);
            toolbarPane.getStyleClass().add(
                    CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);

        } else {
            // no toolbars -> no special Layout needed
            this.baseLayoutPane.setCenter(this.root);
        }

    }

    private void initCSS(final Scene scene) {
        scene.getStylesheets().addAll(
                AFXWorkbench.class.getResource("/styles/jacp-styles.css")
                        .toExternalForm());
    }

    private void initBaseLayout() {
        this.initRootPane();
        this.initDimmer();
        this.initGlassPane();
    }

    private void initRootPane() {
        // root is top most pane
        this.root = new GridPane();
        this.root.setCache(true);
        this.root.setId(CSSUtil.CSSIdConstants.ID_ROOT_PANE);

    }

    private void initDimmer() {
        JACPModalDialog.initDialog(this.baseLayoutPane);
        this.dimmer = JACPModalDialog.getInstance();
        this.dimmer.setVisible(false);
    }

    private void initGlassPane() {
        // Pane for custom elements added to the glasspane
        this.glassPane = this.getWorkbenchLayout().getGlassPane();
        this.glassPane.autosize();
        this.glassPane.setVisible(false);
        this.glassPane.setPrefSize(0, 0);
    }

    /**
     * set toolBars to correct position
     *
     * @param position, The toolbar position
     * @param bar,      the affected toolbar
     * @param pane,     the root pane
     */
    private void assignCorrectToolBarLayout(final ToolbarPosition position,
                                            final ToolBar bar, final BorderPane pane) {
        switch (position) {
            case NORTH:
                pane.setTop(bar);
                break;
            case SOUTH:
                pane.setBottom(bar);
                break;
            case EAST:
                bar.setOrientation(Orientation.VERTICAL);
                pane.setRight(bar);
                break;
            case WEST:
                bar.setOrientation(Orientation.VERTICAL);
                pane.setLeft(bar);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    private FXWorkbenchLayout getWorkbenchLayout() {
        return (FXWorkbenchLayout) this.workbenchLayout;
    }

    @Override
    public Pane getRoot() {
        return this.root;
    }

    @Override
    public Pane getGlassPane() {
        return this.glassPane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkbenchLayout(WorkbenchLayout<Node> workbenchLayout) {
        this.workbenchLayout = workbenchLayout;
    }
}
