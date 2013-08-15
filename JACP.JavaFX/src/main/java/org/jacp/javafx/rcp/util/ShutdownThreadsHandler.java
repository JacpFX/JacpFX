/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [ShutdownThreadsHandler.java]
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
package org.jacp.javafx.rcp.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Util class to register all Thread and executors in JACPFX, needed to shutdown all Threads and Executors on application close.
 * @author Andy Moncsek
 *
 */
public final class ShutdownThreadsHandler{
	private static final List<Thread> registeredThreads = new CopyOnWriteArrayList<>();
	private static final List<ExecutorService> registeredExecutors = new CopyOnWriteArrayList<>();
	private static final Logger logger = Logger.getLogger("ShutdownThreadsHandler");
	public static volatile AtomicBoolean APPLICATION_RUNNING = new AtomicBoolean(true);
	public static final Long WAIT = 1500L;
	/**
	 * Register a Thread.
	 * @param t
	 */
	public static <T extends Thread> void registerThread(T t) {
		registeredThreads.add(t);
	}
	/**
	 * Register an Executor service.
	 * @param t
	 */
	public static <E extends ExecutorService> void registerexecutor(E t) {
		registeredExecutors.add(t);
	}
	/**
	 * Shutdown all registered Threads.
	 */
	public static void shutdownThreads() {
		APPLICATION_RUNNING.set(false);
		for(final Thread t:registeredThreads) {
			logger.info("shutdown thread: "+t);
			t.interrupt();
		}
	}
	/**
	 * Shutdown all registered Executors.
	 */
	public static void shutDownExecutors() {
		for(final ExecutorService e: registeredExecutors) {
			e.shutdown();
		}
	}
	
	/**
	 * Shutdown registered Threads and Executors.
	 */
	public static void shutdowAll() {
		APPLICATION_RUNNING.set(false);
		for(final Thread t:registeredThreads) {
			logger.info("shutdown thread: "+t);
			t.interrupt();
		}
		for(final ExecutorService e: registeredExecutors) {
			e.shutdown();
		}
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		// force to interrupt all threads in waiting condition
		for(final Thread t: threadSet) {
			if(t.getName().contains(HandlerThreadFactory.PREFIX) && !t.isInterrupted()) {
				t.interrupt();
			}
		}
		
	}
}
