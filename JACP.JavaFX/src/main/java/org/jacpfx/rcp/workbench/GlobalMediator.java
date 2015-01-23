package org.jacpfx.rcp.workbench;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.component.ui.Hideable;
import org.jacpfx.api.component.ui.HideableComponent;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author Patrick Symmangk (pete.jacp@gmail.com)
 */
public class GlobalMediator {

    private static GlobalMediator instance;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    public String workbenchId;
    private List<Hideable> hideAbles = new ArrayList<>();
    private Map<ToolbarPosition, JACPToolBar> toolbars = new HashMap<>();

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

    public void registerToolbar(final ToolbarPosition position, final JACPToolBar toolBar) {
        this.toolbars.put(position, toolBar);
    }

    public Map<ToolbarPosition, JACPToolBar> getRegisteredToolBars(final String parentId, final String componentId) {
        for (Iterator<JACPToolBar> iterator = this.toolbars.values().iterator(); iterator.hasNext(); ) {
            JACPToolBar toolBar = iterator.next();
            toolBar.setContext(parentId, componentId);
        }
        return Collections.unmodifiableMap(this.toolbars);
    }

    public JACPToolBar getRegisteredToolbar(final ToolbarPosition position, final String parentId, final String componentId) {
        JACPToolBar toolBar = this.toolbars.get(position);
        toolBar.setContext(parentId, componentId);
        return toolBar;
    }

    public void clearToolbar(final SubComponent<EventHandler<Event>, Event, Object> subComponent, final String parentId) {
        for (Iterator<JACPToolBar> iterator = this.toolbars.values().iterator(); iterator.hasNext(); ) {
            JACPToolBar toolBar = iterator.next();
            toolBar.clearRegions(subComponent, parentId);
        }
    }

    /**
     * Notifies all Hideables to hide its children or itself
     *
     * @param event - the MouseEvent
     */
    public void hideAllHideables(MouseEvent event) {
        Node target = this.detectHideable((Node) event.getTarget());
        this.hideAbles.stream().filter(hideable -> !hideable.equals(target)).forEach(Hideable::hide);
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

    public void handleToolBarButtons(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final boolean visible) {
        // fetch all nodes from all registred toolbars
        for (Iterator<JACPToolBar> iterator = this.toolbars.values().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            // handle visible state
            JACPToolBar toolBar = (JACPToolBar) node;
            toolBar.setButtonsVisible(perspective, visible);

        }
    }

    public void handleToolBarButtons(final SubComponent<EventHandler<Event>, Event, Object> subComponent, final String parentId, final boolean visible) {
        // fetch all nodes from all registred toolbars
        for (Iterator<JACPToolBar> iterator = this.toolbars.values().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            // handle visible state
            JACPToolBar toolBar = (JACPToolBar) node;
            toolBar.setButtonsVisible(subComponent, parentId, visible);
        }
    }

    public void handleWorkbenchToolBarButtons(final String id, final boolean visible) {
        // fetch all nodes from all registred toolbars
        for (Iterator<JACPToolBar> iterator = this.toolbars.values().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            // handle visible state
            JACPToolBar toolBar = (JACPToolBar) node;
            toolBar.setWorkbenchButtonsVisible(id, visible);

        }
    }

    public int countVisibleButtons() {
        final AtomicInteger count = new AtomicInteger();
        this.toolbars.values().forEach(toolbar -> count.getAndAdd(toolbar.countVisibleButtons()));
        return count.get();
    }

    public String getWorkbenchId() {
        return workbenchId;
    }

    public void setWorkbenchId(String workbenchId) {
        this.workbenchId = workbenchId;
    }
}
