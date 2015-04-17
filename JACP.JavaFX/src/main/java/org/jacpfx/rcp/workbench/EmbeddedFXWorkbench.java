package org.jacpfx.rcp.workbench;

import org.jacpfx.rcp.components.workbench.WorkbenchDecorator;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 30.08.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class EmbeddedFXWorkbench extends AFXWorkbench {

    public EmbeddedFXWorkbench(final FXWorkbench handler, final WorkbenchDecorator decorator){
        this.setComponentHandle(handler);
        this.setWorkbenchDecorator(decorator);
    }

}
