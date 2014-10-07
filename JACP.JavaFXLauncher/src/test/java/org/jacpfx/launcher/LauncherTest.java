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

package org.jacpfx.launcher;

import org.jacpfx.api.fragment.Scope;
import org.jacpfx.launcher.components.TestComponent;
import org.jacpfx.minimal.launcher.JacpFXLauncher;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by amo on 21.08.14.
 */
public class LauncherTest {
      private static JacpFXLauncher launcher;

    @BeforeClass
    public static void initLauncher() {
        launcher = new JacpFXLauncher();
    }

    @Test
    public void checkSingleton() {
        TestComponent c1 = launcher.registerAndGetBean(TestComponent.class, "i23",Scope.SINGLETON);
        Assert.assertNotNull(c1);
        TestComponent c2 = launcher.registerAndGetBean(TestComponent.class, "i23",Scope.SINGLETON);
        Assert.assertNotNull(c2);
        Assert.assertTrue(c1==c2);
        TestComponent c3 = launcher.registerAndGetBean(TestComponent.class, "i23",Scope.SINGLETON);
        Assert.assertNotNull(c3);
        Assert.assertTrue(c1==c3);
    }


    @Test
    public void checkPrototype() {
        TestComponent c1 = launcher.registerAndGetBean(TestComponent.class, "234",Scope.PROTOTYPE);
        Assert.assertNotNull(c1);
        TestComponent c2 = launcher.registerAndGetBean(TestComponent.class, "234",Scope.PROTOTYPE);
        Assert.assertNotNull(c2);
        Assert.assertTrue(c1!=c2);
        TestComponent c3 = launcher.registerAndGetBean(TestComponent.class, "234",Scope.PROTOTYPE);
        Assert.assertNotNull(c3);
        Assert.assertTrue(c1!=c3);
    }
}
