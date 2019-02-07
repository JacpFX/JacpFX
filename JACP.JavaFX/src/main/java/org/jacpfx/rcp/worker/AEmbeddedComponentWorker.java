/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AEmbeddedComponentWorker.java]
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
package org.jacpfx.rcp.worker;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.api.exceptions.InvalidComponentMatch;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.WorkerUtil;

import java.util.Map;
import java.util.Optional;
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
     * @param targetComponents, the target component provided by the parent perspective
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
     * @param currentContainer, a valid container which contains component root
     */
    void handleOldComponentRemove(final Node parent,
                                  final Node currentContainer) {
        WorkerUtil.handleViewState(currentContainer, false);
        final Optional<ObservableList<Node>> children = FXUtil.getChildren(parent);
        children.ifPresent(childList -> {
            childList.remove(currentContainer);
        });
    }


    void log(final String message) {
        if (Logger.getLogger(AEmbeddedComponentWorker.class.getName()).isLoggable(
                Level.FINE)) {
            Logger.getLogger(AEmbeddedComponentWorker.class.getName()).finest(
                    ">> " + message);
        }
    }

    /**
     * Checks if component is in correct state.
     * @param componentToCheck
     */
    void checkValidComponent(final SubComponent<EventHandler<Event>, Event, Object> componentToCheck) {
        if (componentToCheck == null)
            throw new InvalidComponentMatch("Component is in invalid state while initialisation: this can happen when component is in shutdown process");
        final JacpContext<EventHandler<Event>, Object> context = componentToCheck.getContext();
        if (context == null || context.getId() == null)
            throw new InvalidComponentMatch("Component is in invalid state while initialisation: this can happen when component is in shutdown process");
        final ComponentHandle<?, Event, Object> handle = componentToCheck.getComponent();
        if (handle == null) throw new InvalidComponentMatch("Component is not initialized correctly");
    }
}
