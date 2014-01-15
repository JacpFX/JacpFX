/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [ILayoutAbleComponent.java]
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
package org.jacpfx.api.component;

import org.jacpfx.api.componentLayout.PerspectiveLayoutInterface;

/**
 * This interface defines perspective components with visible ui.
 *
 * @param <C> defines the base component where others extend from
 * @param <L> defines the message listener type
 * @param <A> defines the basic event type
 * @param <M> defines the basic message type
 * @author Andy Moncsek
 */
public interface PerspectiveView<C, L, A, M> extends Perspective<L, A, M>, Declarative {
    /**
     * Returns layout dto.
     *
     * @return an PerspectiveLayoutInterface instance to define basic layout stuff for
     * perspective
     */
    PerspectiveLayoutInterface<C, C> getIPerspectiveLayout();


    /**
     * Set the default perspective layout entity for the perspective.
     *
     * @param layout, The layout dto
     */
    void setIPerspectiveLayout(PerspectiveLayoutInterface<C, C> layout);


}
