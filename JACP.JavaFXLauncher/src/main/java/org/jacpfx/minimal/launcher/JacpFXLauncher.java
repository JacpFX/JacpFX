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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by amo on 21.08.14.
 */
public class JacpFXLauncher implements Launcher<ApplicationContext> {

    private Map<String, Object> singletons = new ConcurrentHashMap<>();


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
        return (P) singletons.get(qualifier);
    }

    private boolean contains(final String qualifier) {
       return singletons.containsKey(qualifier);
    }

    @Override
    public <P> P registerAndGetBean(Class<? extends P> type, String qualifier, Scope scope) {
        if (contains(qualifier))
            return getBean(qualifier);
        try {
            final Object instance = type.newInstance();
            if(scope.equals(Scope.SINGLETON)) {
                singletons.put(qualifier,instance);
            }
            return (P) instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return null;
    }
}
