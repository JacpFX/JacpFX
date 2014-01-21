/************************************************************************
 *
 * Copyright (C) 2010 - 2014
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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jacpfx.api.util.CustomSecurityManager;
import org.jacpfx.rcp.util.CSSUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static org.jacpfx.rcp.util.CSSUtil.CSSConstants.CLASS_JACP_TOOL_BAR;

/**
 * The Class JACPToolBar.
 *
 * @author Patrick Symmangk
 */
public class JACPToolBar extends ToolBar implements ChangeListener<Orientation>, ListChangeListener<Node> {

    private final static CustomSecurityManager customSecurityManager = new CustomSecurityManager();

    private final Map<String, List<Node>> nodeMap = new HashMap<>();

    private static final int FIRST_PLACE = 0;

    /**
     * The horizontal tool bar.
     */
    private HBox horizontalToolBar;

    /**
     * The vertical tool bar.
     */
    private VBox verticalToolBar;

    /**
     * stores the current toolbar containers
     */
    private ConcurrentHashMap<JACPToolBarPosition, Pane> toolBarContainer;


    /**
     * Instantiates a new jACP tool bar.
     */
    public JACPToolBar() {
        super();

        this.getStyleClass().add(CLASS_JACP_TOOL_BAR);
        this.orientationProperty().addListener(this);
        this.getItems().addListener(this);

        if (this.getOrientation() == VERTICAL) {
            this.initVerticalToolBar();
        } else {
            this.initHorizontalToolBar();
        }
    }


    public void add(final Node node) {
        this.add(customSecurityManager.getCallerClassName(), node);
    }

    /**
     * Adds the.
     *
     * @param id the parent id
     * @param node the node
     */
    public void add(String id, final Node node) {
        this.addNode(id, node, JACPToolBarPosition.START, false);
    }


    public void removeForId(String id) {
        for (Iterator<Node> iterator = this.getNodes(id).iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            this.remove(node);
        }
    }

    /**
     * Add multiple nodes to the toolbar.
     * Those nodes are added by id and will appear in the first place of the toolbar
     *
     * The id is the name of the calling component by default.
     * For self-managed ids see {@link #addAll(String id, Node... nodes)}
     *
     * @param nodes the nodes to add
     */
    public void addAll(final Node... nodes) {
        this.addAll(this.getDefaultId(), nodes);
    }


    /**
     * Add multiple nodes to the toolbar.
     * Those nodes are added by id and will appear in the first place of the toolbar
     *
     * @param id    - the id the nodes will refer to
     * @param nodes the nodes to add
     */
    public void addAll(String id, Node... nodes) {
        for (final Node node : nodes) {
            this.add(id, node);
        }
    }

    /**
     * Add multiple nodes to the toolbar.
     * Those nodes are added by id and will appear on the end of the toolbar
     * Means right hand side for {@link org.jacpfx.api.util.ToolbarPosition#NORTH} and {@link org.jacpfx.api.util.ToolbarPosition#SOUTH}
     * and on the bottom for {@link org.jacpfx.api.util.ToolbarPosition#EAST} and {@link org.jacpfx.api.util.ToolbarPosition#WEST}
     *
     * The id is the name of the calling component by default.
     * For self-managed ids see {@link #addAllOnEnd(String id, Node... nodes)}
     *
     * @param nodes the nodes to add
     */
    public void addAllOnEnd(final Node... nodes) {
        this.addAllOnEnd(this.getDefaultId(), nodes);
    }


    /**
     * Add multiple nodes to the toolbar.
     * Those nodes are added by id and will appear on the end of the toolbar
     * Means right hand side for {@link org.jacpfx.api.util.ToolbarPosition#NORTH} and {@link org.jacpfx.api.util.ToolbarPosition#SOUTH}
     * and on the bottom for {@link org.jacpfx.api.util.ToolbarPosition#EAST} and {@link org.jacpfx.api.util.ToolbarPosition#WEST}
     *
     * @param id    self managed id for the given nodes
     * @param nodes the nodes to add
     */
    public void addAllOnEnd(final String id, final Node... nodes) {
        for (final Node node : nodes) {
            this.addOnEnd(id, node);
        }
    }


    /**
     * Add multiple nodes to the toolbar.
     * Those nodes are added by id and will appear in the middle of the toolbar
     *
     * The id is the name of the calling component by default.
     * For self-managed ids see {@link #addAllOnEnd(String id, Node... nodes)}
     *
     * @param nodes the nodes to add
     */
    public void addAllToCenter(final Node... nodes) {
        this.addAllToCenter(this.getDefaultId(), nodes);
    }

    /**
     * Add multiple nodes to the toolbar.
     * Those nodes are added by id and will appear in the middle of the toolbar
     *
     * @param id    self managed id for the given nodes
     * @param nodes the nodes to add
     */
    public void addAllToCenter(final String id, final Node... nodes) {
        for (final Node node : nodes) {
            this.addToCenter(id, node);
        }
    }

    /**
     * Adds the on end.
     *
     * @param id the parent id
     * @param node the node
     */
    public void addToCenter(String id, final Node node) {
        this.addNode(id, node, JACPToolBarPosition.MIDDLE);
    }

    /**
     * Adds the on end.
     *
     * @param id the parent id
     * @param node the node
     */
    public void addOnEnd(String id, final Node node) {
        this.addNode(id, node, JACPToolBarPosition.END);
    }


