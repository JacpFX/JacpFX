/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [IDelegateDTO.java]
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
package org.jacpfx.api.message;

/**
 * DTO interface to transfer messages to desired target in different perspective.
 *
 * @param <A> defines the basic message type
 * @param <M> defines the basic message type
 * @author Andy Moncsek
 */
public interface IDelegateDTO<A, M> {

    /**
     * Get the target id to transfer to.
     *
     * @return targetId
     */
    String getTarget();

    /**
     * Returns the message.
     *
     * @return the message
     */
    Message<A, M> getMessage();

    /**
     * returns true when message target is a perspective.
     *
     * @return true when target is perspective.
     */
    boolean isPerspective();
}
