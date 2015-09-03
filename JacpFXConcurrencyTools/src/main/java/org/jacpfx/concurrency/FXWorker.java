/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [FXWorker.java]
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

package org.jacpfx.concurrency;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Andy Moncsek on 01.09.15.
 */
public final class FXWorker<T> {

    public static final AtomicLong WAIT = new AtomicLong(3500L);
    public static final AtomicBoolean APPLICATION_RUNNING = new AtomicBoolean(true);
    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();


    private final List<ExecutionStep> steps;


    private FXWorker() {
        steps = new ArrayList<>();
    }

    /**
     * internal constructor for builder pattern
     *
     * @param steps, all executen step
     */
    private FXWorker(List<ExecutionStep> steps) {
        this.steps = steps;
    }


    /**
     * Returns an instace of async handler
     *
     * @param <T>, the type of return
     * @return org.jacpfx.concurrency.FXWorker , the handler instance
     */
    public static <T> FXWorker getInstance() {
        return new FXWorker<T>();
    }


    /**
     * accepts an {@link Consumer} to be applyed on JavaFX application thread.
     * This method ensures the invocation on the FX thread even when the execution started in an different thread.
     *
     * @param consumer , the consumer to execute
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final FXWorker<Void> consumeOnFXThread(Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.FX_THREAD);
    }

    /**
     * accepts an {@link Supplier} to be applyed on JavaFX application thread.  This method ensures the invocation on the FX thread even when the execution started in an different thread and returnes a value of type T.
     *
     * @param supplier the supplier to execute
     * @param <T>      the return type
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <T> FXWorker<T> supplyOnFXThread(Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.FX_THREAD);
    }


    public final <T> FXWorker<T> onError(final Function<Throwable, T> fnException){

         if(steps!=null & !steps.isEmpty()) {
             final ExecutionStep executionStep = steps.get(0); // the last step which should get an exception handler
             final ExecutionStep updatedStep = new ExecutionStep(executionStep.getFunction(),executionStep.getType(),executionStep.getFeature(),fnException);
             steps.set(0,updatedStep);
         }
        return new FXWorker<>(steps);

    }

    /**
     * accepts an {@link Function} to be applyed on JavaFX application thread.
     *
     * @param function the function to execute
     * @param <V>      the type of return
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <V> FXWorker<V> functionOnFXThread(Function<T, V> function) {
        return addUserFunction(function, ExecutionType.FX_THREAD);
    }

    /**
     * accepts an {@link Consumer} to be applyed on worker  thread.
     *
     * @param consumer the consumer to execute
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final FXWorker<Void> consumeOnExecutorThread(Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.POOL);
    }

    /**
     * accepts an {@link Supplier} to be applyed on worker thread.
     *
     * @param supplier the supplier to execute
     * @param <T>      the type of return
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <T> FXWorker<T> supplyOnExecutorThread(Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.POOL);
    }

    /**
     * accepts an {@link Function} to be applyed on worker  thread.
     *
     * @param function the function to execute
     * @param <V>      the type of return
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <V> FXWorker<V> functionOnExecutorThread(Function<T, V> function) {
        return addUserFunction(function, ExecutionType.POOL);
    }

    /**
     * the terminal execute method which starts the execution chain
     */
    public final void execute() {
        execute(() -> {
        });

    }

