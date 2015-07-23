/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ComponentRegistry.java]
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
package org.jacpfx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.util.FXUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global registry with references to all component.
 *
 * @author Andy Moncsek
 *
 */
public class ComponentRegistry {
    private static final List<SubComponent<EventHandler<Event>, Event, Object>> components = new CopyOnWriteArrayList<>();


    /**
     * clears registry on application shutdown
     */
    public static void clearOnShutdown() {
        components.clear();
    }

    /**
     * Registers a component.
     *
     * @param component the component to register
     */
    public static void registerComponent(
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (!components.contains(component))
            components.add(component);

    }

    /**
     * Removes component from registry.
     *
     * @param component the component to remove
     */
    public static void removeComponent(
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        if (components.contains(component))
            components.remove(component);

    }


    /**
     * Returns all component for a parent id
     * @param parentId the perspective id of the components to find
     * @return a list of @see {SubComponent}
     */
    public static List<SubComponent<EventHandler<Event>, Event, Object>> findComponentsByParentId(final String parentId) {
            return FXUtil.getObserveableByParentId(parentId,Collections.unmodifiableList(components));
    }

    /**
     * Find a component by qualified name like parentId.componentId
     * @param targetId the component id
     * @return The @see{SubComponent}
     */
    public static  SubComponent<EventHandler<Event>, Event, Object> findComponentByQualifiedId(
            final String targetId) {
        return FXUtil.getObserveableByQualifiedId(targetId,
                Collections.unmodifiableList(components));

    }

    /**
     *   Find a component by parent and componentId
     * @param parentId the parent id of the component
     * @param componentId the component id
     * @return  The @see{SubComponent}
     */
    public static  SubComponent<EventHandler<Event>, Event, Object> findComponentByQualifiedId(
            final String parentId, final String componentId) {
        return FXUtil.getObserveableByQualifiedId(parentId,componentId,
                Collections.unmodifiableList(components));

    }



}
