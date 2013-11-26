/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [JACPToolBar.java]
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
package org.jacpfx.rcp.components.toolBar;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jacpfx.rcp.common.ColorDefinitions;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.util.CSSUtil;
import org.jacpfx.rcp.util.LayoutUtil;
import org.jacpfx.rcp.workbench.SceneUtil;

import java.util.logging.Logger;

import static org.jacpfx.rcp.components.toolBar.JACPOptionButtonOrientation.BOTTOM;

/**
 * The Class JACPOptionButton.
 *
 * A simple button, with some more options. The button holds no action, except displaying the child options.
 *
 * @author Patrick Symmangk
 */
public class JACPOptionButton extends Button {

    private Pane                        glassPane;
    private Pane                        arrow;
    // used for TOP and BOTTOM
    private VBox                        verticalHoverMenu   = new VBox();
    // used for LEFT and RIGHT
    private HBox                        horizontalHoverMenu = new HBox();
    private VBox                        options             = new VBox();
    private Point2D                     translate;
    private JACPOptionButtonOrientation orientation;

    private FXComponentLayout           layout;

    // properties
    private SimpleDoubleProperty padding                = new SimpleDoubleProperty();
    private SimpleDoubleProperty buttonXLocation        = new SimpleDoubleProperty();
    private SimpleDoubleProperty buttonYLocation        = new SimpleDoubleProperty();

    // constants
    private static final double NO_PADDING              = 0.0;
    private static final double ARROW_WIDTH             = 10.0;
    private static final double ARROW_HEIGHT            = 5.0;
    private static final int INSETS                     = 5;
    private static final Dimension2D ARROW_CENTER       = new Dimension2D(ARROW_WIDTH / 2, ARROW_HEIGHT / 2);
    private static final String CSS_TOP_ARROW_CLASS     = "top-arrow";
    private static final String CSS_BTM_ARROW_CLASS     = "btm-arrow";
    private static final String CSS_LFT_ARROW_CLASS     = "lft-arrow";
    private static final String CSS_RGT_ARROW_CLASS     = "rgt-arrow";

    private Logger LOGGER = Logger.getLogger(JACPOptionButton.class.getName());

    /**
     *
     * Constructor without {@link JACPOptionButtonOrientation} option.
     * The default orientation will be BOTTOM.
     *
     * @param label - the label to show
     * @param layout - the {@link FXComponentLayout} to use
     */
    public JACPOptionButton(final String label, final FXComponentLayout layout) {
        this(label, layout, BOTTOM);
    }

    /**
     *
     * Constructor with {@link JACPOptionButtonOrientation} option.
     *
     * @param label - the label to show
     * @param layout - the {@link FXComponentLayout} to use
     * @param orientation - the {@link JACPOptionButtonOrientation} option, depending on the Orientation of the {@link JACPToolBar}. [LEFT, TOP, RIGHT, BOTTOM]
     */
    public JACPOptionButton(final String label, final FXComponentLayout layout, final JACPOptionButtonOrientation orientation) {
        super(label);
        this.glassPane      = layout.getGlassPane();
        this.orientation    = orientation;
        initLayout();
    }

