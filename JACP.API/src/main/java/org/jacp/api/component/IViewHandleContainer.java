package org.jacp.api.component;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 26.06.13
 * Time: 23:33
 * To change this template use File | Settings | File Templates.
 */
public interface IViewHandleContainer<C, L, A, M> {

    void setComponentHandle(final IComponentView<C, L, A, M>  handle);
}