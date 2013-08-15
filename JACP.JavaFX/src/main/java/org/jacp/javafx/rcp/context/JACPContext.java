package org.jacp.javafx.rcp.context;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.context.Context;
import org.jacp.javafx.rcp.components.managedDialog.ManagedDialogHandler;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 02.07.13
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
public interface JACPContext extends Context<EventHandler<Event>, Event, Object> {

    /**
     * Returns a managed dialog, which contains part of an UI and can fully interact with CDI and it's parent component.
     * @param clazz
     * @param <T>
     * @return
     */
    <T> ManagedDialogHandler<T> getManagedDialogHandler(final Class<T> clazz);

    /**
     * shows the passed Node as modal dialog
     * @param node
     */
    void showModalDialog(final Node node);

    /**
     * hides the current modal dialog
     */
    void hideModalDialog();
}
