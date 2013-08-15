package org.jacp.javafx.rcp.util;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Screen;

/**
 * @author Patrick Symmangk
 *
 */
public class DimensionUtil {
    
    private SimpleDoubleProperty screenWidthProperty  = null;
    private SimpleDoubleProperty screenHeightProperty = null;
    
    private static DimensionUtil instance;
    
    private DimensionUtil() {}

    public DimensionUtil getInstance(){
        if(instance == null)
        {
           instance = new DimensionUtil();
        }
        return instance;
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
