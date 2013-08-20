package org.jacp.javafx.rcp.perspective;

import org.jacp.api.component.Injectable;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 19.08.13
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class EmbeddedFXPerspective extends AFXPerspective {

    public  EmbeddedFXPerspective(Injectable perspective) {
                this.perspectiveHandler = perspective;
    }


}
