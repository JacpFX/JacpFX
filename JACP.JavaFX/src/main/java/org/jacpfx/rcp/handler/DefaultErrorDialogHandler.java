/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [DefaultErrorDialogHandler.java]
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
import org.jacpfx.rcp.components.errorDialog.DefaultErrorDialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Andy Moncsek on 09.01.14.
 * Creates the default error dialog view.
 */
public class DefaultErrorDialogHandler extends AErrorDialogHandler {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Override
    public Node createExceptionDialog(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.log(Level.FINEST,sw.toString());
        return new DefaultErrorDialog("Error Pane",sw.toString());
    }
}
