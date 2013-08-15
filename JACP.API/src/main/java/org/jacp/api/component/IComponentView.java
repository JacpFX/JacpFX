/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IVComponent.java]
 * AHCP Project (http://jacp.googlecode.com)
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
package org.jacp.api.component;

import org.jacp.api.action.IAction;

/**
 * Represents an UI component handled by a perspective. A IVComponent is an // *
 * visible UI component displayed in a defined area of perspective.
 * 
 * @author Andy Moncsek
 * @param <C>
 *            defines the base component where others extend from
 * @param <L>
 *            defines the action listener type
 * @param <A>
 *            defines the basic action type
 * @param <M>
 *            defines the basic message type
 */
public interface IComponentView<C, L, A, M>  extends IComponentHandle<C, L, A, M> {

	/**
	 * To avoid toolkit specific threading issues the postHandle method always
	 * called after the handle method. While the handle method is executed in a
	 * separate thread the postHandle method is guaranteed to run in application
	 * main thread. It is mostly save to create new components outside the main
	 * thread in the handle method but when you like to recycle your components
	 * you should use the postHandle method. In the postHandle method you should
	 * avoid long running tasks. Use it only to create or update your ui
	 * components.
	 * 
	 * @param node , the ui node
	 * @param action ,  the trigger action
	 * @return an ui component
	 */
	C postHandle(final C node, final IAction<A, M> action) throws Exception;

}
