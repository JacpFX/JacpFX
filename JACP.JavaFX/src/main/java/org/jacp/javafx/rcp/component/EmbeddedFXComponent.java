package org.jacp.javafx.rcp.component;

import org.jacp.api.component.IComponentView;

import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 02.07.13
 * Time: 21:30
 * This is an implementation of an AFXComponent which will be used to encapsulate handles on application startup.
 */
public class EmbeddedFXComponent extends AFXComponent{
    public EmbeddedFXComponent() {

    }

    public EmbeddedFXComponent(IComponentView handle) {
        this.setComponentHandle(handle);
    }


}

