/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
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

package org.jacp.test;

import org.jacp.test.component.CheckComponentsTest;
import org.jacp.test.messaging.*;
import org.jacp.test.perspective.CheckPerspectiveTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Andy Moncsek on 03.06.15.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CheckPerspectiveTest.class,
        CheckComponentsTest.class, PerspectiveMessagingTest.class, FXComponentMessagingTest2.class, FXComponentBaseMessagingTest.class,
        FXComponentBaseCallBackMessagingTest.class, FXComponentBaseAsyncCallbackMessagingTest.class,CallbackComponentToFXComponentBaseMessagingTest.class})
public class UITests {
        // ShutdownAndReopenComponentsTest3.class,
        //ShutdownAndReopenComponentsTest4.class, ShutdownAndReopenComponentsTest5.class, ShutdownAndReopenComponentsTest6.class,   OnShowOnHidePerspective.class,
        //
}
