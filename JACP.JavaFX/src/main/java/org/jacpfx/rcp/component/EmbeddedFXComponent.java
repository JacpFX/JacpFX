package org.jacpfx.rcp.component;

import org.jacpfx.api.component.IComponentView;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 02.07.13
 * Time: 21:30
 * This is an implementation of an AFXComponent which will be used to encapsulate handles on application startup.
 */
public class EmbeddedFXComponent extends AFXComponent{

    public EmbeddedFXComponent(IComponentView handle) {
        this.setComponent(handle);
    }

    @Override
    public String toString() {
        return this.getContext() != null ? this.getContext().getId() : this.getComponent().toString();
    }

}

