
/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ComponentHandle.java]
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

import org.jacpfx.api.message.Message;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 25.06.13
 * Time: 16:13
 * This Interface declares a minimal component interface. This interface should be implemented or extended to declare a JacpFX component.
 *
 * @param <C> defines the base component where others extend from
 * @param <A> defines the basic event type
 * @param <M> defines the basic message type
 */
public interface ComponentHandle<C, A, M> extends Injectable {
    /**
     * Handles component when called. The handle method in sub component is
     * always executed in a separate thread;
     *
     * @param message , the triggering message
     * @return view component
     * @throws java.lang.Exception , the method will be implemented by JacpFX developer an can throw any exception
     */
    default C handle(final Message<A, M> message) throws Exception{
        return null;
    }
}
