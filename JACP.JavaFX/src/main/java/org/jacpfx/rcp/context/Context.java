package org.jacpfx.rcp.context;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.context.JacpContext;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.components.managedFragment.ManagedFragmentHandler;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 02.07.13
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
public interface Context extends JacpContext<EventHandler<Event>, Object> {

    /**
     * Returns a managed fragment handler, which contains part of an UI and can fully interact with CDI and it's parent component.
     * @param clazz the class of the requested managed fragment
     * @param <T> the type of the managed fragment
     * @return a managedFragmentHandler which gives access to the fragments controller and UI node
     */
    <T> ManagedFragmentHandler<T> getManagedFragmentHandler(final Class<T> clazz);

    /**
     * shows the passed Node as modal dialog
     * @param node the Node to show in dialog
     */
    void showModalDialog(final Node node);

    /**
     * hides the current modal dialog
     */
    void hideModalDialog();

    /**
     * Retruns the component layout
     * @return The ComponentLayout handler
     */
    FXComponentLayout getComponentLayout();

    /**
     * Invoke a Runnable on FXApplication Thread and wait until it is finished (Platform.runLater won't wait)
     * @param r, The Runnable
     */
    void invokeFXAndWait(final Runnable r);
}