    /**
     *
     * Adds a button (Option) to the menu.
     *
     * @param option - the Button to add to the menu.
     */
    public void addOption(Button option) {
        option.setMaxWidth(Integer.MAX_VALUE);
        options.getChildren().add(option);
        option.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // hide option when button is pressed
                hideOptions();
            }
        });
        VBox.setMargin(option, new Insets(INSETS));
    }

    /**
     * Adds multiple buttons to the menu.
     * @param options - all the buttons to add
     */
    public void addOptions( Button... options){
        for(final Button btn : options){
            addOption(btn);
        }
    }


    private void initArrow(){
        this.arrow = new Pane();
        switch (orientation){
            case TOP: case BOTTOM:
                arrow.setMinHeight(ARROW_HEIGHT);
                arrow.setMaxWidth(ARROW_WIDTH);
                break;
            case LEFT: case RIGHT:
                arrow.setMinHeight(ARROW_WIDTH);
                arrow.setMaxHeight(ARROW_WIDTH);
                arrow.setMinWidth(ARROW_HEIGHT);
                arrow.setMaxWidth(ARROW_HEIGHT);
                break;
        }

    }

    private void initHoverMenu(){
        verticalHoverMenu.setAlignment(Pos.CENTER);
        verticalHoverMenu.setVisible(false);
        horizontalHoverMenu.setVisible(false);
        CSSUtil.setBackgroundColor(options, ColorDefinitions.HEX_MID_GRAY);
    }

    /*********************
     *   VERTICAL MENU   *
     *********************/

    private void initVerticalLayout() {

        this.verticalHoverMenu.boundsInLocalProperty().addListener(new RealignListener<>(orientation));

        switch (orientation) {

            case TOP:
                CSSUtil.addCSSClass(CSS_BTM_ARROW_CLASS, arrow);
                this.verticalHoverMenu.getChildren().setAll(options, arrow);

                // since the menu is on top we have to check the height, too!
                this.verticalHoverMenu.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                        // get location of the optionbutton - top left corner
                        translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMinY());
                        // menu will be cut at the left
                        buttonYLocation.set(translate.getY() - (verticalHoverMenu.getHeight()));
                    }
                });

                break;

            case BOTTOM:
                CSSUtil.addCSSClass(CSS_TOP_ARROW_CLASS, arrow);
                this.verticalHoverMenu.getChildren().setAll(arrow, options);
                break;
        }

    }

    private void realignVerticalMenu(){
        // get padding to center hovermenu
        double fullPadding = (getWidth() - verticalHoverMenu.getWidth());
        padding.set(fullPadding / 2);

        switch (orientation) {
            case TOP:
                translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMinY() - this.verticalHoverMenu.getHeight());
                break;
            case BOTTOM:
                translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMaxY());
                break;
        }

        // get location of the optionbutton - buttom left corner

        // menu will be cut at the left
        if (translate.getX() + padding.get() < 0) {
            // hovermenu alignes with the optionbutton on the left hand side
            arrow.setTranslateX(padding.get());
            padding.set(NO_PADDING);
        }

        if(translate.getX() + padding.get() + this.verticalHoverMenu.getWidth() > SceneUtil.getScene().getWidth()){
            arrow.setTranslateX(getHalfWidth());
            padding.set(fullPadding);
        }



        buttonXLocation.set(translate.getX());
        buttonYLocation.set(translate.getY());
    }

    /***********************
     *   HORIZONTAL MENU   *
     ***********************/

    private void initHorizontalLayout(){
        this.horizontalHoverMenu.boundsInLocalProperty().addListener(new RealignListener<>(orientation));
        switch (orientation){
            case LEFT:
                CSSUtil.addCSSClass(CSS_RGT_ARROW_CLASS, arrow);
                this.horizontalHoverMenu.getChildren().setAll(options, arrow);
                break;
            case RIGHT:
                CSSUtil.addCSSClass(CSS_LFT_ARROW_CLASS, arrow);
                this.horizontalHoverMenu.getChildren().setAll(arrow, options);
                break;
        }
    }

    private class RealignListener<T> implements ChangeListener<T>{

        private JACPOptionButtonOrientation orientation;

        private RealignListener(final JACPOptionButtonOrientation orientation) {
            super();
            this.orientation = orientation;
        }

        @Override
        public void changed(ObservableValue<? extends T> observableValue, T t, T t2) {
             switch(orientation){
                 case TOP: case BOTTOM: realignVerticalMenu(); break;
                 case LEFT: case RIGHT: realignHorizontalMenu(); break;
             }

        }
    }



    private void realignHorizontalMenu(){

        switch (orientation) {
            case LEFT:
                translate = localToScene(getBoundsInLocal().getMinX() - this.horizontalHoverMenu.getWidth(), getBoundsInLocal().getMinY());
                break;
            case RIGHT:
                translate = localToScene(getBoundsInLocal().getMaxX(), getBoundsInLocal().getMinY());
                break;
        }

        // not enough space to the top... menu aligns with top of the button
        if (translate.getY() + padding.get() < 0) {
            // hovermenu alignes with the optionbutton on the top
            arrow.setTranslateY(padding.get());
            // clear padding
            padding.set(NO_PADDING);
        } else{
            arrow.setTranslateY(getHalfHeight() - ARROW_CENTER.getWidth());
        }

        // not enough space to the bottom --> menu aligns with bottom of the button
        if(translate.getY() + this.horizontalHoverMenu.getHeight() > SceneUtil.getScene().getHeight()){
            arrow.setTranslateY(this.horizontalHoverMenu.getHeight() - getHalfHeight());
            padding.set(-this.horizontalHoverMenu.getHeight() + getHeight());
        }

        buttonXLocation.set(translate.getX());
        buttonYLocation.set(translate.getY());
    }


    private void initLayout(){

        // check for changes
        this.localToSceneTransformProperty().addListener(new RealignListener<>(orientation));
        this.glassPane.visibleProperty().addListener(new RealignListener<>(orientation));

        initArrow();
        initHoverMenu();

        switch(orientation){
            case TOP: case BOTTOM:
                initVerticalLayout();
                initAction(verticalHoverMenu);
                break;
            case LEFT: case RIGHT:
                initHorizontalLayout();
                initAction(horizontalHoverMenu);
                break;
        }
    }

    private void initAction(final Node node) {
        this.setOnAction((EventHandler<ActionEvent>)(actionEvent)-> {

            node.setVisible(!node.isVisible());
            // if another option is shown, hide everything before switching to the current content
            if(!glassPane.getChildren().contains(node)){
                LayoutUtil.hideAllChildren(glassPane);
            }
            // adjust hovermenu
            glassPane.getChildren().setAll(node);
            glassPane.setMaxWidth(options.getWidth());
            glassPane.setMaxHeight(options.getHeight());
            StackPane.setAlignment(glassPane, Pos.TOP_LEFT);

            switch (orientation) {
                case LEFT: case RIGHT:
                    glassPane.translateXProperty().bind(buttonXLocation);
                    glassPane.translateYProperty().bind(buttonYLocation.add(padding));
                    break;
                case TOP: case BOTTOM:
                    glassPane.translateXProperty().bind(buttonXLocation.add(padding));
                    glassPane.translateYProperty().bind(buttonYLocation);
                    break;
            }

            // show everything
            glassPane.setVisible(node.isVisible());

        });
    }



    /**
     * Hides the menu.
     */
    public void hideOptions(){
        this.glassPane.setVisible(false);
        this.verticalHoverMenu.setVisible(false);
        this.horizontalHoverMenu.setVisible(false);
    }

    private double getHalfWidth(){
        return getWidth()/2;
    }

    private double getHalfHeight(){
        return getHeight()/2;
    }


}
