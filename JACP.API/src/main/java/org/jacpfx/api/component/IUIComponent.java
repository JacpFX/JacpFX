/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [IVComponent.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacpfx.api.component;

/**
 * Represents an basic UI component handled by a perspective. A IUIComponent is
 * an // * visible UI component displayed in a defined area of perspective.
 * 
 * @author Andy Moncsek
 * @param <C>
 *            defines the base component where others extend from
 * @param <L>
 *            defines the message listener type
 * @param <A>
 *            defines the basic event type
 * @param <M>
 *            defines the basic message type
 */
public interface IUIComponent<C, L, A, M> extends ISubComponent<L, A, M> {

	/**
	 * Returns the 'root' ui component created by the handle method.
	 * 
	 * @return the root component
	 */
	C getRoot();


    /**
     * Set the 'root' ui component created by the handle method.
     * @param root, the UI root
     */
    void setRoot(C root);

    /**
     * Returns the component handle class, this is the users implementation of the component.
     * @param <X>, X extends an IComponentView
     * @return IComponentHandle, the component handle.
     */
    public default <X extends IComponentView<C, A, M>> X  getComponentViewHandle(){
        //noinspection unchecked
        return (X) this.getComponent();
    }

}
