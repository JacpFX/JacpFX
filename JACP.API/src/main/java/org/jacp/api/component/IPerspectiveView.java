/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ILayoutAbleComponent.java]
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

import org.jacp.api.componentLayout.IPerspectiveLayout;

/**
 * This interface defines perspective components with visible ui.
 * 
 * @author Andy Moncsek
 * 
 * @param <C>
 *            defines the base component where others extend from
 * @param <L>
 *            defines the action listener type
 * @param <A>
 *            defines the basic action type
 * @param <M>
 *            defines the basic message type
 */
public interface IPerspectiveView<C, L, A, M> extends IPerspective<L, A, M>, IDeclarative {
	/**
	 * Returns layout dto.
	 * 
	 * @return an IPerspectiveLayout instance to define basic layout stuff for
	 *         perspective
	 */
	IPerspectiveLayout<? extends C, C> getIPerspectiveLayout();
	
	
}
