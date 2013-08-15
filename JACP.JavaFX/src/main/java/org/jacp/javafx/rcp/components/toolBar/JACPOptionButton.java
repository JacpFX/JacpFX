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
package org.jacp.javafx.rcp.components.toolBar;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.util.CSSUtil;
import org.jacp.javafx.rcp.util.LayoutUtil;

import java.util.logging.Logger;

/**
 * The Class JACPOptionButton.
 *
 * @author Patrick Symmangk
 */
public class JACPOptionButton extends Button {

    private Pane glassPane;

    private VBox hoverMenu;

    private VBox options;

    private Logger LOGGER = Logger.getLogger(JACPOptionButton.class.getName());

    private SimpleDoubleProperty padding         = new SimpleDoubleProperty();
    private SimpleDoubleProperty buttonXLocation = new SimpleDoubleProperty();
    private SimpleDoubleProperty buttonYLocation = new SimpleDoubleProperty();

    public JACPOptionButton(String label, final FXComponentLayout layout) {
        super(label);
        this.glassPane  = layout.getGlassPane();
        initAction();
        initLayout();
    }

    private void initLayout(){
        this.options    = new VBox();
        this.hoverMenu  = new VBox();
        Pane arrow = new Pane();
        arrow.setMinHeight(5);
        arrow.setMaxWidth(10);
        hoverMenu.setAlignment(Pos.CENTER);
        hoverMenu.setVisible(false);
        CSSUtil.addCSSClass("top-arrow", arrow);
        CSSUtil.setBackgroundColor(options, "#333333");
        this.hoverMenu.getChildren().setAll(arrow, options);
        this.hoverMenu.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            //  ----------
            //  |        |
            //  ----------
            //      A
            // ------------
            // |           |

            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                // get padding to center hovermenu
                padding.set((getWidth() - hoverMenu.getWidth()) / 2);

            }
        });
    }

    private void initAction() {
        this.setOnAction((EventHandler<ActionEvent>)(actionEvent)-> {
                // get location of the optionbutton
                Point2D translate = localToScene(getBoundsInLocal().getMinX(), getBoundsInLocal().getMaxY());
                buttonXLocation.set(translate.getX());
                buttonYLocation.set(translate.getY());

                hoverMenu.setVisible(!hoverMenu.isVisible());
                // if another option is show, hide everything before switching to the current content
                if(!glassPane.getChildren().contains(hoverMenu)){
                    LayoutUtil.hideAllChildren(glassPane);
                }
                // adjust hovermenu
                glassPane.getChildren().setAll(hoverMenu);
                glassPane.setMaxWidth(options.getWidth());
                glassPane.setMaxHeight(options.getHeight());
                StackPane.setAlignment(glassPane, Pos.TOP_LEFT);
                glassPane.translateXProperty().bind(buttonXLocation.add(padding));
                glassPane.translateYProperty().bind(buttonYLocation);
                // show everything
                glassPane.setVisible(hoverMenu.isVisible());

        });
    }

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
        VBox.setMargin(option, new Insets(5,5,5,5));
    }


    public void hideOptions(){
        this.glassPane.setVisible(false);
        this.hoverMenu.setVisible(false);
    }



}
