package org.jacpfx.rcp.component;

import org.jacpfx.api.component.ComponentHandle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 23.07.13
 * Time: 11:57
 * This is an implementation of an AStatelessCallbackComponent which will be used to encapsulate handles on application startup.
 */
public class EmbeddedStatelessCallbackComponent extends AStatelessCallbackComponent{

    public EmbeddedStatelessCallbackComponent(ComponentHandle handle) {
        super();
        this.setComponent(handle);
    }

    @Override
    public String toString() {
        return this.getContext() != null ? this.getContext().getId() : this.getComponent().toString();
    }

}
