package org.jacp.javafx.rcp.util;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * The Class LayoutUtil.
 *
 * Util to group some Layout-Function.
 *
 * @author Patrick Symmangk
 *
 */
public class LayoutUtil {

    public static class GridPaneUtil {


        /**
         *
         * Set GridPane hGrow AND vGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setFullGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    GridPane.setVgrow(node, priority);
                    GridPane.setHgrow(node, priority);
                }
            }
        }

        /**
         *
         * Set GridPane hGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setHGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    GridPane.setHgrow(node, priority);
                }
            }
        }


        /**
         *
         * Set GridPane vGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setVGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    GridPane.setVgrow(node, priority);
                }
            }
        }
        
    }

    /**
     * The HBoxUtil subclass.
     *
     */
    public static class HBoxUtil {

        /**
         *
         * Set HBox hGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setHGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    HBox.setHgrow(node, priority);
                }
            }
        }

        /**
         *
         * Set margin to multiple Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Node... nodes) {
            setMargin(insets, Arrays.asList(nodes));

        }

        /**
         *
         * Set margin to a collection of Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Collection<Node> nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    HBox.setMargin(node, insets);
                }
            }
        }

    }

    /**
     * The VBoxUtil subclass.
     *
     */
    public static class VBoxUtil {

        /**
         *
         * Set VBox vGrow to multiple Nodes
         *
         * @param priority - priority to set
         * @param nodes - the nodes
         */
        public static void setVGrow(Priority priority, Node... nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    VBox.setVgrow(node, priority);
                }
            }
        }

        /**
         *
         * Set margin to multiple Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Node... nodes) {
            setMargin(insets, Arrays.asList(nodes));

        }

        /**
         *
         * Set margin to a collection of Nodes.
         *
         * @param insets - the margin insets
         * @param nodes - the nodes to receive the margin
         */
        public static void setMargin(Insets insets, Collection<Node> nodes) {
            for (Node node : nodes) {
                if (node != null) {
                    VBox.setMargin(node, insets);
                }
            }
        }
    }

    public static void hideAllChildren(Region parent){
        for(Node node : parent.getChildrenUnmodifiable() ){
            node.setVisible(false);
        }
    }
}
