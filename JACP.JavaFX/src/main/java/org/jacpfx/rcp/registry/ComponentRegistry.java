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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Global registry with references to all component.
 *
 * @author Andy Moncsek
 */
public class ComponentRegistry {
    private static final Map<String, SubComponent<EventHandler<Event>, Event, Object>> componentsReg = new ConcurrentHashMap<>();


    /**
     * clears registry on application shutdown
     */
    public static void clearOnShutdown() {
        componentsReg.clear();
    }

    /**
     * Registers a component.
     *
     * @param component the component to register
     */
    public static void registerComponent(
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        Objects.requireNonNull(component.getContext());
        componentsReg.putIfAbsent(component.getContext().getFullyQualifiedId(), component);
    }

    /**
     * Removes component from registry.
     *
     * @param component the component to remove
     */
    public static void removeComponent(
            final SubComponent<EventHandler<Event>, Event, Object> component) {
        Objects.requireNonNull(component.getContext());
        componentsReg.remove(component.getContext().getFullyQualifiedId());
    }


    /**
     * Returns all component for a parent id
     *
     * @param parentId the perspective id of the components to find
     * @return a list of @see {SubComponent}
     */
    public static List<SubComponent<EventHandler<Event>, Event, Object>> findComponentsByParentId(final String parentId) {

        return componentsReg.values().stream().
                filter(nonNull -> nonNull != null && nonNull.getContext() != null).
                filter(comp -> comp.getContext().getParentId() != null).
                filter(c -> c.getContext().getParentId().equals(parentId)).
                collect(Collectors.toList());
    }

    /**
     * Find a component by qualified name like parentId.componentId
     *
     * @param targetId the component id
     * @return The @see{SubComponent}
     */
    public static SubComponent<EventHandler<Event>, Event, Object> findComponentByQualifiedId(
            final String targetId) {
        return componentsReg.get(targetId);

    }

    /**
     * Find a component by parent and componentId
     *
     * @param parentId    the parent id of the component
     * @param componentId the component id
     * @return The @see{SubComponent}
     */
    public static SubComponent<EventHandler<Event>, Event, Object> findComponentByQualifiedId(
            final String parentId, final String componentId) {

        return findComponentByQualifiedId(FXUtil.getQualifiedComponentId(parentId,componentId));

    }


}
