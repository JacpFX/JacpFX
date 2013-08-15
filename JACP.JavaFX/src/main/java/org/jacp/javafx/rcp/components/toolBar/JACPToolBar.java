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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jacp.javafx.rcp.util.CSSUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class JACPToolBar.
 *
 * @author Patrick Symmangk
 *
 */
public class JACPToolBar extends ToolBar implements ChangeListener<Orientation>, ListChangeListener<Node> {

    private final Map<String, List<Node>> nodeMap = new HashMap<>();

    /** The left buttons. */
    private HBox leftButtons;

    /** The center buttons. */
    private HBox centerButtons;

    /** The right buttons. */
    private HBox rightButtons;

    /** The horizontal tool bar. */
    private HBox horizontalToolBar;

    /** The top buttons. */
    private VBox topButtons;

    /** The middle buttons. */
    private VBox middleButtons;

    /** The bottom buttons. */
    private VBox bottomButtons;

    /** The vertical tool bar. */
    private VBox verticalToolBar;

    private final double toolbarPadding = 20;

    /**
     * Instantiates a new jACP tool bar.
     */
    public JACPToolBar() {
        super();
        this.getStyleClass().add(CSSUtil.CSSConstants.CLASS_JACP_TOOL_BAR);

        this.orientationProperty().addListener(this);
        this.getItems().addListener(this);
        if (this.getOrientation() == Orientation.VERTICAL) {
            this.initVerticalToolBar();
        } else {
            this.initHorizontalToolBar();
        }
    }

    /**
     * Adds the.
     *
     * @param node
     *            the node
     */
    public void add(String id, final Node node) {

        if (this.getOrientation() == Orientation.HORIZONTAL) {
            HBox.setMargin(node, new Insets(0, 2, 0, 2));
            this.leftButtons.getChildren().add(node);
        } else {
            VBox.setMargin(node, new Insets(2, 0, 2, 0));
            this.topButtons.getChildren().add(node);
        }
        getNodes(id).add(node);

    }


    public void removeForId(String id){
        for(Node node : getNodes(id)){
            remove(node);
        }
    }

    /**
     * Removes the.
     *
     * @param node the node
     */
    public void remove(final Node node) {
        if (this.getOrientation() == Orientation.HORIZONTAL) {
            this.leftButtons.getChildren().remove(node);
            this.centerButtons.getChildren().remove(node);
            this.rightButtons.getChildren().remove(node);
        } else {
            this.topButtons.getChildren().remove(node);
            this.middleButtons.getChildren().remove(node);
            this.bottomButtons.getChildren().remove(node);
        }
    }


    public void addAll(String id, Node... nodes){
        for(final Node node : nodes){
            add(id, node);
        }
    }


    public void addAllOnEnd(String id, Node... nodes){
        for(final Node node : nodes){
            addOnEnd(id, node);
        }
    }

    public void addAllToCenter(String id, Node... nodes){
        for(final Node node : nodes){
            addToCenter(id, node);
        }
    }

    /**
     * Adds the on end.
     *
     * @param node
     *            the node
     */
    public void addToCenter(String id, final Node node) {
        if (this.getOrientation() == Orientation.HORIZONTAL) {
            HBox.setMargin(node, new Insets(0, 2, 0, 2));
            this.centerButtons.getChildren().add(node);
        } else {
            VBox.setMargin(node, new Insets(2, 0, 2, 0));
            this.middleButtons.getChildren().add(node);
        }

        getNodes(id).add(node);

        this.bind();
    }
/**
     * Adds the on end.
     *
     * @param node
     *            the node
     */
    public void addOnEnd(String id, final Node node) {
        if (this.getOrientation() == Orientation.HORIZONTAL) {
            HBox.setMargin(node, new Insets(0, 2, 0, 2));
            this.rightButtons.getChildren().add(node);
        } else {
            VBox.setMargin(node, new Insets(2, 0, 2, 0));
            this.bottomButtons.getChildren().add(node);
        }

        getNodes(id).add(node);

        this.bind();
    }

