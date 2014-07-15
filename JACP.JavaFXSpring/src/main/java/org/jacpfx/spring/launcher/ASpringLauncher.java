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

package org.jacpfx.spring.launcher;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.rcp.handler.DefaultErrorDialogHandler;
import org.jacpfx.rcp.handler.ExceptionHandler;
import org.jacpfx.rcp.registry.ClassRegistry;
import org.jacpfx.rcp.util.ClassFinder;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 03.02.14.
 * The abstract Spring launcher contains all methods common to both the Spring XML and the Spring avaConfig launcher.
 */
public abstract class ASpringLauncher extends Application{
    protected AFXWorkbench workbench;





    public AFXWorkbench getWorkbench() {
        return this.workbench;
    }

    protected abstract Class<? extends FXWorkbench> getWorkbenchClass();

    protected void scanPackegesAndInitRegestry() {
        final String[] packages = getBasePackages();
        if (packages == null)
            throw new InvalidParameterException("no  packes declared, declare all packages containing perspective and component");
        final ClassFinder finder = new ClassFinder();
        Stream.of(packages).forEach(p -> {
            try {
                ClassRegistry.addClasses(Arrays.asList(finder.getAll(p)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    protected void initExceptionHandler() {
        ExceptionHandler.initExceptionHandler(getErrorHandler());
        Thread.currentThread().setUncaughtExceptionHandler(ExceptionHandler.getInstance());

    }

    /**
     * Return all packages which contains component and perspective that should be scanned. This is needed to find component/prespectives by id.
     *
     * @return  an array of package names
     */
    protected abstract String[] getBasePackages();

    /**
     * Will be executed after Spring/JavaFX initialisation.
     *
     * @param stage the javafx Stage
     */
    protected abstract void postInit(Stage stage);

    /**
     * Returns an ErrorDialog handler to display exceptions and errors in workspace. Overwrite this method if you need a customized handler.
     *
     * @return an ErrorHandler instance
     */
    protected ErrorDialogHandler<Node> getErrorHandler() {
        return new DefaultErrorDialogHandler();
    }
}
