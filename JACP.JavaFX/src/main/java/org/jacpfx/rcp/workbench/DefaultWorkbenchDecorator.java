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

package org.jacpfx.rcp.workbench;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.stage.StageStyle;
import org.jacpfx.api.componentLayout.WorkbenchLayout;
import org.jacpfx.api.util.OS;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXWorkbenchLayout;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.util.CSSUtil;

import java.util.Map;

/**
 * Contains all classes and UI elements needed to decorate the workbench
 * Created by Andy Moncsek on 18.12.14.
 */
public class DefaultWorkbenchDecorator implements WorkbenchDecorator {
    public static double TOOLBAR_HIGHT = 0.033d;
    private final WorkbenchLayout<Node> workbenchLayout ;
    private Stage stage;
    private StackPane rootPane;
    private StackPane topPane;
    private StackPane menuPane;

    private StackPane leftPane;


    private StackPane bottomPane;
    private StackPane rightPane;


    private AnchorPane absoluteRoot;
    private StackPane base;
    private Pane glassPane;
    private JACPModalDialog dimmer;

    double topHight = 0d;
    double menuHight = 0d;
    double bottomHight = 0d;
    double leftHight = 0d;
    double rightHight =0d;

    public DefaultWorkbenchDecorator(WorkbenchLayout<Node> workbenchLayout) {
        this.workbenchLayout = workbenchLayout;
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

        if (OS.MAC.equals(OS.getOS())) {
            // OSX will always be DECORATED due to fullscreen option!
            stage.initStyle(StageStyle.DECORATED);
        } else {
            stage.initStyle(this.getWorkbenchLayout().getStyle());
        }

        //absoluteRoot.setMaxWidth(Double.MAX_VALUE);
       // absoluteRoot.setMaxHeight(Double.MAX_VALUE);
       // absoluteRoot.setMinHeight(0);
       // absoluteRoot.setMinWidth(0);

        rootPane = new StackPane();
        rootPane.setCache(true);
        rootPane.setId(CSSUtil.CSSIdConstants.ID_ROOT_PANE);


        rightPane = new StackPane();
        rightPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);



