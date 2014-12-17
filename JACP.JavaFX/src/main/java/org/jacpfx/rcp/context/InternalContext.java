/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2014
 *
 *  [Component.java]
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

package org.jacpfx.rcp.context;

import org.jacpfx.rcp.componentLayout.FXComponentLayout;

import java.util.ResourceBundle;

/**
 * The internal context provides all setter methods on a context which is only for internal usage
 * Created by Andy Moncsek on 17.12.14.
 */
public interface InternalContext {

    /**
     * Returns the perspective in which the component should executed in.
     * @return a perspectiveId
     */
    String getExecutionTarget();


    /**
     * Returns the retrun target and clears the value
     * @return an component id
     */
    String getReturnTargetAndClear();

    /**
     * Set the FXComponentLayout
     * @param layout
     */
    void setFXComponentLayout(final FXComponentLayout layout);

    /**
     * Set the resource bundle
     * @param resourceBundle
     */
    void setResourceBundle(ResourceBundle resourceBundle);


    /**
     * Set the components name
     * @param name
     */
    void setName(final String name);

    /**
     * Set the parent id
     * @param parentId
     */
    void setParentId(final String parentId);


    /**
     * Set the component id
     * @param id
     */
    void setId(final String id);

    /**
     * Set component targetId which is the target of a background component return
     * value; the return value will be handled like an average message and will
     * be delivered to targeted component.
     *
     * @param componentTargetId ; represents a component id to return the value to
     */
    void updateReturnTarget(final String componentTargetId) throws IllegalStateException;

    /**
     * Set default active state of component.
     *
     * @param active ; the component active state.
     */
    void updateActiveState(boolean active);

    /**
     * Defines the perspective in which the component should executed in.
     *
     * @param id, the id of the parent perspective where the component should be executed in.
     */
    void updateExecutionTarget(final String id) throws IllegalStateException;

    /**
     * Returns the target layout in parent perspective
     * @return a target layout id
     */
    String getTargetLayout();
}
