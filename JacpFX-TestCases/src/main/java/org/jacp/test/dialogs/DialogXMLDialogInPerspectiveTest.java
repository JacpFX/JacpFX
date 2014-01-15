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

package org.jacp.test.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jacp.test.perspectives.PerspectiveDialogInPerspectiveTest;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.fragment.Fragment;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.perspective.FXPerspective;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

/**
 * Created by amo on 06.01.14.
 */
@Fragment(id = "id1002", viewLocation = "/fxml/DialogXMLDialogInPerspectiveTest.fxml", resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", scope = Scope.SINGLETON)
public class DialogXMLDialogInPerspectiveTest {
    @FXML
    private GridPane root;
    public static CountDownLatch latch = new CountDownLatch(7);

    @Resource
    private static Context context;
    @Resource
    private static FXPerspective parent;

    @Resource
    private static PerspectiveDialogInPerspectiveTest parentImpl;

    @Resource
    private static ResourceBundle bundle;

    public void init() {
        root.getChildren().addAll(new Label("TEST 2"));
        latch.countDown();
        if (context != null && context.getId().equals(PerspectiveIds.PerspectiveDialogInPerspectiveTest)) {
            latch.countDown();
        }
        if (bundle != null) {
            latch.countDown();
        }
        if (parent != null) {
            latch.countDown();
        }
        if (parentImpl != null) {
            latch.countDown();
        }
    }
}
