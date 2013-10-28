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
package org.jacp.javafx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IPerspective;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.util.FXUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Global registry with references to all components.
 *
 * @author Andy Moncsek
 *
 */
public class ComponentRegistry {
	private static volatile List<ISubComponent<EventHandler<Event>, Event, Object>> components = new ArrayList<>();
	private static volatile StampedLock lock = new StampedLock();
	/**
	 * Registers a component.
	 *
	 * @param component
	 */
	public static void registerComponent(
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final long stamp = lock.tryWriteLock();
		try{
			if (!components.contains(component))
				components.add(component);
		}finally{
            lock.unlockWrite(stamp);
		}

	}

	/**
	 * Removes component from registry.
	 *
	 * @param component
	 */
	public static void removeComponent(
			final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final long stamp = lock.tryWriteLock();
		try{
			if (components.contains(component))
				components.remove(component);
		}finally{
            lock.unlockWrite(stamp);
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

        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            final List<ISubComponent<EventHandler<Event>, Event, Object>> c = components;
            if (lock.validate(stamp)) {
                return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                        c);
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                    components);
        } finally {
            lock.unlockRead(stamp);
        }
	}
	/**
	 * Returns the a component by class.
	 * @param clazz
	 * @return
	 */
	public static ISubComponent<EventHandler<Event>, Event, Object> findComponentByClass(final Class<?> clazz) {
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            final List<ISubComponent<EventHandler<Event>, Event, Object>> comp = components;
            if (lock.validate(stamp)) {
                final Optional<ISubComponent<EventHandler<Event>, Event, Object>> returnVal = comp.parallelStream().filter(c -> c.getComponentHandle().getClass().isAssignableFrom(clazz)).findFirst();
                if(returnVal.isPresent())return returnVal.get();

                return null;
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            final Optional<ISubComponent<EventHandler<Event>, Event, Object>> returnVal = components.parallelStream().filter(c -> c.getComponentHandle().getClass().isAssignableFrom(clazz)).findFirst();
            if(returnVal.isPresent())return returnVal.get();

            return null;
        } finally {
            lock.unlockRead(stamp);
        }
	}

}
