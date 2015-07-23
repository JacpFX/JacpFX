/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ExceptionHandler.java]
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

package org.jacpfx.rcp.handler;

import javafx.scene.Node;
import org.jacpfx.api.exceptions.InvalidInitialisationException;
import org.jacpfx.api.handler.ErrorDialogHandler;

/**
 * Created by Andy Moncsek on 08.01.14.
 * The ExceptionHandler catches all Exceptions in a running JacpFX application and delegates it to an ExceptionDialog.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private ErrorDialogHandler<Node> dialogHandler;

    private static ExceptionHandler handler;

    public ExceptionHandler(final ErrorDialogHandler<Node> dialogHandler) {
        this.dialogHandler = dialogHandler;
    }

    public static synchronized void initExceptionHandler(final ErrorDialogHandler<Node> dialogHandler) {
        handler = new ExceptionHandler(dialogHandler);
    }

    public static synchronized ExceptionHandler getInstance() {
        if (handler == null)
            throw new InvalidInitialisationException("you must call initExceptionHandler with a valid dialogHandler before ");
        return handler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        dialogHandler.handleExceptionInDialog(e);

    }
}
