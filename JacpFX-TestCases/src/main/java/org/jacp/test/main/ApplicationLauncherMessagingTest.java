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
package org.jacp.test.main;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jacp.test.config.BasicConfigVersion3;
import org.jacp.test.workbench.WorkbenchMessagingTest;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.rcp.handler.AErrorDialogHandler;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.jacpfx.spring.launcher.AFXSpringJavaConfigLauncher;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * The application launcher containing the main method
 *
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 */
public class ApplicationLauncherMessagingTest extends AFXSpringJavaConfigLauncher {
    private static final Logger log = Logger.getLogger(ApplicationLauncherMessagingTest.class
            .getName());
    public static final String[] STYLES = new String[2];
    private static final String[] STYLE_FILES = {"/styles/style_light.css", "/styles/style_dark.css"};
    /// binary style sheets created while deployment
    private static final String[] BINARY_FILES = {"/styles/style_light.bss", "/styles/style_dark.bss"};
    public static CountDownLatch latch = new CountDownLatch(6);
    public static volatile ApplicationLauncherMessagingTest[] instance = new ApplicationLauncherMessagingTest[1];
    public static AErrorDialogHandler exceptionhandler;

    public ApplicationLauncherMessagingTest() {
    }

    public ApplicationLauncherMessagingTest(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public Class<?>[] getConfigClasses() {
        return new Class<?>[]{BasicConfigVersion3.class};
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchMessagingTest.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    /**
     * only for testing
     */
    public void startComponentScaning() {
        this.scanPackegesAndInitRegestry();
    }

    @Override
    public void postInit(final Stage stage) {
        initStyles();
        stage.setMinHeight(580);
        stage.setMinWidth(800);
        final Scene scene = stage.getScene();
        stage.getIcons().add(new Image("images/icons/JACP_512_512.png"));
        // add style sheet
        scene.getStylesheets().add(STYLES[0]);
        instance[0] = this;
        ApplicationLauncherMessagingTest.latch.countDown();
    }

    private static void initStyles() {
        for (int i = 0; i < 2; i++) {
            URL res = ApplicationLauncherMessagingTest.class.getResource(BINARY_FILES[i]);
            if (res == null)
                res = ApplicationLauncherMessagingTest.class.getResource(STYLE_FILES[i]);
            STYLES[i] = res.toExternalForm();
            log.info("found: " + STYLES[i] + " stylesheet");
        }

    }

    /**
     * Returns an ErrorDialog handler to display exceptions and errors in workspace. Overwrite this method if you need a customized handler.
     *
     * @return
     */
    @Override
    protected ErrorDialogHandler<Node> getErrorHandler() {
        return exceptionhandler;
    }


}
