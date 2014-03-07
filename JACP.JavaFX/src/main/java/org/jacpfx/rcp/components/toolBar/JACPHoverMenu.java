/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [JACPToolBar.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
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
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jacpfx.api.component.ui.Hideable;
import org.jacpfx.api.component.ui.HideableComponent;
import org.jacpfx.rcp.common.ColorDefinitions;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.util.CSSUtil;
import org.jacpfx.rcp.util.LayoutUtil;
import org.jacpfx.rcp.workbench.GlobalMediator;
import org.jacpfx.rcp.workbench.SceneUtil;

import static org.jacpfx.rcp.components.toolBar.JACPOptionButtonOrientation.BOTTOM;
import static org.jacpfx.rcp.util.CSSUtil.CSSConstants.*;

/**
 * The Class JACPOptionButton.
 *
 * A simple button, with some more parent. The button holds no message, except displaying the child parent.
 *
 * @author Patrick Symmangk
 */
public class JACPHoverMenu extends Button implements Hideable {

    private Pane glassPane;
    private Pane arrow;
    // used for TOP and BOTTOM
    private VBox verticalHoverMenu = new HideableVBox(this);
    // used for LEFT and RIGHT
    private HBox horizontalHoverMenu = new HideableHBox(this);
    private final Pane parent = new Pane();
    private Point2D translate;
    private JACPOptionButtonOrientation orientation;

    // properties
    private SimpleDoubleProperty padding = new SimpleDoubleProperty();
    private SimpleDoubleProperty buttonXLocation = new SimpleDoubleProperty();
    private SimpleDoubleProperty buttonYLocation = new SimpleDoubleProperty();

    // constants
    private static final double NO_PADDING = 0.0;
    private static final double ARROW_WIDTH = 10.0;
    private static final double ARROW_HEIGHT = 5.0;
    private static final Dimension2D ARROW_CENTER = new Dimension2D(ARROW_WIDTH / 2, ARROW_HEIGHT / 2);


    /**
     * Constructor without {@link org.jacpfx.rcp.components.toolBar.JACPOptionButtonOrientation} option.
     * The default orientation will be BOTTOM.
     *
     * @param label  - the label to show
     * @param layout - the {@link org.jacpfx.rcp.componentLayout.FXComponentLayout} to use
     */
    public JACPHoverMenu(final String label, final FXComponentLayout layout) {
        this(label, layout, BOTTOM);
    }

    /**
     * Constructor with {@link org.jacpfx.rcp.components.toolBar.JACPOptionButtonOrientation} option.
     *
     * @param label       - the label to show
     * @param layout      - the {@link org.jacpfx.rcp.componentLayout.FXComponentLayout} to use
     * @param orientation - the {@link org.jacpfx.rcp.components.toolBar.JACPOptionButtonOrientation} option, depending on the Orientation of the {@link org.jacpfx.rcp.components.toolBar.JACPToolBar}. [LEFT, TOP, RIGHT, BOTTOM]
     */
    public JACPHoverMenu(final String label, final FXComponentLayout layout, final JACPOptionButtonOrientation orientation) {
        super(label);
        this.glassPane = layout.getGlassPane();
        this.orientation = orientation;
        this.initComponent();
        GlobalMediator.getInstance().registerHideAble(this);
    }

    private void initComponent() {
        this.initParent();
        this.initLayout();
        this.initStyles();
    }

    private void initStyles() {
        CSSUtil.addCSSClass(CLASS_HOVER_MENU_BUTTON, this);
        CSSUtil.addCSSClass(CLASS_HOVER_MENU_PANE, this.parent);
    }

    public Pane getContentPane() {
        return this.parent;
    }


    private void initParent() {
        CSSUtil.addCSSClass(CLASS_JACP_OPTION_PANE_PARENT, this.parent);
        this.parent.setMaxHeight(Integer.MAX_VALUE);
        this.parent.setMaxWidth(Integer.MAX_VALUE);
    }


    private void initArrow() {
        this.arrow = new Pane();
        switch (this.orientation) {
            case TOP:
            case BOTTOM:
                this.arrow.setMinHeight(ARROW_HEIGHT);
                this.arrow.setMaxWidth(ARROW_WIDTH);
                break;
            case LEFT:
            case RIGHT:
                this.arrow.setMinHeight(ARROW_WIDTH);
                this.arrow.setMaxHeight(ARROW_WIDTH);
                this.arrow.setMinWidth(ARROW_HEIGHT);
                this.arrow.setMaxWidth(ARROW_HEIGHT);
                break;
        }

    }

    private void initHoverMenu() {
        this.verticalHoverMenu.setAlignment(Pos.CENTER);
        this.verticalHoverMenu.setVisible(false);
        this.horizontalHoverMenu.setVisible(false);
        CSSUtil.setBackgroundColor(parent, ColorDefinitions.HEX_MID_GRAY);
    }

    /**
     * ******************
     * VERTICAL MENU   *
     * *******************
     */

    private void initVerticalLayout() {

        this.verticalHoverMenu.boundsInLocalProperty().addListener(new RealignListener<>(this.orientation));

        switch (this.orientation) {

            case TOP:
                CSSUtil.addCSSClass(CSS_BTM_ARROW_CLASS, arrow);
                this.verticalHoverMenu.getChildren().setAll(this.parent, this.arrow);

                // since the menu is on top we have to check the height, too!
                this.verticalHoverMenu.heightProperty().addListener((observableValue, number, number2) -> {
                    // get location of the optionbutton - top left corner
                    this.translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMinY());
                    // menu will be cut at ththis.e left
                    this.buttonYLocation.set(this.translate.getY() - (this.verticalHoverMenu.getHeight()));
                });

                break;

            case BOTTOM:
                CSSUtil.addCSSClass(CSS_TOP_ARROW_CLASS, this.arrow);
                this.verticalHoverMenu.getChildren().setAll(this.arrow, this.parent);
                break;
        }

    }

    private void realignVerticalMenu() {
        // get padding to center hovermenu
        double fullPadding = (getWidth() - this.verticalHoverMenu.getWidth());
        this.padding.set(fullPadding / 2);

        switch (orientation) {
            case TOP:
                this.translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMinY() - this.verticalHoverMenu.getHeight());
                break;
            case BOTTOM:
                this.translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMaxY());
                break;
        }

        // get location of the optionbutton - buttom left corner

        // menu will be cut at the left
        if (this.translate.getX() + this.padding.get() < 0) {
            // hovermenu alignes with the optionbutton on the left hand side
            this.arrow.setTranslateX(this.padding.get());
            this.padding.set(NO_PADDING);
        }

        if (this.translate.getX() + this.padding.get() + this.verticalHoverMenu.getWidth() > SceneUtil.getScene().getWidth()) {
            this.arrow.setTranslateX(getHalfWidth());
            this.padding.set(fullPadding);
        }


        this.buttonXLocation.set(this.translate.getX());
        this.buttonYLocation.set(this.translate.getY());
    }

    /**
     * ********************
     * HORIZONTAL MENU   *
     * *********************
     */

    private void initHorizontalLayout() {
        this.horizontalHoverMenu.boundsInLocalProperty().addListener(new RealignListener<>(orientation));
        switch (this.orientation) {
            case LEFT:
                CSSUtil.addCSSClass(CSS_RGT_ARROW_CLASS, arrow);
                this.horizontalHoverMenu.getChildren().setAll(parent, arrow);
                break;
            case RIGHT:
                CSSUtil.addCSSClass(CSS_LFT_ARROW_CLASS, arrow);
                this.horizontalHoverMenu.getChildren().setAll(arrow, parent);
                break;
        }
    }


    private class RealignListener<T> implements ChangeListener<T> {

        private JACPOptionButtonOrientation orientation;

        private RealignListener(final JACPOptionButtonOrientation orientation) {
            super();
            this.orientation = orientation;
        }

        @Override
        public void changed(ObservableValue<? extends T> observableValue, T t, T t2) {
            switch (this.orientation) {
                case TOP:
                case BOTTOM:
                    realignVerticalMenu();
                    break;
                case LEFT:
                case RIGHT:
                    realignHorizontalMenu();
                    break;
            }
        }
    }


    private void realignHorizontalMenu() {

        switch (this.orientation) {
            case LEFT:
                this.translate = localToScene(getBoundsInLocal().getMinX() - this.horizontalHoverMenu.getWidth(), getBoundsInLocal().getMinY());
                break;
            case RIGHT:
                this.translate = localToScene(getBoundsInLocal().getMaxX(), getBoundsInLocal().getMinY());
                break;
        }

        // not enough space to the top... menu aligns with top of the button
        if (this.translate.getY() + this.padding.get() < 0) {
            // hovermenu alignes with the optionbutton on the top
            this.arrow.setTranslateY(this.padding.get());
            // clear padding
            this.padding.set(NO_PADDING);
        } else {
            this.arrow.setTranslateY(getHalfHeight() - ARROW_CENTER.getWidth());
        }

        // not enough space to the bottom --> menu aligns with bottom of the button
        if (this.translate.getY() + this.horizontalHoverMenu.getHeight() > SceneUtil.getScene().getHeight()) {
            this.arrow.setTranslateY(this.horizontalHoverMenu.getHeight() - getHalfHeight());
            this.padding.set(-this.horizontalHoverMenu.getHeight() + getHeight());
        }

        this.buttonXLocation.set(this.translate.getX());
        this.buttonYLocation.set(this.translate.getY());
    }


    private void initLayout() {

        // check for changes
        this.localToSceneTransformProperty().addListener(new RealignListener<>(this.orientation));
        this.glassPane.visibleProperty().addListener(new RealignListener<>(this.orientation));

        initArrow();
        initHoverMenu();

        switch (this.orientation) {
            case TOP:
            case BOTTOM:
                initVerticalLayout();
                initAction(this.verticalHoverMenu);
                break;
            case LEFT:
            case RIGHT:
                initHorizontalLayout();
                initAction(this.horizontalHoverMenu);
                break;
        }
    }

    private void initAction(final Node node) {
        this.setOnAction((actionEvent) -> {

            node.setVisible(!node.isVisible());
            // if another option is shown, hide everything before switching to the current content
            if (!this.glassPane.getChildren().contains(node)) {
                LayoutUtil.hideAllChildren(this.glassPane);
            }
            // adjust hovermenu
            this.glassPane.getChildren().setAll(node);
            this.glassPane.setMaxWidth(this.parent.getWidth());
            this.glassPane.setMaxHeight(this.parent.getHeight());
            StackPane.setAlignment(this.glassPane, Pos.TOP_LEFT);

            switch (this.orientation) {
                case LEFT:
                case RIGHT:
                    this.glassPane.translateXProperty().bind(this.buttonXLocation);
                    this.glassPane.translateYProperty().bind(this.buttonYLocation.add(this.padding));
                    break;
                case TOP:
                case BOTTOM:
                    this.glassPane.translateXProperty().bind(this.buttonXLocation.add(this.padding));
                    this.glassPane.translateYProperty().bind(this.buttonYLocation);
                    break;
            }

            // show everything
            glassPane.setVisible(node.isVisible());

        });
    }

    private class HideableHBox extends HBox implements HideableComponent {
        private Hideable hideableParent;

        private HideableHBox(final Hideable hideableParent) {
            super();
            this.hideableParent = hideableParent;
        }

        public Hideable getHideableParent() {
            return hideableParent;
        }
    }

    private class HideableVBox extends VBox implements HideableComponent {
        private Hideable hideableParent;

        private HideableVBox(final Hideable hideableParent) {
            super();
            this.hideableParent = hideableParent;
        }

        public Hideable getHideableParent() {
            return hideableParent;
        }
    }

    /**
     * Hides the menu.
     */
    public void hideOptions() {
        this.glassPane.setVisible(false);
        this.verticalHoverMenu.setVisible(false);
        this.horizontalHoverMenu.setVisible(false);
    }

    private double getHalfWidth() {
        return getWidth() / 2;
    }

    private double getHalfHeight() {
        return getHeight() / 2;
    }


    @Override
    public void hide() {
        this.hideOptions();
    }
}
