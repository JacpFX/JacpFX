package org.jacpfx.rcp.workbench;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.jacpfx.api.component.ui.Hideable;
import org.jacpfx.api.component.ui.HideableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick Symmangk (pete.jacp@gmail.com)
 */
public class GlobalMediator {

    private static GlobalMediator instance;
    private List<Hideable> hideAbles = new ArrayList<>();

    private GlobalMediator() {
        // Singleton
    }

    public static GlobalMediator getInstance() {
        if (instance == null) {
            instance = new GlobalMediator();
        }
        return instance;
    }


    public void registerHideAble(Hideable hideable) {
        this.hideAbles.add(hideable);
    }

    /**
     * Notifies all Hideables to hide its children or itself
     *
     * @param event - the MouseEvent
     */
    public void hideAllHideables(MouseEvent event) {
        Node target = this.detectHideable((Node) event.getTarget());
        for (Hideable hideable : this.hideAbles) {
            if (!hideable.equals(target)) {
                hideable.hide();
            }
        }
    }

    //    Detect all hideables in Node Tree. A Parent might be a Hideable or Hideable Component, so those nodes shouldn't be hidden.
    private Node detectHideable(final Node node) {
        if (node != null) {
            if (node instanceof Hideable) {
                return node;
            } else if (node instanceof HideableComponent) {
                return (Node) ((HideableComponent) node).getHideableParent();
            }
            return detectHideable(node.getParent());
        }
        return node;
    }

}
