/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [Component.java]
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

package org.jacpfx.rcp.context;

import javafx.application.Platform;
import org.jacpfx.rcp.util.WorkerUtil;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Andy Moncsek on 16.04.15.
 */
public class AsyncHandler<T> {

    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private List<ExecutionStep> steps;


    private AsyncHandler() {
        steps = new ArrayList<>();
    }

    /**
     * internal constructor for builder pattern
     *
     * @param steps, all executen step
     */
    private AsyncHandler(List<ExecutionStep> steps) {
        this.steps = steps;
    }


    /**
     * Returns an instace of async handler
     *
     * @return org.jacpfx.rcp.context.AsyncHandler , the handler instance
     */
    public static <T> AsyncHandler getInstance() {
        return new AsyncHandler<T>();
    }


    /**
     * accepts an {@link java.util.function.Consumer} to be applyed on JavaFX application thread.
     * This method ensures the invocation on the FX thread even when the execution started in an different thread.
     *
     * @param consumer , the consumer to execute
     * @return {@link org.jacpfx.rcp.context.AsyncHandler}
     */
    public AsyncHandler<Void> consumeOnFXThread(Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.FX_THREAD);
    }

    /**
     * accepts an {@link java.util.function.Supplier} to be applyed on JavaFX application thread.  This method ensures the invocation on the FX thread even when the execution started in an different thread and returnes a value of type T.
     *
     * @param supplier the supplier to execute
     * @param <T>      the return type
     * @return {@link org.jacpfx.rcp.context.AsyncHandler}
     */
    public <T> AsyncHandler<T> supplyOnFXThread(Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.FX_THREAD);
    }

    public <V> AsyncHandler<T> functionOnFXThread(Function<T, V> supplier) {
        throw new NotImplementedException();
    }

    /**
     * accepts an {@link java.util.function.Consumer} to be applyed on worker  thread.
     *
     * @param consumer the consumer to execute
     * @return {@link org.jacpfx.rcp.context.AsyncHandler}
     */
    public AsyncHandler<Void> consumeOnExecutorThread(Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.POOL);
    }

    /**
     * accepts an {@link java.util.function.Supplier} to be applyed on worker thread.
     *
     * @param supplier the supplier to execute
     * @param <T>      the type of return
     * @return {@link org.jacpfx.rcp.context.AsyncHandler}
     */
    public <T> AsyncHandler<T> supplyOnExecutorThread(Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.POOL);
    }

    public <V> AsyncHandler<T> functionOnExecutorThread(Function<T, V> supplier) {
        throw new NotImplementedException();
    }

    /**
     * the terminal execute method which starts the execution chain
     */
    public void execute() {
        execute(() -> {
        });

    }

    /**
     * the terminal execute method which starts the execution chain.
     *
     * @param r the supplied runnable will be invoked on fx application thread when the chain has finished
     */
    public void execute(Runnable r) {
        CompletableFuture<?> feature = executeChain();
        // TODO ensure execution on FX thread
        feature.thenRun(r);

    }

    /**
     * the terminal execute method which starts the execution chain.
     *
     * @param r the supplied consumer will be invoked on fx application thread when the chain has finished, the input of the consumer is the last output of your execution chain
     */
    public void execute(Consumer<T> r) {
        CompletableFuture<T> feature = executeChain();
        // TODO ensure execution on FX thread
        feature.thenAccept(r);

    }

    private CompletableFuture<T> executeChain() {
        CompletableFuture<T> feature = CompletableFuture.supplyAsync(() -> null, EXECUTOR);
        for (ExecutionStep step : steps) {
            switch (step.getType()) {
                case FX_THREAD:
                    feature = feature.thenApply(step.getFunction());
                    break;
                case POOL:
                    feature = feature.thenApplyAsync(step.getFunction());
                    break;
                default:
            }
        }
        return feature;
    }


    private <V> AsyncHandler<V> addSupplier(Supplier<V> supplier, ExecutionType type) {
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


    private AsyncHandler<Void> addConsumer(Consumer<T> consumer, ExecutionType type) {
        return addFunction((Function<T, Void>) (e) -> {
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

    private <V> V invokeOnFXThread(Supplier<V> supplier) {
        if (isFXThread()) {
            return supplier.get();
        } else {
            return invokeOnApplicationThread(supplier);
        }
    }

    private void invokeOnFXThread(Consumer<T> consumer, T e) {
        if (isFXThread()) {
            consumer.accept(e);
        } else {
            invokeOnApplicationThread(consumer, e);
        }
    }

    private boolean isFXThread() {
        return Platform.isFxApplicationThread();
    }

    private <V> V invokeOnApplicationThread(Supplier<V> supplier) {
        final AtomicReference<V> resultRef = new AtomicReference<>();
        try {
            WorkerUtil.invokeOnFXThreadAndWait(() -> resultRef.set(supplier.get()));
        } catch (InterruptedException e1) {
            // TODO add exception provider
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
        return resultRef.get();
    }


    private void invokeOnApplicationThread(Consumer<T> consumer, T e) {
        try {
            WorkerUtil.invokeOnFXThreadAndWait(() -> consumer.accept(e));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
    }

    private <V> AsyncHandler<V> addFunction(Function<T, V> function, ExecutionType type) {
        return addStepp(new ExecutionStep<>(function, type));
    }

    private <V> AsyncHandler<V> addStepp(ExecutionStep<T, V> stepp) {
        steps.add(stepp);
        return new AsyncHandler<>(steps);
    }


}
