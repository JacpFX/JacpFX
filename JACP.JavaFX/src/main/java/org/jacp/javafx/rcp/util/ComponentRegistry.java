/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
 *
 * [CSSUtil.java]
 * AHCP Project http://jacp.googlecode.com
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
package org.jacp.javafx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.ISubComponent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Global registry with references to all components.
 * 
 * @author Andy Moncsek
 * 
 */
public class ComponentRegistry {
	private static volatile List<ISubComponent<EventHandler<Event>, Event, Object>> components = new CopyOnWriteArrayList<>();
	private static volatile ReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * Registers a component.
	 * 
	 * @param component
	 */
	public static void registerComponent(
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		lock.writeLock().lock();
		try{
			if (!components.contains(component))
				components.add(component);
		}finally{
			lock.writeLock().unlock();
		}

	}

	/**
	 * Removes component from registry.
	 * 
	 * @param component
	 */
	public static void removeComponent(
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
		lock.writeLock().lock();
		try{
			if (components.contains(component))
				components.remove(component);
		}finally{
			lock.writeLock().unlock();
		}		

	}

	/**
	 * Returns a component by component id
	 * 
	 * @param targetId
	 * @return
	 */
	public static ISubComponent<EventHandler<Event>, Event, Object> findComponentById(
			final String targetId) {
		lock.readLock().lock();
		try{
			return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
					components);
		}finally{
			lock.readLock().unlock();
		}		
		
	}
	/**
	 * Returns the a component by class.
	 * @param clazz
	 * @return
	 */
	public static ISubComponent<EventHandler<Event>, Event, Object> findComponentByClass(final Class<?> clazz) {
		lock.readLock().lock();
		try{
            final Optional<ISubComponent<EventHandler<Event>, Event, Object>> returnVal = components.parallelStream().filter(c -> c.getComponentHandle().getClass().isAssignableFrom(clazz)).findFirst();
			if(returnVal.isPresent())return returnVal.get();

			return null;
		}finally{
			lock.readLock().unlock();
		}		
	}

}
