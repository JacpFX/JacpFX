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

package org.jacp.launcher;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.jacp.decorator.UnitTestWorkbenchDecorator;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.exceptions.AnnotationNotFoundException;
import org.jacpfx.api.exceptions.AttributeNotFoundException;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.handler.ErrorDialogHandler;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.rcp.components.workbench.WorkbenchDecorator;
import org.jacpfx.rcp.handler.DefaultErrorDialogHandler;
import org.jacpfx.rcp.handler.ExceptionHandler;
import org.jacpfx.rcp.registry.ClassRegistry;
import org.jacpfx.rcp.registry.ComponentRegistry;
import org.jacpfx.rcp.registry.PerspectiveRegistry;
import org.jacpfx.rcp.util.ClassFinder;
import org.jacpfx.rcp.workbench.AFXWorkbench;
import org.jacpfx.rcp.workbench.EmbeddedFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.jacpfx.spring.launcher.SpringXmlConfigLauncher;
import org.junit.After;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testfx.framework.junit.ApplicationTest;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * This Launcher is a TestFX Launcher for JacpFX
 */
public abstract class TestFXJacpFXSpringLauncher extends ApplicationTest {

    protected AFXWorkbench workbench;

    protected Stage stage;


    public AFXWorkbench getWorkbench() {
        return workbench;
    }

    protected abstract Class<? extends FXWorkbench> getWorkbenchClass();

    protected void scanPackegesAndInitRegestry() {
        final String[] packages = getBasePackages();
        if (packages == null)
            throw new InvalidParameterException("no  packes declared, declare all packages containing perspective and component");
        final ClassFinder finder = new ClassFinder();
        ClassRegistry.clearAllClasses();
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
     * @return an array of package names
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

    /**
     * Return an instance of your WorkbenchDecorator, which defines the basic layout structure with toolbars and main content
     *
     * @return returns an instance of a {@link org.jacpfx.rcp.components.workbench.WorkbenchDecorator}
     */
    protected WorkbenchDecorator getWorkbenchDecorator() {
        return new UnitTestWorkbenchDecorator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws Exception {
        stage = stage;
        initExceptionHandler();
        scanPackegesAndInitRegestry();
        final Launcher<ClassPathXmlApplicationContext> launcher = new SpringXmlConfigLauncher(getXmlConfig());
        final Class<? extends FXWorkbench> workbenchHandler = getWorkbenchClass();
        if (workbenchHandler == null) throw new ComponentNotFoundException("no FXWorkbench class defined");
        initWorkbench(stage, launcher, workbenchHandler);
    }



    private void initWorkbench(final Stage stage, final Launcher<ClassPathXmlApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        if (workbenchHandler.isAnnotationPresent(Workbench.class)) {
            workbench = createWorkbench(launcher, workbenchHandler);
            workbench.init(launcher, stage);
            postInit(stage);
        } else {
            throw new AnnotationNotFoundException("no @Workbench annotation found on class");
        }
    }

    private EmbeddedFXWorkbench createWorkbench(final Launcher<ClassPathXmlApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        final Workbench annotation = workbenchHandler.getAnnotation(Workbench.class);
        final String id = annotation.id();
        if (id.isEmpty()) throw new AttributeNotFoundException("no workbench id found for: " + workbenchHandler);
        final FXWorkbench handler = launcher.registerAndGetBean(workbenchHandler, id, Scope.SINGLETON);
        return new EmbeddedFXWorkbench(handler,getWorkbenchDecorator());
    }

    public abstract String getXmlConfig();

    @After
    public void after()
            throws Exception {
        //ShutdownThreadsHandler.shutdowAll();
        //TearDownHandler.handleGlobalTearDown();

        cleanup();
        internalAfter();

    }

    protected void cleanup(){
        ComponentRegistry.clearOnShutdown();
        PerspectiveRegistry.clearOnShutdown();
    }
}
