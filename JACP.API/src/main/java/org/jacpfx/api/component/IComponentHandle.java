package org.jacpfx.api.component;

import org.jacpfx.api.message.Message;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 25.06.13
 * Time: 16:13
 * This Interface declares a minimal component interface
 */
public interface IComponentHandle<C, A, M> extends Injectable{
    /**
     * Handles component when called. The handle method in sub components is
     * always executed in a separate thread;
     *
     * @param message , the triggering message
     * @return view component
     */
    C handle(final Message<A, M> message) throws Exception;
}
