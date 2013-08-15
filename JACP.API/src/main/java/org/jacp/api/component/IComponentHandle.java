package org.jacp.api.component;

import org.jacp.api.action.IAction;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 25.06.13
 * Time: 16:13
 * This Interface declares a minimal component interface
 */
public interface IComponentHandle<C, L, A, M> extends Injectable{
    /**
     * Handles component when called. The handle method in sub components is
     * always executed in a separate thread;
     *
     * @param action , the triggering action
     * @return view component
     */
    C handle(final IAction<A, M> action) throws Exception;
}
