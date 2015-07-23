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

package org.jacp.test.dialogs;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.perspectives.PerspectiveIds;
import org.jacp.test.workbench.WorkbenchManagedFragmentMessage;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.fragment.Fragment;
import org.jacpfx.api.fragment.Scope;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.context.Context;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by Andy Moncsek on 06.01.14.
 */
@Fragment(id = "id1002", resourceBundleLocation = "bundles.languageBundle", localeID = "en_US", scope = Scope.SINGLETON)
public class DialogManagedFragmentMessageTest extends GridPane {

    @Resource
    private static Context context;
    @Resource
    private static FXComponent parent;


    @Resource
    private static ResourceBundle bundle;

    public void init() {
        VBox box = new VBox();
        Button b = new Button("send");
        TextField field = new TextField();
        b.setOnAction((event) ->
        {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            //dialog.initOwner(primaryStage);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("This is a Dialog"));
            Scene dialogScene = new Scene(dialogVbox, 300, 200);
            dialog.setScene(dialogScene);
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(WorkbenchManagedFragmentMessage.stage);
            System.out.println(selectedFile);
            context.send(PerspectiveIds.PerspectiveManagedFragmentMessage2+"."+ ComponentIds.CallbackComponentManagedFragmentMessage,selectedFile.getAbsolutePath());
        });
        box.getChildren().addAll(b,field);
        getChildren().add(box);
        setStyle("-fx-background-color: #0066FF");
        setPrefHeight(90);
        setMaxHeight(90);
        setMaxWidth(90);
        setPrefWidth(90);

    }
}
