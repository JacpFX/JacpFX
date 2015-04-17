/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [Component.java]
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
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.components.menuBar.JACPMenuBar;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.util.CSSUtil;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.GlobalMediator;

import java.util.Map;

/**
 * Contains all classes and UI elements needed to decorate the workbench
 * Created by Andy Moncsek on 18.12.14.
 */
public class DefaultWorkbenchDecorator implements WorkbenchDecorator {
    public static double TOOLBAR_HIGHT = 0.033d;


    private WorkbenchLayout<Node> workbenchLayout;
    private Stage stage;
    private StackPane rootPane;
    private StackPane menuPane;

    private StackPane nortPane;
    private StackPane southPane;
    private StackPane eastPane;
    private StackPane westPane;


    private AnchorPane absoluteRoot;
    private StackPane base;
    private Pane glassPane;
    private JACPModalDialog dimmer;

    double northHight = 0d;
    double menuHight = 0d;
    double southHight = 0d;
    double westWidth = 0d;
    double rightWidth = 0d;

    public DefaultWorkbenchDecorator(WorkbenchLayout<Node> workbenchLayout) {
        this.workbenchLayout = workbenchLayout;
    }

    public DefaultWorkbenchDecorator() {
    }

    /**
     * set basic layout manager for workspace
     *
     * @param stage javafx.stage.Stage
     */

    @Override
    public void initBasicLayout(final Stage stage) {
        // the top most pane is a Stackpane
        base = new StackPane();
        absoluteRoot = new AnchorPane();
        this.stage = stage;




        rootPane = new StackPane();
        rootPane.setCache(true);
        rootPane.setId(CSSUtil.CSSIdConstants.ID_ROOT_PANE);


        westPane = new StackPane();
        westPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);
        westPane.setId("westPane");
        manageNode(westPane, true, false);


