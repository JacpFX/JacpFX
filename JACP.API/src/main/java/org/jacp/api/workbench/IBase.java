/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [IBase.java]
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
package org.jacp.api.workbench;

import org.jacp.api.component.IPerspective;
import org.jacp.api.launcher.Launcher;

import java.util.List;

/**
 * 
 * This Interface defines the basic root construct of an AHCP application, it
 * has no dependencies to any UI
 * 
 * @param <L>
 *            defines the action listener type
 * @param <A>
 *            defines the basic action type
 * @param <M>
 *            defines the basic message type
 * @author Andy Moncsek
 * 
 */
public interface IBase<L, A, M> {

	/**
	 * Set perspectives to workbench.
	 * 
	 * @param perspectives
	 */
	void setPerspectives(final List<IPerspective<L, A, M>> perspectives);

	/**
	 * Get perspectives in workbench.
	 * 
	 * @return a list of all perspectives
	 */
	List<IPerspective<L, A, M>> getPerspectives();

	/**
	 * Initialization sequence returns basic container to handle perspectives.
	 * 
	 * @param launcher
	 *            for di container
	 */
	void init(final Launcher<?> launcher);

}
