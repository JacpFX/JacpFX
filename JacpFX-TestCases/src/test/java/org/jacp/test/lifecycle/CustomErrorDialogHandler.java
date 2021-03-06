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

package org.jacp.test.lifecycle;

import javafx.scene.Node;
import org.jacpfx.rcp.handler.AErrorDialogHandler;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 10.01.14.
 */
public class CustomErrorDialogHandler extends AErrorDialogHandler {
    public static CountDownLatch exceptionLatch= new CountDownLatch(1);

    @Override
    public Node createExceptionDialog(Throwable e) {
        e.printStackTrace();
        exceptionLatch.countDown();
        return null;
    }
}