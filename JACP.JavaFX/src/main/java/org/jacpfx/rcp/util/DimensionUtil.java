/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [DimensionUtil.java]
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

package org.jacpfx.rcp.util;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jacpfx.api.exceptions.InvalidInitialisationException;

/**
 * @author Patrick Symmangk
 *
 */
public class DimensionUtil {
    
    private SimpleDoubleProperty screenWidthProperty  = null;
    private SimpleDoubleProperty screenHeightProperty = null;
    private final Stage stage;
    private static DimensionUtil instance;
    
    private DimensionUtil(final Stage stage) {this.stage = stage;}

    public static synchronized void init(final Stage stage) {
        instance = new DimensionUtil(stage);
    }

    public static synchronized DimensionUtil getInstance(){
        if(instance == null)
        {
           throw new InvalidInitialisationException("init util before use");
        }
        return instance;
    }

    public ReadOnlyDoubleProperty getStageWidthProperty() {
        return stage.widthProperty();
    }

    public ReadOnlyDoubleProperty getStageHeightProperty() {
        return stage.heightProperty();
    }
    
    public SimpleDoubleProperty getScreenWidthProperty(){
        return screenWidthProperty == null ? initScreenWidthProperty() : screenWidthProperty; 
    }
    
    public SimpleDoubleProperty getScreenHeightProperty(){
        return screenHeightProperty == null ? initScreenHeightProperty() : screenHeightProperty; 
    }
    
    
    private SimpleDoubleProperty initScreenWidthProperty(){
        screenWidthProperty = new SimpleDoubleProperty(Screen.getPrimary().getBounds().getWidth());
        return screenWidthProperty;
    }
 
    private SimpleDoubleProperty initScreenHeightProperty(){
        screenHeightProperty = new SimpleDoubleProperty(Screen.getPrimary().getBounds().getHeight());
        return screenHeightProperty;
    }

}