    /**
     * Inits the horizontal tool bar.
     */
    private void initHorizontalToolBar() {
        /*
         * ----------------------------------------------------------------------
         * |left hand side buttons| | centered buttons| |right hand side buttons|
         * ----------------------------------------------------------------------
         */
        this.clear();
        // the main box for the toolbar
        // holds the left hand side and the right hand side buttons!
        // the buttons are separated by a spacer box, that fills the remaining
        // width
        this.horizontalToolBar = new HBox();
        // the place for the buttons on the left hand side
        this.leftButtons = new HBox();
        this.leftButtons.setAlignment(Pos.CENTER_LEFT);
        // the spacer that fills the remaining width between the buttons
        this.centerButtons = new HBox();
        this.centerButtons.setAlignment(Pos.CENTER);
        HBox.setHgrow(this.centerButtons, Priority.ALWAYS);


        this.rightButtons = new HBox();
        this.rightButtons.setAlignment(Pos.CENTER_RIGHT);

        CSSUtil.addCSSClass(CSSUtil.CSSConstants.CLASS_JACP_BUTTON_BAR, this,leftButtons, this.centerButtons, this.rightButtons);
        this.horizontalToolBar.getChildren().addAll(this.leftButtons, this.centerButtons, this.rightButtons);
        this.getItems().add(0, this.horizontalToolBar);
    }

    /**
     * Inits the vertical tool bar.
     */
    private void initVerticalToolBar() {
        /*
         * --------------------------------------------------------------- |
         * |left hand side buttons| |spacer| |right hand side buttons| |
         * ---------------------------------------------------------------
         */
        this.clear();
        // the main box for the toolbar
        // holds the lefthand side and the right hand side buttons!
        // the buttons are separated by a spacer box, that fills the remaining
        // width
        this.verticalToolBar = new VBox();

        // the place for the buttons on the left hand side
        this.topButtons = new VBox();
        this.topButtons.setAlignment(Pos.CENTER_LEFT);
        // the spacer that fills the remaining width between the buttons
        this.middleButtons = new VBox();
        VBox.setVgrow(this.middleButtons, Priority.ALWAYS);

        this.bottomButtons = new VBox();
        this.bottomButtons.setAlignment(Pos.CENTER_RIGHT);

        this.verticalToolBar.getChildren().addAll(this.topButtons, this.middleButtons, this.bottomButtons);
        this.getItems().add(0, this.verticalToolBar);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue
     * , java.lang.Object, java.lang.Object)
     */
    @Override
    public void changed(final ObservableValue<? extends Orientation> arg0, final Orientation oldOrientation, final Orientation newOrientation) {

        if (newOrientation == Orientation.VERTICAL) {
            this.initVerticalToolBar();
        } else {
            this.initHorizontalToolBar();
        }
        this.unbind();

    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.
     * ListChangeListener.Change)
     */
    @Override
    public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Node> arg0) {
        if (this.getItems().size() > 1) {
            this.unbind();
        }

    }

    /**
     * Bind.
     */
    private void bind() {
        if (this.getOrientation() == Orientation.HORIZONTAL) {
            if (this.horizontalToolBar != null) {
                this.horizontalToolBar.maxWidthProperty().bind(this.widthProperty().subtract(this.toolbarPadding));
                this.horizontalToolBar.minWidthProperty().bind(this.widthProperty().subtract(this.toolbarPadding));
            }
        } else {
            if (this.verticalToolBar != null) {
                this.verticalToolBar.maxHeightProperty().bind(this.heightProperty().subtract(this.toolbarPadding));
                this.verticalToolBar.minHeightProperty().bind(this.heightProperty().subtract(this.toolbarPadding));
            }
        }
    }

    /**
     * Unbind.
     */
    private void unbind() {
        if (this.getOrientation() == Orientation.HORIZONTAL) {
            if (this.horizontalToolBar != null) {
                this.horizontalToolBar.maxWidthProperty().unbind();
                this.horizontalToolBar.minWidthProperty().unbind();
            }
        } else {
            if (this.verticalToolBar != null) {
                this.verticalToolBar.maxHeightProperty().unbind();
                this.verticalToolBar.minHeightProperty().unbind();
            }
        }
    }

    /**
     * Clear.
     */
    private void clear() {
        if (!this.getItems().isEmpty()) {
            final Node node = this.getItems().get(0);
            if (node instanceof HBox || node instanceof VBox) {
                this.getItems().remove(node);
            }
        }
    }

    public List<Node> getNodes(String id){
        if(nodeMap.containsKey(id)){
            return nodeMap.get(id);
        }
        List<Node> currentList = new ArrayList<>();
        nodeMap.put(id, currentList);

        return currentList;
    }



}
