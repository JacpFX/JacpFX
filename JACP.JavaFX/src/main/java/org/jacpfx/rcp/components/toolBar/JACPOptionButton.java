/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [JACPOptionButton.java]
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
package org.jacpfx.rcp.components.toolBar;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;

import static org.jacpfx.rcp.components.toolBar.JACPOptionButtonOrientation.BOTTOM;

/**
 * The Class JACPOptionButton.
 *
 * A simple button, with some more options. The button holds no message, except displaying the child options.
 *
 * @author Patrick Symmangk
 */
public class JACPOptionButton extends JACPHoverMenu {

    private VBox options = new VBox();

    private static final int INSETS = 5;

    private boolean hideOnAction;

    /**
     * Constructor without {@link JACPOptionButtonOrientation} option.
     * The default orientation will be BOTTOM.
     *
     * @param label  - the label to show
     * @param layout - the {@link FXComponentLayout} to use
     */
    public JACPOptionButton(final String label, final FXComponentLayout layout) {
        this(label, layout, BOTTOM, true);
    }

    /**
     * Constructor without {@link JACPOptionButtonOrientation} option.
     * The default orientation will be BOTTOM.
     *
     * @param label        - the label to show
     * @param layout       - the {@link FXComponentLayout} to use
     * @param hideOnAction - choose if the Menu will disappear after a button was pressed
     */
    public JACPOptionButton(final String label, final FXComponentLayout layout, boolean hideOnAction) {
        this(label, layout, BOTTOM, hideOnAction);
    }

    /**
     * Constructor with {@link JACPOptionButtonOrientation} option.
     * The Options will disappear after a button was pressed.
     *
     * @param label       - the label to show
     * @param layout      - the {@link FXComponentLayout} to use
     * @param orientation - the {@link JACPOptionButtonOrientation} option, depending on the Orientation of the {@link JACPToolBar}. [LEFT, TOP, RIGHT, BOTTOM]
     */
    public JACPOptionButton(final String label, final FXComponentLayout layout, final JACPOptionButtonOrientation orientation) {
        this(label, layout, orientation, true);
    }

    /**
     * Constructor with {@link JACPOptionButtonOrientation} option.
     * * The Options will disappear after a button was pressed.
     *
     * @param label        - the label to show
     * @param layout       - the {@link FXComponentLayout} to use
     * @param orientation  - the {@link JACPOptionButtonOrientation} option, depending on the Orientation of the {@link JACPToolBar}. [LEFT, TOP, RIGHT, BOTTOM]
     * @param hideOnAction - choose if the Menu will disappear after a button was pressed
     */
    public JACPOptionButton(final String label, final FXComponentLayout layout, final JACPOptionButtonOrientation orientation, boolean hideOnAction) {
        super(label, layout, orientation);
        this.hideOnAction = hideOnAction;
        this.getContentPane().getChildren().add(this.options);
    }

    /**
     * Adds a button (Option) to the menu.
     *
     * @param option - the Button to add to the menu.
     */
    public void addOption(final Button option) {
        option.setMaxWidth(Integer.MAX_VALUE);
        VBox.setMargin(option, new Insets(INSETS));

        if (this.hideOnAction) {
            this.addHideHandler(option);
        }

        this.options.getChildren().add(option);
    }

    /**
     * Adds multiple buttons to the menu.
     *
     * @param options - all the buttons to add
     */
    public void addOptions(final Button... options) {
        for (final Button btn : options) {
            addOption(btn);
        }
    }

    private void addHideHandler(final Button option) {
        option.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> this.hideOptions());

    }

    public boolean isHideOnAction() {
        return hideOnAction;
    }


}
