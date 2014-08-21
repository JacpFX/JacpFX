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

package org.jacpfx.minimal.launcher;

import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;

/**
 * Created by amo on 21.08.14.
 */
public class JacpFXLauncher implements Launcher<ApplicationContext> {
    @Override
    public ApplicationContext getContext() {
        return null;
    }

    @Override
    public <P> P getBean(Class<P> clazz) {
        return null;
    }

    @Override
    public <P> P getBean(String qualifier) {
        return null;
    }

    @Override
    public <P> P registerAndGetBean(Class<? extends P> type, String id, Scope scope) {
        return null;
    }
}