        //////////// bottom //////////////
        bottomPane = new StackPane();
        bottomPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);


        leftPane = new StackPane();
        leftPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);


        topPane = new StackPane();
        topPane.getStyleClass().add(
                CSSUtil.CSSClassConstants.CLASS_DARK_BORDER);


        menuPane = new StackPane();

        absoluteRoot.getChildren().addAll(rightPane, rootPane, leftPane,menuPane,topPane,bottomPane) ;

        base.getChildren().add(absoluteRoot);

        // fetch current workbenchsize
        final int x = getWorkbenchLayout().getWorkbenchSize().getX();
        final int y = getWorkbenchLayout().getWorkbenchSize().getY();

        this.stage.setScene(new Scene(this.base, x, y));
        initCSS(this.stage.getScene());
        SceneUtil.setScene(this.stage.getScene());

        // add the menu if needed
        if (getWorkbenchLayout().isMenuEnabled()) {
            menuPane.getChildren().add(getWorkbenchLayout().getMenu());
            getWorkbenchLayout().getMenu().setMenuDragEnabled(stage);
            menuHight = getWorkbenchLayout().getMenu().getHeight();
        }
        initToolbarLayout();
        initGlobalMouseEvents();
        initDimmer();
        initGlassPane();

        base.getChildren().addAll(glassPane,dimmer);

        updateAnchorSizes();

    }

    private void updateAnchorSizes() {
        AnchorPane.setBottomAnchor(rootPane, bottomHight);
        AnchorPane.setRightAnchor(rootPane, rightHight);
        AnchorPane.setLeftAnchor(rootPane,leftHight);
        AnchorPane.setTopAnchor(rootPane, topHight+menuHight);


       // rightPane.setPrefWidth(rightHight);
        AnchorPane.setBottomAnchor(rightPane, bottomHight);
        AnchorPane.setRightAnchor(rightPane, 0.0d);
        AnchorPane.setTopAnchor(rightPane, topHight+menuHight);

        bottomPane.setLayoutX(0.0d);
        bottomPane.setPrefHeight(bottomHight);
        AnchorPane.setBottomAnchor(bottomPane, 0.0d);
        AnchorPane.setLeftAnchor(bottomPane,0.0d);
        AnchorPane.setRightAnchor(bottomPane,0.0d);

      //  leftPane.setPrefWidth(leftHight);
        AnchorPane.setBottomAnchor(leftPane, bottomHight);
        AnchorPane.setTopAnchor(leftPane, topHight+menuHight);

      //  topPane.setPrefHeight(topHight);
        AnchorPane.setRightAnchor(topPane, 0.0d);
        AnchorPane.setLeftAnchor(topPane, 0.0d);
        AnchorPane.setTopAnchor(topPane,  menuHight);

        AnchorPane.setRightAnchor(menuPane, 0.0d);
        AnchorPane.setLeftAnchor(menuPane, 0.0d);
        AnchorPane.setTopAnchor(menuPane,  0.0d);

    }



    private void initGlobalMouseEvents() {
        // catch global clicks
        this.stage.getScene().addEventFilter(
                MouseEvent.MOUSE_RELEASED,
                (event) -> GlobalMediator.getInstance().hideAllHideables(event));
    }



    private void initToolbarLayout() {
        // add toolbars in a specific order
        if (!this.getWorkbenchLayout().getRegisteredToolBars().isEmpty()) {


            final Map<ToolbarPosition, JACPToolBar> registeredToolbars =
                    getWorkbenchLayout().getRegisteredToolBars();
            registeredToolbars
                    .entrySet().forEach(entry->assignCorrectToolBarLayout(entry.getKey(), entry.getValue(), Screen.getPrimary().getVisualBounds())
            );


        }

    }

    private void initCSS(final Scene scene) {
        scene.getStylesheets().addAll(
                AFXWorkbench.class.getResource("/styles/jacp-styles.css")
                        .toExternalForm());
    }





    private void initDimmer() {
        JACPModalDialog.initDialog(this.absoluteRoot);
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
     * @param position, The toolbar position
     * @param bar,      the affected toolbar
     * @param visualBounds,     the visual bounds
     */
    private void assignCorrectToolBarLayout(final ToolbarPosition position,
                                            final ToolBar bar,final Rectangle2D visualBounds) {
        switch (position) {
            case NORTH:
                bar.setPrefWidth(this.getWorkbenchLayout().getWorkbenchSize().getX());
                topPane.getChildren().add(bar);
                topHight =   this.getWorkbenchLayout().getWorkbenchSize().getY()*TOOLBAR_HIGHT;
                bar.setPrefHeight(topHight);
                break;
            case SOUTH:
                bar.setPrefWidth(this.getWorkbenchLayout().getWorkbenchSize().getX());
                bottomPane.getChildren().add(bar);
                bottomHight =  this.getWorkbenchLayout().getWorkbenchSize().getY()*TOOLBAR_HIGHT;
                bar.setPrefHeight(bottomHight);
                break;
            case EAST:
                bar.setPrefWidth(this.getWorkbenchLayout().getWorkbenchSize().getY());
                bar.setOrientation(Orientation.VERTICAL);
                leftPane.getChildren().add(bar);

                leftHight = this.getWorkbenchLayout().getWorkbenchSize().getX()*TOOLBAR_HIGHT;
                break;
            case WEST:
                //bar.setPrefWidth(this.getWorkbenchLayout().getWorkbenchSize().getY());
                bar.setOrientation(Orientation.VERTICAL);
                //System.out.println("bar: "+bar.getWidth()+"  "+bar.getPrefWidth());
                rightPane.getChildren().add(bar);
                rightPane.widthProperty().addListener(new InvalidationListener() {

                    @Override
                    public void invalidated(Observable observable) {
                        rightHight = rightPane.getWidth();
                        updateAnchorSizes();
                        System.out.println("CHANGE SIZE: "+ rightPane.getWidth());
                    }
                });

                rightHight =  this.getWorkbenchLayout().getWorkbenchSize().getX()*TOOLBAR_HIGHT;
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
        return this.rootPane;
    }

    @Override
    public Pane getGlassPane() {
        return this.glassPane;
    }
}
