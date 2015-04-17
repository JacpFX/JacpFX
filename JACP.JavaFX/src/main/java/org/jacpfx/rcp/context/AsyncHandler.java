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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Andy Moncsek on 16.04.15.
 */
public class AsyncHandler<T> {

    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public List<ExecutionStep> steps;

    public AsyncHandler() {
        steps = new ArrayList<>();
    }

    public AsyncHandler(List<ExecutionStep> steps) {
        this.steps = steps;
    }


    public static AsyncHandler getInstance() {
        return new AsyncHandler();
    }


    public AsyncHandler<Void> onFXThread(Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.FX_THREAD);
    }

    public <U> AsyncHandler<T> onFXThread(Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.FX_THREAD);
    }

    public AsyncHandler<Void> onExecutorThread(Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.POOL);
    }

    public <U> AsyncHandler<T> onExecutorThread(Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.POOL);
    }

    private <V> AsyncHandler<V> addSupplier(Supplier<V> supplier, ExecutionType type) {
        return addFunction((e) -> supplier.get(), type);
    }

    private AsyncHandler<Void> addConsumer(Consumer<T> consumer, ExecutionType type) {
        return addFunction((Function<T, Void>) (e) -> {
            consumer.accept(e);
            return null;
        }, type);
    }

    private <V> AsyncHandler<V> addFunction(Function<T, V> function, ExecutionType type) {
        return addStepp(new ExecutionStep<>(function, type));
    }

    private <V> AsyncHandler<V> addStepp(ExecutionStep<T, V> stepp) {
        steps.add(stepp);
        return new AsyncHandler<>(steps);
    }

    public void execute() {
        execute(() -> {
        });

    }

    public void execute(Runnable r) {
        CompletableFuture<?> feature = executeChain();
        feature.thenRun(r);

    }


    public void execute(Consumer<T> r) {
        CompletableFuture<T> feature = executeChain();
        feature.thenAccept(r);

    }

    private CompletableFuture<T> executeChain() {
        CompletableFuture<T> feature = CompletableFuture.supplyAsync(() -> {
            return null;
        }, EXECUTOR);
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


}
