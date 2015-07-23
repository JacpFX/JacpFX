/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [EmbeddedFXPerspective.java]
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

package org.jacpfx.rcp.perspective;

import org.jacpfx.api.component.Injectable;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 19.08.13
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class EmbeddedFXPerspective extends AFXPerspective {

    public EmbeddedFXPerspective(final Injectable perspective) {
        this.perspective = perspective;
    }

    @Override
    public String toString() {
        return this.getContext() != null ? this.getContext().getId() : this.perspective.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