    /**
     * Removes the.
     *
     * @param node the node
     */
    public void remove(final Node node) {
        for (Iterator<Pane> iterator = this.toolBarContainer.values().iterator(); iterator.hasNext(); ) {
            Pane toolBarItem = iterator.next();
            toolBarItem.getChildren().remove(node);
        }
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

        if (newOrientation == VERTICAL) {
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
     * returns all the nodes for the given id.
     *
     * @param id - the custom id, which was provided on add .
     * @return A list of nodes, if any or an empty list.
     */
    public List<Node> getNodes(String id) {
        return Collections.unmodifiableList(this.getInternalNodes(id));
    }

    /*
        adds a node for a given Id on the given Positon and updates all bindings
     */
    private void addNode(final String id, final Node node, final JACPToolBarPosition position) {
        this.addNode(id, node, position, true);
    }

    /*
        adds a node for a given Id on the given Positon and updates all bindings, as needed.
     */
    private void addNode(final String id, final Node node, final JACPToolBarPosition position, boolean bind) {
        this.setInsets(node);
        this.toolBarContainer.get(position).getChildren().add(node);
        this.getInternalNodes(id).add(node);
        if (bind) {
            this.bind();
        }
    }

    /*
        get the Internal Nodes to add some more Nodes.
     */
    private List<Node> getInternalNodes(String id) {
        if (this.nodeMap.containsKey(id)) {
            return this.nodeMap.get(id);
        }
        List<Node> currentList = new ArrayList<>();
        this.nodeMap.put(id, currentList);

        return currentList;
    }

    /*
     * Bind the needed Properties to fit the width or the height
     */
    private void bind() {

        double toolbarPadding = 20;

        if (this.getOrientation() == HORIZONTAL) {
            if (this.horizontalToolBar != null) {
                this.horizontalToolBar.maxWidthProperty().bind(this.widthProperty().subtract(toolbarPadding));
                this.horizontalToolBar.minWidthProperty().bind(this.widthProperty().subtract(toolbarPadding));
            }
        } else {
            if (this.verticalToolBar != null) {
                this.verticalToolBar.maxHeightProperty().bind(this.heightProperty().subtract(toolbarPadding));
                this.verticalToolBar.minHeightProperty().bind(this.heightProperty().subtract(toolbarPadding));
            }
        }
    }


    /*
        add Insets to the buttons as needed
     */
    private void setInsets(final Node node) {
        if (this.getOrientation() == HORIZONTAL) {
            HBox.setMargin(node, new Insets(0, 2, 0, 2));
        } else {
            VBox.setMargin(node, new Insets(2, 0, 2, 0));
        }
    }


    /**
     * /**
     * Unbind.
     */
    private void unbind() {
        if (this.getOrientation() == HORIZONTAL) {
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
            final Node node = this.getItems().get(FIRST_PLACE);
            if (node instanceof HBox || node instanceof VBox) {
                this.getItems().remove(node);
            }
        }
        // reset "cache"
        this.toolBarContainer = new ConcurrentHashMap<>();
    }


    private String getDefaultId() {
        return customSecurityManager.getCallerClassName();
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
        HBox leftButtons = new HBox();
        leftButtons.setAlignment(Pos.CENTER_LEFT);
        this.toolBarContainer.put(JACPToolBarPosition.START, leftButtons);
        // the spacer that fills the remaining width between the buttons
        HBox centerButtons = new HBox();
        centerButtons.setAlignment(Pos.CENTER);
        this.toolBarContainer.put(JACPToolBarPosition.MIDDLE, centerButtons);

        HBox rightButtons = new HBox();
        rightButtons.setAlignment(Pos.CENTER_RIGHT);
        this.toolBarContainer.put(JACPToolBarPosition.END, rightButtons);

        HBox.setHgrow(centerButtons, Priority.ALWAYS);
        CSSUtil.addCSSClass(CSSUtil.CSSConstants.CLASS_JACP_BUTTON_BAR, this, leftButtons, centerButtons, rightButtons);
        this.horizontalToolBar.getChildren().addAll(leftButtons, centerButtons, rightButtons);
        this.getItems().add(FIRST_PLACE, this.horizontalToolBar);
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
        // holds the left hand side and the right hand side buttons!
        // the buttons are separated by a spacer box, that fills the remaining
        // width
        this.verticalToolBar = new VBox();

        // the place for the buttons on the left hand side
        VBox topButtons = new VBox();
        topButtons.setAlignment(Pos.CENTER_LEFT);
        this.toolBarContainer.put(JACPToolBarPosition.START, topButtons);
        // the spacer that fills the remaining width between the buttons

        VBox middleButtons = new VBox();
        this.toolBarContainer.put(JACPToolBarPosition.MIDDLE, middleButtons);

        VBox bottomButtons = new VBox();
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        this.toolBarContainer.put(JACPToolBarPosition.END, bottomButtons);

        VBox.setVgrow(middleButtons, Priority.ALWAYS);
        this.verticalToolBar.getChildren().addAll(topButtons, middleButtons, bottomButtons);
        this.getItems().add(FIRST_PLACE, this.verticalToolBar);
    }

    private enum JACPToolBarPosition {
        START, MIDDLE, END;
    }


}
