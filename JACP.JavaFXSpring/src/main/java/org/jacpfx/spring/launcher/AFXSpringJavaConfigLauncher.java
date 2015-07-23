/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AFXSpringJavaConfigLauncher.java]
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

import javafx.stage.Stage;
import org.jacpfx.api.annotations.workbench.Workbench;
import org.jacpfx.api.exceptions.AnnotationNotFoundException;
import org.jacpfx.api.exceptions.AttributeNotFoundException;
import org.jacpfx.api.exceptions.ComponentNotFoundException;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.rcp.workbench.EmbeddedFXWorkbench;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * JavaFX / Spring application launcher; This abstract class handles reference
 * to spring context and contains the JavaFX start method; Implement this
 * abstract class and add a main method to call the default JavaFX launch
 * ("Application.launch(args);") sequence.
 * Created by Andy Moncsek on 28.01.14.
 */
public abstract class AFXSpringJavaConfigLauncher extends ASpringLauncher {


    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws Exception {
        initExceptionHandler();
        scanPackegesAndInitRegestry();
        final Launcher<AnnotationConfigApplicationContext> launcher = new SpringJavaConfigLauncher(getConfigClasses());
        final Class<? extends FXWorkbench> workbenchHandler = getWorkbenchClass();
        if (workbenchHandler == null) throw new ComponentNotFoundException("no FXWorkbench class defined");
        initWorkbench(stage, launcher, workbenchHandler);
    }

    private void initWorkbench(final Stage stage, final Launcher<AnnotationConfigApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        if (workbenchHandler.isAnnotationPresent(Workbench.class)) {
            this.workbench = createWorkbench(launcher, workbenchHandler);
            workbench.init(launcher, stage);
            postInit(stage);
        } else {
            throw new AnnotationNotFoundException("no @Workbench annotation found on class");
        }
    }

    private EmbeddedFXWorkbench createWorkbench(final Launcher<AnnotationConfigApplicationContext> launcher, final Class<? extends FXWorkbench> workbenchHandler) {
        final Workbench annotation = workbenchHandler.getAnnotation(Workbench.class);
        final String id = annotation.id();
        if (id.isEmpty()) throw new AttributeNotFoundException("no workbench id found for: " + workbenchHandler);
        final FXWorkbench handler = launcher.registerAndGetBean(workbenchHandler, id, Scope.SINGLETON);
        return new EmbeddedFXWorkbench(handler,getWorkbenchDecorator());
    }


    protected abstract Class<?>[] getConfigClasses();


}
