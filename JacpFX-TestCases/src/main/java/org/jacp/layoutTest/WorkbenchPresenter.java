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

package org.jacp.layoutTest;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;

/**
 * This presenter covers the top-level layout concepts of the workbench.
 */
public class WorkbenchPresenter {

    @FXML
    private StackPane topPane;

    @FXML
    private StackPane menuPane;

    @FXML
    private StackPane centerPane;

    @FXML
    private StackPane bottomPane;

    public WorkbenchPresenter() {
    }

    private final BooleanProperty showMenuPane = new SimpleBooleanProperty(this, "showMenuPane", true);

    public final boolean isShowMenuPane() {
        return showMenuPane.get();
    }

    public final void setShowMenuPane(boolean showMenu) {
        showMenuPane.set(showMenu);
    }

    /**
     * Returns the property used to control the visibility of the menu panel.
     * When the value of this property changes to false then the menu panel will
     * slide out to the left).
     *
     * @return the property used to control the menu panel
     */
    public final BooleanProperty showMenuPaneProperty() {
        return showMenuPane;
    }

    private final BooleanProperty showBottomPane = new SimpleBooleanProperty(this, "showBottomPane", true);

    public final boolean isShowBottomPane() {
        return showBottomPane.get();
    }

    public final void setShowBottomPane(boolean showBottom) {
        showBottomPane.set(showBottom);
    }

    /**
     * Returns the property used to control the visibility of the bottom panel.
     * When the value of this property changes to false then the bottom panel
     * will slide out to the left).
     *
     * @return the property used to control the bottom panel
     */
    public final BooleanProperty showBottomPaneProperty() {
        return showBottomPane;
    }

    public final void initialize() {
        menuPaneLocation.addListener(it -> updateMenuPaneAnchors());
        bottomPaneLocation.addListener(it -> updateBottomPaneAnchors());

        showMenuPaneProperty().addListener(it -> animateMenuPane());
        showBottomPaneProperty().addListener(it -> animateBottomPane());

        menuPane.setOnMouseClicked(evt -> setShowMenuPane(false));

        centerPane.setOnMouseClicked(evt -> {
            setShowMenuPane(true);
            setShowBottomPane(true);
        });

        bottomPane.setOnMouseClicked(evt -> setShowBottomPane(false));
    }

/*
 * The updateMenu/BottomPaneAnchors methods get called whenever the value of
 * menuPaneLocation or bottomPaneLocation changes. Setting anchor pane
 * constraints will automatically trigger a relayout of the anchor pane
 * children.
 */

    private void updateMenuPaneAnchors() {
        setLeftAnchor(menuPane, getMenuPaneLocation());
        setLeftAnchor(centerPane, getMenuPaneLocation() + menuPane.getWidth());
    }

    private void updateBottomPaneAnchors() {
        setBottomAnchor(bottomPane, getBottomPaneLocation());
        setBottomAnchor(centerPane,
                getBottomPaneLocation() + bottomPane.getHeight());
        setBottomAnchor(menuPane,
                getBottomPaneLocation() + bottomPane.getHeight());
    }

    /*
    * Starts the animation for the menu pane.
    */
    private void animateMenuPane() {
        if (isShowMenuPane()) {
            slideMenuPane(0);
        } else {
            slideMenuPane(-menuPane.prefWidth(-1));
        }
    }

    /*
    * Starts the animation for the bottom pane.
    */
    private void animateBottomPane() {
        if (isShowBottomPane()) {
            slideBottomPane(0);
        } else {
            slideBottomPane(-bottomPane.prefHeight(-1));
        }
    }

/*
 * The animations are using the JavaFX timeline concept. The timeline updates
 * properties. In this case we have to introduce our own properties further
 * below (menuPaneLocation, bottomPaneLocation) because ultimately we need to
 * update layout constraints, which are not properties. So this is a little
 * work-around.
 */

    private void slideMenuPane(double toX) {
        KeyValue keyValue = new KeyValue(menuPaneLocation, toX);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    private void slideBottomPane(double toY) {
        KeyValue keyValue = new KeyValue(bottomPaneLocation, toY);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    private DoubleProperty menuPaneLocation = new SimpleDoubleProperty(this, "menuPaneLocation");

    private double getMenuPaneLocation() {
        return menuPaneLocation.get();
    }

    private DoubleProperty bottomPaneLocation = new SimpleDoubleProperty(this, "bottomPaneLocation");

    private double getBottomPaneLocation() {
        return bottomPaneLocation.get();
    }
}