    /**
     * the terminal execute method which starts the execution chain.
     *
     * @param r the supplied runnable will be invoked on fx application thread when the chain has finished
     */
    public final void execute(Runnable r) {
        executeChain().thenRun(() -> {
            try {
                executeOnFXThread(r);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * the terminal execute method which starts the execution chain.
     *
     * @param r the supplied consumer will be invoked on fx application thread when the chain has finished, the input of the consumer is the last output of your execution chain
     */
    public final void execute(Consumer<T> r) {
        executeChain().thenAccept((value) -> {
            try {
                executeOnFXThread(() -> r.accept(value));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private CompletableFuture<T> executeChain() {
        final ExecutionStep lastStep = steps.stream().reduce(null, (a, b) -> {
            if (a == null)
                return getExecutionStep(b, CompletableFuture.supplyAsync(() -> null, EXECUTOR));
            return getExecutionStep(b, a.getFeature());
        });
        return lastStep.getFeature();

    }

    private ExecutionStep getExecutionStep(ExecutionStep b, CompletableFuture<T> f) {
        switch (b.getType()) {
            case FX_THREAD:
                return new ExecutionStep(b.getFunction(), b.getType(), f.thenApply((val) -> applyFunction(val, b)));
            case POOL:
                return new ExecutionStep(b.getFunction(), b.getType(), f.thenApplyAsync((val) -> applyFunction(val, b)));
            default:
                return new ExecutionStep(b.getFunction(), b.getType(), f);
        }
    }

    private Object applyFunction(Object val, ExecutionStep b) {
        try {
            return b.getFunction().apply(val);
        } catch (Exception e) {
            // Check XF_Thread when needed
            if (b.getFnException() != null) {

                switch (b.getType()) {
                    case FX_THREAD:
                        final AtomicReference<Object> resultRef = new AtomicReference<>();
                        try {
                            executeOnFXThread(() -> resultRef.set(b.getFnException().apply(e.getCause())));
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                        return resultRef.get();
                    case POOL:
                        return b.getFnException().apply(e);
                    default:
                        return b.getFnException().apply(e);
                }

            } else {
                e.printStackTrace();
            }


        }
        return null;
    }


    private <V> FXWorker<V> addSupplier(Supplier<V> supplier, ExecutionType type) {
        return addFunction((e) -> {
                    switch (type) {
                        case POOL:
                            return supplier.get();
                        case FX_THREAD:
                            return invokeOnFXThread(supplier);
                        default:
                            return null;

                    }
                }, type
        );
    }

    private <V> FXWorker<V> addSupplier(Supplier<V> supplier, ExecutionType type, final Function<Throwable, V> fnException) {
        return addFunction((e) -> {
                    switch (type) {
                        case POOL:
                            return supplier.get();
                        case FX_THREAD:
                            return invokeOnFXThread(supplier);
                        default:
                            return null;

                    }
                }, type, fnException
        );
    }

    private FXWorker<Void> addConsumer(Consumer<T> consumer, ExecutionType type) {
        return addFunction((CheckedFunction<T, Void>) (e) -> {
            switch (type) {
                case POOL:
                    consumer.accept(e);
                    break;
                case FX_THREAD:
                    invokeOnFXThread(consumer, e);
                    break;
            }
            return null;
        }, type);
    }

    private <V> FXWorker<V> addUserFunction(Function<T, V> function, ExecutionType type) {
        return addFunction((e) -> {
            switch (type) {
                case POOL:
                    return function.apply(e);
                case FX_THREAD:
                    return invokeOnFXThread(function, e);
            }
            return null;
        }, type);
    }

    private <V> FXWorker<V> addUserFunction(Function<T, V> function, ExecutionType type, final Function<Throwable, V> fnException) {
        return addFunction((e) -> {
            switch (type) {
                case POOL:
                    return function.apply(e);
                case FX_THREAD:
                    return invokeOnFXThread(function, e);
            }
            return null;
        }, type, fnException);
    }

    private <V> FXWorker<V> addFunction(CheckedFunction<T, V> function, ExecutionType type) {
        return addStepp(new ExecutionStep<>(function, type));
    }

    private <V> FXWorker<V> addFunction(final CheckedFunction<T, V> function, final ExecutionType type, final Function<Throwable, V> fnException) {
        return addStepp(new ExecutionStep<>(function, type, fnException));
    }

    private <V> FXWorker<V> addStepp(ExecutionStep<T, V> stepp) {
        steps.add(stepp);
        return new FXWorker<>(steps);
    }


    private <V> V invokeOnFXThread(Supplier<V> supplier) throws ExecutionException {
        if (isFXThread()) {
            return supplier.get();
        } else {
            return invokeOnApplicationThread(supplier);
        }
    }

    private void invokeOnFXThread(Consumer<T> consumer, T e) throws ExecutionException {
        if (isFXThread()) {
            consumer.accept(e);
        } else {
            invokeOnApplicationThread(consumer, e);
        }
    }

    private <V> V invokeOnFXThread(Function<T, V> function, T e) throws ExecutionException {
        if (isFXThread()) {
            return function.apply(e);
        } else {
            return invokeOnApplicationThread(function, e);
        }
    }

    private static boolean isFXThread() {
        return Platform.isFxApplicationThread();
    }

    private <V> V invokeOnApplicationThread(Supplier<V> supplier) throws ExecutionException {
        final AtomicReference<V> resultRef = new AtomicReference<>();
        executeOnFXThread(() -> resultRef.set(supplier.get()));
        return resultRef.get();
    }

    private <V> V invokeOnApplicationThread(Function<T, V> function, T e) throws ExecutionException {
        final AtomicReference<V> resultRef = new AtomicReference<>();
        executeOnFXThread(() -> resultRef.set(function.apply(e)));
        return resultRef.get();
    }


    private void invokeOnApplicationThread(Consumer<T> consumer, T e) throws ExecutionException {
        executeOnFXThread(() -> consumer.accept(e));
    }

    private void executeOnFXThread(Runnable r) throws ExecutionException {
        final Thread t = Thread.currentThread();
        try {
            invokeOnFXThreadAndWait(r);
        } catch (InterruptedException e1) {
            t.getUncaughtExceptionHandler().uncaughtException(t, e1);
        }
    }


    /**
     * Update the max wait time a process can run on FXThread
     *
     * @param waitTime
     */
    public static void updateMaxWaitTime(Long waitTime) {
        WAIT.set(waitTime);
    }

    /**
     * invokes a runnable on application thread and waits until execution is
     * finished
     *
     * @param runnable, a runnable which will be invoked and wait until execution is finished
     * @throws InterruptedException                    when thread was interrupted on shutdown
     * @throws java.util.concurrent.ExecutionException when an exception was thrown in a component
     */
    public static void invokeOnFXThreadAndWait(final Runnable runnable)
            throws InterruptedException, ExecutionException {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final AtomicBoolean conditionReady = new AtomicBoolean(false);
        final AtomicReference<ThrowableWrapper> exchanger = new AtomicReference<>();
        lock.lock();
        try {
            Platform.runLater(() -> {
                lock.lock();
                try {
                    // prevent execution when application is closed
                    if (APPLICATION_RUNNING.get())
                        runnable.run();
                } catch (Exception e) {
                    final ThrowableWrapper throwableWrapper = new ThrowableWrapper(e);
                    exchanger.set(throwableWrapper);
                } finally {
                    conditionReady.set(true);
                    condition.signal();
                    lock.unlock();
                }

            });
            // wait until execution is finished and check if application is
            // still running to prevent wait
            while (!conditionReady.get()
                    && APPLICATION_RUNNING.get())
                condition.await(WAIT.get(),
                        TimeUnit.MILLISECONDS);

            final ThrowableWrapper throwableWrapper = exchanger.get();
            if (throwableWrapper != null) {
                throw new ExecutionException(throwableWrapper.t);
            }
        } finally {
            lock.unlock();
        }
    }
}