        southPane = new StackPane();
        southPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);
        southPane.setId("southPane");
        manageNode(southPane, true, false);


        eastPane = new StackPane();
        eastPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);
        eastPane.setId("eastPane");
        manageNode(eastPane, true, false);

        nortPane = new StackPane();
        nortPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);
        nortPane.setId("nortPane");
        manageNode(nortPane, true, false);


        menuPane = new StackPane();
        manageNode(menuPane, true, false);

        absoluteRoot.getChildren().addAll(westPane, rootPane, eastPane, menuPane, nortPane, southPane);

        base.getChildren().add(absoluteRoot);

        defineScene();
        initMenu(stage);
        initToolbarLayout();
        initGlobalMouseEvents();
        initDimmer();
        initGlassPane();

        base.getChildren().addAll(glassPane, dimmer);

        updateAnchorSizes();

    }

    private void initMenu(final Stage stage) {
        if (getWorkbenchLayout().isMenuEnabled()) {
            manageNode(menuPane, false, true);
            final JACPMenuBar menu = getWorkbenchLayout().getMenu();
            menuPane.getChildren().add(menu);
            menu.setMenuDragEnabled(stage);
            menuPane.heightProperty().addListener((observableValue, number, t1) -> {
                if (!number.equals(t1)) {
                    menuHight = t1.doubleValue();
                    menu.setPrefHeight(menuHight);
                    updateAnchorSizes();
                }
            });
            menuHight = menu.getHeight();
        }
    }

    private void defineScene() {
        final int x = getWorkbenchLayout().getWorkbenchSize().getX();
        final int y = getWorkbenchLayout().getWorkbenchSize().getY();

        this.stage.setScene(new Scene(this.base, x, y));
        initCSS(this.stage.getScene());
    }

    private void updateAnchorSizes() {
        AnchorPane.setBottomAnchor(rootPane, southHight);
        AnchorPane.setRightAnchor(rootPane, rightWidth);
        AnchorPane.setLeftAnchor(rootPane, westWidth);
        AnchorPane.setTopAnchor(rootPane, northHight + menuHight);

        //westPane.setPrefWidth(rightWidth);
        AnchorPane.setBottomAnchor(westPane, southHight);
        AnchorPane.setLeftAnchor(westPane, 0.0d);
        AnchorPane.setTopAnchor(westPane, northHight + menuHight);

        // southPane.setPrefHeight(southHight);
        AnchorPane.setBottomAnchor(southPane, 0.0d);
        AnchorPane.setLeftAnchor(southPane, 0.0d);
        AnchorPane.setRightAnchor(southPane, 0.0d);

        //  eastPane.setPrefWidth(rightWidth);
        AnchorPane.setBottomAnchor(eastPane, southHight);
        AnchorPane.setTopAnchor(eastPane, northHight + menuHight);
        AnchorPane.setRightAnchor(eastPane, 0.0d);

        //   nortPane.setPrefHeight(northHight);
        AnchorPane.setRightAnchor(nortPane, 0.0d);
        AnchorPane.setLeftAnchor(nortPane, 0.0d);
        AnchorPane.setTopAnchor(nortPane, menuHight);

        AnchorPane.setRightAnchor(menuPane, 0.0d);
        AnchorPane.setLeftAnchor(menuPane, 0.0d);
        AnchorPane.setTopAnchor(menuPane, 0.0d);


    }


    private void initGlobalMouseEvents() {
        // catch global clicks
        stage.getScene().addEventFilter(
                MouseEvent.MOUSE_RELEASED,
                (event) -> GlobalMediator.getInstance().hideAllHideables(event));
    }


    private void initToolbarLayout() {
        // add toolbars in a specific order
        if (!getWorkbenchLayout().getRegisteredToolBars().isEmpty()) {
            final Map<ToolbarPosition, JACPToolBar> registeredToolbars =
                    getWorkbenchLayout().getRegisteredToolBars();
            registeredToolbars
                    .entrySet().forEach(entry -> assignCorrectToolBarLayout(entry.getKey(), entry.getValue(), Screen.getPrimary().getBounds())
            );


        }

    }

    private void initCSS(final Scene scene) {
        scene.getStylesheets().addAll(
                AFXWorkbench.class.getResource("/styles/jacp-styles.css")
                        .toExternalForm());
    }


    private void initDimmer() {
        JACPModalDialog.initDialog(absoluteRoot);
        dimmer = JACPModalDialog.getInstance();
        dimmer.setVisible(false);
    }

    private void initGlassPane() {
        // Pane for custom elements added to the glasspane
        glassPane = getWorkbenchLayout().getGlassPane();
        glassPane.autosize();
        glassPane.setVisible(false);
        glassPane.setPrefSize(0, 0);
    }

    /**
     * set toolBars to correct position
     *
     * @param position,     The toolbar position
     * @param bar,          the affected toolbar
     * @param visualBounds, the visual bounds
     */
    private void assignCorrectToolBarLayout(final ToolbarPosition position,
                                            final ToolBar bar, final Rectangle2D visualBounds) {
        switch (position) {
            case NORTH:
                manageNode(nortPane, false, true);
                bar.setPrefWidth(this.getWorkbenchLayout().getWorkbenchSize().getX());
                nortPane.getChildren().add(bar);
                nortPane.heightProperty().addListener((observableValue, number, t1) -> {
                    if (!number.equals(t1)) {
                        northHight = t1.doubleValue();
                        bar.setPrefHeight(northHight);
                        updateAnchorSizes();
                    }
                });
                northHight = this.getWorkbenchLayout().getWorkbenchSize().getY() * TOOLBAR_HIGHT;

                break;
            case SOUTH:
                manageNode(southPane, false, true);
                bar.setPrefWidth(this.getWorkbenchLayout().getWorkbenchSize().getX());
                southPane.getChildren().add(bar);

                southPane.heightProperty().addListener((observableValue, number, t1) -> {
                    if (!number.equals(t1)) {
                        southHight = t1.doubleValue();
                        bar.setPrefHeight(southHight);
                        updateAnchorSizes();
                    }
                });
                southHight = this.getWorkbenchLayout().getWorkbenchSize().getY() * TOOLBAR_HIGHT;


                break;
            case EAST:
                manageNode(eastPane, false, true);
                bar.setOrientation(Orientation.VERTICAL);

                eastPane.getChildren().add(bar);

                eastPane.widthProperty().addListener((observableValue, number, t1) -> {
                    if (!number.equals(t1)) {
                        rightWidth = eastPane.getWidth();
                        bar.setMaxWidth(rightWidth);
                        eastPane.setMaxWidth(rightWidth);
                        updateAnchorSizes();
                    }
                });


                rightWidth = this.getWorkbenchLayout().getWorkbenchSize().getX() * TOOLBAR_HIGHT;
                break;
            case WEST:
                manageNode(westPane, false, true);
                bar.setOrientation(Orientation.VERTICAL);

                westPane.getChildren().add(bar);

                westPane.widthProperty().addListener((observableValue, number, t1) -> {
                    if (!number.equals(t1)) {
                        westWidth = westPane.getWidth();
                        bar.setMaxWidth(westWidth);
                        westPane.setMaxWidth(westWidth);
                        updateAnchorSizes();
                    }
                });

                westWidth = this.getWorkbenchLayout().getWorkbenchSize().getX() * TOOLBAR_HIGHT;
                break;
        }
    }

    private void manageNode(Node node, boolean disable, boolean managed) {
        node.setDisable(disable);
        node.setManaged(managed);
    }

    /**
     * {@inheritDoc}
     */
    private FXWorkbenchLayout getWorkbenchLayout() {
        return (FXWorkbenchLayout) workbenchLayout;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkbenchLayout(WorkbenchLayout<Node> workbenchLayout) {
        this.workbenchLayout = workbenchLayout;
    }

    @Override
    public Pane getRoot() {
        return rootPane;
    }

    @Override
    public Pane getGlassPane() {
        return glassPane;
    }
}
