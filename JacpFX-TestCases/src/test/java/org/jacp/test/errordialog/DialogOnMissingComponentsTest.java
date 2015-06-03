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

package org.jacp.test.errordialog;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.stage.Stage;
import junit.framework.TestCase;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.workbench.WorkbenchMissingComponentInitialTargetId;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.rcp.handler.AErrorDialogHandler;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 24.04.15.
 */
public class DialogOnMissingComponentsTest extends TestFXJacpFXSpringLauncher {

    @Override
    public String getXmlConfig() {
        return "main.xml";
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchMissingComponentInitialTargetId.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }


    @Override
    public void postInit(final Stage stage) {

    }

    @Override
    protected ErrorDialogHandler<Node> getErrorHandler() {
        return new CustomErrorDialogHandler();
    }

    @Test
    public void failedToStartMissingTargetId() throws Exception {





    }

    public class CustomErrorDialogHandler extends AErrorDialogHandler {
        public CountDownLatch latch = new CountDownLatch(1);
        @Override
        public Node createExceptionDialog(Throwable e) {
            System.out.println("ERROR "+e.getMessage());
            //
            TestCase.assertTrue(e.getMessage().contains("no targetLayout for layoutID:"));

            return null;
        }
    }

}
