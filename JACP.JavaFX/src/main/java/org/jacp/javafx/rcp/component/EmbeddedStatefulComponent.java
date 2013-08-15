package org.jacp.javafx.rcp.component;

import org.jacp.api.component.IComponentHandle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 17.07.13
 * Time: 10:02
 * This is an implementation of an AStatefulCallbackComponent which will be used to encapsulate handles on application startup.
 */
public class EmbeddedStatefulComponent extends ASubComponent{
    public EmbeddedStatefulComponent() {

    }

    public EmbeddedStatefulComponent(IComponentHandle handle) {
        this.setComponentHandle(handle);
    }
}
