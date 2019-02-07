/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ShutdownThreadsHandler.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */
package org.jacpfx.rcp.util;

import org.jacpfx.concurrency.FXWorker;
import org.jacpfx.rcp.handler.ExceptionHandler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
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
	/**
	 * Register a Thread.
	 * @param t the Thread to register
     * @param <T> the type of the object extending the thread
	 */
	public static <T extends Thread> void registerThread(T t) {
        t.setUncaughtExceptionHandler(ExceptionHandler.getInstance());
		registeredThreads.add(t);
	}

    /**
     * unregister a Thread.
     * @param t the Thread to unregister
     * @param <T> the type of the object extending the thread
     */
    public static <T extends Thread> void unRegisterThread(T t) {
        if(registeredThreads.contains(t)){
            registeredThreads.remove(t);
        }
    }
	/**
	 * Register an Executor service.
	 * @param t the ExecutorService to register
     * @param <E> the concrete type of the ExecutorService
	 */
	public static <E extends ExecutorService> void registerExecutor(E t) {
		registeredExecutors.add(t);
	}
	/**
	 * Shutdown all registered Threads.
	 */
	public static void shutdownThreads() {
		FXWorker.APPLICATION_RUNNING.set(false);
		registeredThreads.stream().filter(t -> t.isAlive()).forEach(t -> {
			logger.finest("shutdown thread: " + t);
			t.interrupt();
		});
	}
	/**
	 * Shutdown all registered Executors.
	 */
	public static void shutDownExecutors() {
		registeredExecutors.forEach(java.util.concurrent.ExecutorService::shutdown);
	}
	
	/**
	 * Shutdown registered Threads and Executors.
	 */
	public static void shutdowAll() {
		FXWorker.APPLICATION_RUNNING.set(false);
		registeredThreads.stream().filter(t -> t.isAlive()).forEach(t -> {
			logger.finest("shutdown thread: " + t);
			t.interrupt();
		});
		registeredExecutors.forEach(java.util.concurrent.ExecutorService::shutdown);
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		// force to interrupt all threads in waiting condition
		threadSet.stream().filter(t -> t.getName().contains(HandlerThreadFactory.PREFIX) && !t.isInterrupted()).forEach(java.lang.Thread::interrupt);
		
	}
}
