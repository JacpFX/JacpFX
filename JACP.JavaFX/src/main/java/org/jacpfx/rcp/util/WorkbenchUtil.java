/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [WorkbenchUtil.java]
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

package org.jacpfx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.component.Injectable;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.exceptions.NonUniqueComponentException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.perspective.EmbeddedFXPerspective;
import org.jacpfx.rcp.registry.ClassRegistry;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 03.09.13
 * Time: 08:59
 * Contains utility classes for a Workbench
 */
public class WorkbenchUtil {

    private static final Logger LOGGER = Logger.getLogger(WorkbenchUtil.class.getName());

    private final Launcher<?> launcher;

    private WorkbenchUtil(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    /**
     * Returns an instance of the WorkbenchUtil
     *
     * @param launcher the launcher instance
     * @return The WorkbechUtil instance
     */
    public static WorkbenchUtil getInstance(final Launcher<?> launcher) {
        return new WorkbenchUtil(launcher);
    }


    /**
     * Creates all perspective instances by annotated id's in @Workbench annotation
     *
     * @param annotation , the workbench annotation
     * @return a list with all perspective associated with a workbench
     */
    public List<Perspective<Node, EventHandler<Event>, Event, Object>> createPerspectiveInstances(final Workbench annotation) {
        final Stream<String> componentIds = Stream.of(annotation.perspectives());
        final Stream<Injectable> perspectiveHandlerList = componentIds.map(this::mapToInjectable);
        final List<Injectable> tmp = perspectiveHandlerList.collect(Collectors.toList());
        checkUniqueComponentReferences(tmp.stream());
        return tmp.stream().map(this::mapToPerspective).collect(Collectors.toList());
    }

    private void checkUniqueComponentReferences(final Stream<Injectable> perspectiveHandlerList) {
      perspectiveHandlerList.
                map(Object::getClass).
                map(clazz -> clazz.getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class)).
                filter(ann -> ann != null).
                map(annotation -> new PerspectiveCheckDTO(annotation.id(), Arrays.asList(annotation.components()))).
                collect(UniqueCheckConsumer::new, UniqueCheckConsumer::accept, UniqueCheckConsumer::andThen);

    }

    private class UniqueCheckConsumer implements Consumer<PerspectiveCheckDTO> {

        @Override
        public void accept(PerspectiveCheckDTO perspectiveCheckDTO) {
            final Set<String> duplicateTest = new HashSet<>(perspectiveCheckDTO.getComponentIds());
            if (duplicateTest.size()<perspectiveCheckDTO.getComponentIds().size())
                throw new NonUniqueComponentException("ERROR in perspective " + perspectiveCheckDTO.getId() + " non unique component ids: " + perspectiveCheckDTO.getComponentIds());

        }

        @Override
        public Consumer<PerspectiveCheckDTO> andThen(Consumer<? super PerspectiveCheckDTO> after) {
            return this;
        }
    }

    public class PerspectiveCheckDTO {
        private final String id;
        private final List<String> componentIds;

        public PerspectiveCheckDTO(final String id, final List<String> componentIds) {
            this.id = id;
            this.componentIds = componentIds;
        }

        public String getId() {
            return id;
        }

        public List<String> getComponentIds() {
            return componentIds;
        }

    }

    /**
     * Returns a FXPerspective instance.
     *
     * @param handler, the handler
     * @return The FXPerspective instance
     */
    private Perspective<Node, EventHandler<Event>, Event, Object> mapToPerspective(Injectable handler) {
        return new EmbeddedFXPerspective(handler);
    }

    /**
     * Returns a handler by id.
     *
     * @param id, The component id
     * @return, The handler instance.
     */
    private Injectable mapToInjectable(final String id) {
        final Class perspectiveClass = ClassRegistry.getPerspectiveClassById(id);
        final Object component = launcher.registerAndGetBean(perspectiveClass, id, Scope.SINGLETON);
        if (Injectable.class.isAssignableFrom(component.getClass())) {
            return Injectable.class.cast(component);
        } else {
            throw new InvalidParameterException("Only Perspective component are allowed");
        }
    }

    /**
     * set meta attributes defined in annotations
     *
     * @param perspective, the perspective where to handle the metadata
     * @param parentId,    the id of parent workbench
     */
    public static void handleMetaAnnotation(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final String parentId) {
        final Injectable handler = perspective.getPerspective();
        final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation = handler.getClass()
                .getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
        if (perspectiveAnnotation == null) throw new IllegalArgumentException("no perspective annotation found");
        final String id = perspectiveAnnotation.id();
        if (id == null) throw new IllegalArgumentException("no perspective id set");
        initContext(InternalContext.class.cast(perspective.getContext()), parentId, id, perspectiveAnnotation.active());
        LOGGER.finest("register perspective with annotations : "
                + perspectiveAnnotation.id());
        initDeclarativePerspectiveParts(perspective, perspectiveAnnotation);
        initLocaleAttributes(perspective, perspectiveAnnotation);
        initResourceBundleAttributes(perspective, perspectiveAnnotation);
    }


    /**
     * Set all resource bundle attributes.
     *
     * @param perspective,           the perspective instance
     * @param perspectiveAnnotation, the @Perspective annotation
     */
    private static void initResourceBundleAttributes(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final String resourceBundleLocation = perspectiveAnnotation
                .resourceBundleLocation();
        if (resourceBundleLocation.length() > 1)
            perspective.setResourceBundleLocation(resourceBundleLocation);
    }

    /**
     * Set locale attributes.
     *
     * @param perspective            , the perspective instance
     * @param perspectiveAnnotation, the @Perspective annotation
     */
    private static void initLocaleAttributes(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final String localeID = perspectiveAnnotation.localeID();
        if (localeID.length() > 1)
            perspective.setLocaleID(localeID);
    }

    /**
     * Set all metadata for a declarative perspective
     *
     * @param perspective               , the perspective instance
     * @param perspectiveAnnotation,the @Perspective annotation
     */
    private static void initDeclarativePerspectiveParts(final Perspective<Node, EventHandler<Event>, Event, Object> perspective, final org.jacpfx.api.annotations.perspective.Perspective perspectiveAnnotation) {
        final String viewLocation = perspectiveAnnotation.viewLocation();
        if (viewLocation.length() > 1) {
            perspective.setViewLocation(perspectiveAnnotation.viewLocation());
            perspective.setUIType(UIType.DECLARATIVE);
        }
    }

    /**
     * Create context object instance.
     *
     * @param contextInterface, the context instance
     * @param parentId,         the parent id
     * @param id,               the component id
     * @param active,           the active state
     */
    private static void initContext(final InternalContext contextInterface, final String parentId, final String id, final boolean active) {
        contextInterface.setParentId(parentId);
        contextInterface.setId(id);
        contextInterface.updateActiveState(active);
    }
}
