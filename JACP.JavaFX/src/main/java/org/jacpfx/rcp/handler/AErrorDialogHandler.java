/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AErrorDialogHandler.java]
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

import javafx.application.Platform;
import javafx.scene.Node;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;

/**
 * Created by Andy Moncsek on 08.01.14.
 * the abstract ErrorDialog, handles the Node creation in concrete ErrorDialogs
 */
public abstract class AErrorDialogHandler implements ErrorDialogHandler<Node> {
    @Override
    public void  handleExceptionInDialog(Throwable e) {
        final Node dialog = createExceptionDialog(e);
        if(dialog!=null) showModalDialog(dialog);
    }

    protected void showModalDialog(final Node node) {
        if(Platform.isFxApplicationThread()) {
            JACPModalDialog.getInstance().showModalDialog(node);
        }else {
            Platform.runLater(()->JACPModalDialog.getInstance().showModalDialog(node));
        }

    }
}
