package org.jacp.javafx.rcp.component;

import org.jacp.api.component.IComponentHandle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 23.07.13
 * Time: 11:57
 * This is an implementation of an AStatelessCallbackComponent which will be used to encapsulate handles on application startup.
 */
public class EmbeddedStatelessCallbackComponent extends AStatelessCallbackComponent{
    public EmbeddedStatelessCallbackComponent() {
        super();
    }

    public EmbeddedStatelessCallbackComponent(IComponentHandle handle) {
        super();
        this.setComponentHandle(handle);
    }
}
