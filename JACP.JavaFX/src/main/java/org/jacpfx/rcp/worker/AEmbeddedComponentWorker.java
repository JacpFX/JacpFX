/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [AFX2ComponentWorker.java]
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
package org.jacpfx.rcp.worker;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import org.jacpfx.api.component.IComponentHandle;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.component.IUIComponent;
import org.jacpfx.api.exceptions.InvalidComponentMatch;
import org.jacpfx.rcp.component.AFXComponent;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.WorkerUtil;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * handles component methods in own thread;
 *
 * @author Andy Moncsek
 */
public abstract class AEmbeddedComponentWorker extends Thread {


    AEmbeddedComponentWorker(final String name) {
        super(name);
        this.setDaemon(true);
    }

    /**
     * perform all cleanings after worker was interrupted.
     */
    public abstract void cleanAfterInterrupt();

    /**
     * find valid target component in perspective
     *
     * @param targetComponents, the target components provided by the parent perspective
     * @param id,               a target id
     * @return returns a target node by id
     */
    Node getValidContainerById(
            final Map<String, Node> targetComponents, final String id) {
        return targetComponents.get(id);
    }

    /**
     * removes old ui component of subcomponent form parent ui component
     *
     * @param parent,           the parent node
     * @param currentContainer, a valid container which contains components root
     */
    void handleOldComponentRemove(final Node parent,
                                  final Node currentContainer) {
        WorkerUtil.handleViewState(currentContainer, false);
        final ObservableList<Node> children = FXUtil.getChildren(parent);
        children.remove(currentContainer);
    }


    /**
     * Handle target change inside perspective.
     *
     * @param component,      the component
     * @param validContainer, a valid JavaFX Node
     */
    void handleLayoutTargetChange(
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component,
            final Node validContainer) {
        WorkerUtil.addComponentByType(validContainer, component);
    }

    /**
     * Handle target change to an other perspective. If target component not
     * found in current perspective, move to an other perspective and run
     * tear down.
     *
     * @param delegateQueue, the component delegate queue
     * @param component,     a component
     */
    void handlePerspectiveChange(
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> delegateQueue,
            final IUIComponent<Node, EventHandler<Event>, Event, Object> component) {
        // handle target outside current perspective
        WorkerUtil.changeComponentTarget(delegateQueue, component);
    }

    void log(final String message) {
        if (Logger.getLogger(AEmbeddedComponentWorker.class.getName()).isLoggable(
                Level.FINE)) {
            Logger.getLogger(AEmbeddedComponentWorker.class.getName()).fine(
                    ">> " + message);
        }
    }

    /**
     * Set desired caching to component
     *
     * @param cache,     cache enabled
     * @param hint,      the cache hint
     * @param component, the component
     */
    void setCacheHints(boolean cache, CacheHint hint, final AFXComponent component) {
        final Node currentRoot = component.getRoot();
        if (currentRoot == null) return;
        final Node parentNode = currentRoot.getParent();
        if (parentNode == null) return;
        if (currentRoot.getParent().isCache() != cache)
            currentRoot.getParent().setCache(cache);
        if (!currentRoot.getParent().getCacheHint().equals(hint))
            currentRoot.getParent().setCacheHint(CacheHint.SPEED);
    }

    /**
     * Checks if component is in correct state.
     * @param component
     */
    void checkValidComponent(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IComponentHandle<?, Event, Object> handle = component.getComponent();
        if (handle == null) throw new InvalidComponentMatch("Component is not initialized correctly");
        if (component == null || component.getContext() == null || component.getContext().getId() == null)
            throw new InvalidComponentMatch("Component is in invalid state while initialisation:" + handle.getClass()+" this can happen when component is in shutdown process");
    }
}
