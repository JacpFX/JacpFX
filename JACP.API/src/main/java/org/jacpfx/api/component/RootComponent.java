/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [RootComponent.java]
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
package org.jacpfx.api.component;

import org.jacpfx.api.handler.ComponentHandler;

/**
 * All root component have containing sub component (workspace -
 * perspective; perspective - editors) and listeners; all sub component have
 * to be initialized, registered and handled
 *
 * @param <T> component type to register
 * @param <A> message type to use in registration process
 * @author Andy Moncsek
 */
public interface RootComponent<T, A> {

    /**
     * Register the component at the listener.
     *
     * @param component, the component to register.
     */
    void registerComponent(final T component);

    /**
     * Add a component, this does not fully register the component.
     * If you want to add a newly created component use registerComponent instead.
     *
     * @param component, the component to add.
     */
    void addComponent(final T component);



    /**
     * Handles initialization of subcomponents.
     *
     * @param message, the initial message
     */
    void initComponents(final A message);

    /**
     * Returns component handler to handle initialization and reassignment of
     * subcomponents.
     *
     * @return the component handler
     */
    ComponentHandler<T, A> getComponentHandler();

}
