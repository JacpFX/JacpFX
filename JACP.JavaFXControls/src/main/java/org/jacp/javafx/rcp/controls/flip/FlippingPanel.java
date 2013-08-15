/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [FlippingPanel.java]
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
package org.jacp.javafx.rcp.controls.flip;

import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Patrick Symmangk
 *
 * The Class FlippingPanel.
 */
public class FlippingPanel extends Pane {

    /*STATIC STUFF*/
    private static double DEFAULT_FLIP_DURATION = 500.0;

    private static final double BY_ANGLE = 180.0;

    private static final double VIEW_ANGLE = 90;

    private static final double SIDES = 2.0;

    /* ALL THOSE PANELS*/
    private Region frontPanel;

    private Region backPanel;

    /* Some Transitions*/
    private RotateTransition flipToBackTransition;

    private RotateTransition flipToFrontTransition;

    private RotateTransition reverseflipToBackTransition;

    private RotateTransition reverseflipToFrontTransition;

    private boolean front = true;

    private double flipDuration;

    public FlippingPanel() {

    }

    public FlippingPanel(final Region pFrontPanel, final Region pBackPanel) {
        this(pFrontPanel, pBackPanel, DEFAULT_FLIP_DURATION);
    }

    public FlippingPanel(final Region pFrontPanel, final Region pBackPanel, double pFlipDuration) {
        super();
        this.frontPanel = pFrontPanel;
        this.backPanel = pBackPanel;
        this.flipDuration = pFlipDuration;
        init();
    }

    private void init() {
        initPanel(frontPanel, true);
        initPanel(backPanel, false);
        initRotateTransition();
        this.getChildren().addAll(frontPanel, backPanel);
    }

    private void initPanel(Region panel, boolean front) {
        panel.translateZProperty().bind(panel.widthProperty().divide(SIDES));
        panel.visibleProperty().bind(front ? panel.rotateProperty().lessThan(VIEW_ANGLE) : panel.rotateProperty().greaterThan(VIEW_ANGLE + BY_ANGLE));
        if (!front) {
            panel.setRotate(BY_ANGLE);
            panel.setRotationAxis(Rotate.Y_AXIS);
        }

    }

    private void initRotateTransition() {

        flipToBackTransition = createTransition(true, frontPanel);
        flipToFrontTransition = createTransition(true, backPanel);

        reverseflipToFrontTransition = createTransition(false, frontPanel);
        reverseflipToBackTransition = createTransition(false, backPanel);

    }

    /**
     * If the Flipping Panel shows the Backside, it will flip Back to "neutral" position and will show the front. 
     * 
     * Use this method if you have multiple Flipping-Panels in a View and you just want to flip all those panels back, that will show the backside. 
     * 
     * for 
     * 
     */

    public void reset() {
        if (!front) {
            flipToFront();
        }
    }

    public void flip() {
        if (front) {
            flipToBack();
        } else {
            flipToFront();
        }
    }

    /*##### PRIVATE STUFF #####*/

    private void flipToBack() {
        flipToFrontTransition.play();
        flipToBackTransition.play();
        toggleSide();
    }

    private void flipToFront() {
        reverseflipToFrontTransition.play();
        reverseflipToBackTransition.play();
        toggleSide();
    }

    private void toggleSide() {
        front = !front;
    }

    private RotateTransition createTransition(boolean forward, Node node) {

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(flipDuration / 2), node);
        rotateTransition.setByAngle(forward ? BY_ANGLE : -BY_ANGLE);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);

        return rotateTransition;
    }

    public Region getFrontSide() {
        return frontPanel;
    }

    public void setFrontSide(Region frontPanel) {
        this.frontPanel = frontPanel;
    }

    public Region getBackSide() {
        return backPanel;
    }

    public void setBackSide(Region backPanel) {
        this.backPanel = backPanel;
    }

}
