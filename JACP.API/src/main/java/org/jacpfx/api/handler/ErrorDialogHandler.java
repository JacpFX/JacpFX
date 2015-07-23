/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ErrorDialogHandler.java]
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

package org.jacpfx.api.handler;

/**
 * Created by Andy Moncsek on 08.01.14.
 * This interface allows to define an Error Fragment which is called when a exception is thrown.
 * @param <N>, an UI root node which represents a dialog
 */
public interface ErrorDialogHandler<N> {


    /**
     * handle an thrown exception and return an error dialog.
     * @param e, The throwable
     */
    void handleExceptionInDialog(Throwable e);

    /**
     * Create an exception dialog
     * @param e, The throwable
     * @return The Fragment
     */
    N createExceptionDialog(Throwable e);
}
