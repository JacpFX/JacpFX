/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ComponentHandler.java]
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
package org.jacpfx.api.handler;

/**
 * A component handler handles initialization and reassignment of component
 *
 * @param <T> component type to register
 * @param <A> message type to use in registration process
 * @author Andy Moncsek
 */
public interface ComponentHandler<T, A> {
    /**
     * Handles initialization of a single component.
     *
     * @param message    , the initial message
     * @param component, the component which should be initiated
     */
    void initComponent(final A message, final T component);

    /**
     * Runs 'handle' method and replace of subcomponent in perspective.
     *
     * @param component, the component which should be handled
     * @param message,   the message which triggers the execution
     */
    void handleAndReplaceComponent(final A message, final T component);
}
