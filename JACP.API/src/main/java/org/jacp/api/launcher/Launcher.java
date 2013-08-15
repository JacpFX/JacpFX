/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
 *
 * [Launcher.java]
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
package org.jacp.api.launcher;

import org.jacp.api.dialog.Scope;

/**
 * Defines an interface for launchers witch is an abstraction used DI containers
 * 
 * @author Andy Moncsek
 * 
 * @param <E>
 */
public interface Launcher<E> {
	/**
	 * Returns the DI container context.
	 * 
	 * @return the DI contect object
	 */
	E getContext();

	/**
	 * Returns a bean by class name.
	 * 
	 * @param clazz
	 * @return the bean
	 */
	<P> P getBean(final Class<P> clazz);

	/**
	 * Registers a Class in context and returns a managed bean.
	 * 
	 * @param type
	 * @return
	 */
	<T> T registerAndGetBean(final Class<? extends T> type, final String id, final Scope scope);
}
