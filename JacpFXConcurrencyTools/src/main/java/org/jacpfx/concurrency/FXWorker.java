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
import javafx.beans.property.*;

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
    private final static ExecutorService EXECUTOR = Executors.newWorkStealingPool();
    private final DoubleProperty progress;
    private final StringProperty message;
    private final BooleanProperty cancel;


    private final List<ExecutionStep> steps;


    private FXWorker() {
        steps = new ArrayList<>();
        progress = new SimpleDoubleProperty(this, "progress", -1);
        message = new SimpleStringProperty(this, "message", "");
        cancel = new SimpleBooleanProperty(this, "cancel", false);
    }

    /**
     * internal constructor for builder pattern
     *
     * @param steps, all executen step
     * @param cancel
     */
    private FXWorker(final List<ExecutionStep> steps, final DoubleProperty progress, final StringProperty message, final BooleanProperty cancel) {
        this.steps = steps;
        this.progress = progress;
        this.message = message;
        this.cancel = cancel;
    }


    /**
     * Returns an instace of async handler
     *
     * @param <T>, the type of return
     * @return org.jacpfx.concurrency.FXWorker , the handler instance
     */
    public static <T> FXWorker instance() {
        return new FXWorker<T>();
    }


    /**
     * accepts an {@link Consumer} to be applyed on JavaFX application thread.
     * This method ensures the invocation on the FX thread even when the execution started in an different thread.
     *
     * @param consumer , the consumer to execute
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final FXWorker<Void> consumeOnFXThread(final Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.FX_THREAD);
    }

    /**
     * accepts an {@link Supplier} to be applyed on JavaFX application thread.  This method ensures the invocation on the FX thread even when the execution started in an different thread and returnes a value of type T.
     *
     * @param supplier the supplier to execute
     * @param <T>      the return type
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <T> FXWorker<T> supplyOnFXThread(final Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.FX_THREAD);
    }


    public final <T> FXWorker<T> onError(final Function<Throwable, T> fnException) {

        if (steps != null & !steps.isEmpty()) {
            final ExecutionStep executionStep = steps.get(steps.size() - 1); // the last step which should get an exception handler
            // TODO create builder
            steps.set(steps.size() - 1, new ExecutionStep(executionStep.getFunction(), executionStep.getType(), executionStep.getFeature(), fnException, executionStep.getPos(), executionStep.getAmount()));
        }
        return new FXWorker<>(steps, progress, message, cancel);

    }

    public final <T> FXWorker<T> retry(final int amount) {
        // TODO implement
        return new FXWorker<>(steps, progress, message, cancel);
    }

    /**
     * accepts a {@link Function} to be applyed on JavaFX application thread.
     *
     * @param function the function to execute
     * @param <V>      the type of return
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <V> FXWorker<V> functionOnFXThread(final Function<T, V> function) {
        return addUserFunction(function, ExecutionType.FX_THREAD);
    }

    /**
     * accepts an {@link Consumer} to be applyed on worker  thread.
     *
     * @param consumer the consumer to execute
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final FXWorker<Void> consumeOnExecutorThread(final Consumer<T> consumer) {
        return addConsumer(consumer, ExecutionType.POOL);
    }

    /**
     * accepts a {@link Supplier} to be applyed on worker thread.
     *
     * @param supplier the supplier to execute
     * @param <T>      the type of return
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <T> FXWorker<T> supplyOnExecutorThread(final Supplier<T> supplier) {
        return addSupplier(supplier, ExecutionType.POOL);
    }

    /**
     * accepts a {@link Function} to be applyed on worker  thread.
     *
     * @param function the function to execute
     * @param <V>      the type of return
     * @return {@link org.jacpfx.concurrency.FXWorker}
     */
    public final <V> FXWorker<V> functionOnExecutorThread(final Function<T, V> function) {
        return addUserFunction(function, ExecutionType.POOL);
    }

    /**
     * Cancel the current execution
     */
    public void cancel() {
        cancel.setValue(true);
    }

    /**
     * the terminal execute method which starts the execution chain
     */
    public final FXWorker<?> execute() {
        return execute(() -> {
        });

    }

    /**
     * the terminal execute method which starts the execution chain.
     *
     * @param r the supplied runnable will be invoked on fx application thread when the chain has finished
     */
    public final FXWorker<?> execute(final Runnable r) {
        // TODO handle exception in execute method with speciffic error function
        executeChain().thenRun(() -> {
            try {
                executeOnFXThread(r);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        cancel.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // TODO update message
            }
        });
        return this;
    }

    /**
     * the terminal execute method which starts the execution chain.
     *
     * @param r the supplied consumer will be invoked on fx application thread when the chain has finished, the input of the consumer is the last output of your execution chain
     */
    public final FXWorker<?> execute(final Consumer<T> r) {
        executeChain().thenAccept((value) -> {
            try {
                executeOnFXThread(() -> r.accept(value));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        cancel.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // TODO update message
            }
        });

        return this;
    }

    private CompletableFuture<T> executeChain() {
        final int amount = steps.size();
        final ExecutionStep lastStep = steps.stream().reduce(null, (a, b) -> {
            if (a == null)
                return getExecutionStep(b, CompletableFuture.supplyAsync(() -> null, EXECUTOR), 1, amount);
            return getExecutionStep(b, a.getFeature(), a.getPos() + 1, amount);
        });
        return lastStep.getFeature();

    }

    private ExecutionStep getExecutionStep(final ExecutionStep step, final CompletableFuture<T> future, final int pos, final int amount) {
        switch (step.getType()) {
            case FX_THREAD:
                return new ExecutionStep(step.getFunction(), step.getType(), future.thenApply((val) -> applyFunction(val, step)), pos, amount);
            case POOL:
                return new ExecutionStep(step.getFunction(), step.getType(), future.thenApplyAsync((val) -> applyFunction(val, step)));
            default:
                return new ExecutionStep(step.getFunction(), step.getType(), future);
        }
    }

    private Object applyFunction(final Object val, final ExecutionStep step) {
        if (cancel.get()) return null;
        try {
            return step.getFunction().apply(val);
        } catch (Exception e) {
            if (handleExceptionOnStepExecution(step, e)) return invokeExceptionHandler(step, e);
        } finally {
            updateProgress(step);
        }
        return null;
    }

    private void updateProgress(final ExecutionStep step) {
        Platform.runLater(() -> {
            // TODO check if progress was updated manually
            if (step.getAmount() == step.getPos()) {
                progress.set(1);
            } else {
                if (!isProgressManuallyUpdated(step)) {
                    progress.set(step.getPos() / step.getAmount());
                }

            }
        });
    }

    private boolean isProgressManuallyUpdated(ExecutionStep step) {
        return step.getPos() > 1 && ((step.getPos() - 1) / step.getAmount()) != progress.get();
    }

    private boolean handleExceptionOnStepExecution(final ExecutionStep step, final Exception e) {
        // Check XF_Thread when needed
        if (step.getFnException() != null)
            return true;
        e.printStackTrace();
        return false;
    }

    private Object invokeExceptionHandler(final ExecutionStep step, final Exception e) {
        switch (step.getType()) {
            case FX_THREAD:
                return invokeExceptionHandlerOnFXThread(step, e);
            case POOL:
                return step.getFnException().apply(e);
            default:
                return step.getFnException().apply(e);
        }
    }

    private Object invokeExceptionHandlerOnFXThread(final ExecutionStep step, final Exception e) {
        final AtomicReference<Object> resultRef = new AtomicReference<>();
        try {
            executeOnFXThread(() -> resultRef.set(step.getFnException().apply(e.getCause())));
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
        return resultRef.get();
    }


    private <V> FXWorker<V> addSupplier(final Supplier<V> supplier, final ExecutionType type) {
        return addFunction((value) -> {
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


    private FXWorker<Void> addConsumer(final Consumer<T> consumer, final ExecutionType type) {
        return addFunction((CheckedFunction<T, Void>) (value) -> {
            switch (type) {
                case POOL:
                    consumer.accept(value);
                    break;
                case FX_THREAD:
                    invokeOnFXThread(consumer, value);
                    break;
            }
            return null;
        }, type);
    }

    private <V> FXWorker<V> addUserFunction(final Function<T, V> function, final ExecutionType type) {
        return addFunction((value) -> {
            switch (type) {
                case POOL:
                    return function.apply(value);
                case FX_THREAD:
                    return invokeOnFXThread(function, value);
            }
            return null;
        }, type);
    }


    private <V> FXWorker<V> addFunction(final CheckedFunction<T, V> function, final ExecutionType type) {
        return addStepp(new ExecutionStep<>(function, type));
    }


    private <V> FXWorker<V> addStepp(final ExecutionStep<T, V> stepp) {
        steps.add(stepp);
        return new FXWorker<>(steps, progress, message, cancel);
    }


    private <V> V invokeOnFXThread(final Supplier<V> supplier) throws ExecutionException {
        if (isFXThread()) {
            return supplier.get();
        } else {
            return invokeOnApplicationThread(supplier);
        }
    }

    private void invokeOnFXThread(final Consumer<T> consumer, final T value) throws ExecutionException {
        if (isFXThread()) {
            consumer.accept(value);
        } else {
            invokeOnApplicationThread(consumer, value);
        }
    }

    private <V> V invokeOnFXThread(final Function<T, V> function, final T value) throws ExecutionException {
        if (isFXThread()) {
            return function.apply(value);
        } else {
            return invokeOnApplicationThread(function, value);
        }
    }

    private static boolean isFXThread() {
        return Platform.isFxApplicationThread();
    }

    private <V> V invokeOnApplicationThread(final Supplier<V> supplier) throws ExecutionException {
        final AtomicReference<V> resultRef = new AtomicReference<>();
        executeOnFXThread(() -> resultRef.set(supplier.get()));
        return resultRef.get();
    }

    private <V> V invokeOnApplicationThread(final Function<T, V> function, final T value) throws ExecutionException {
        final AtomicReference<V> resultRef = new AtomicReference<>();
        executeOnFXThread(() -> resultRef.set(function.apply(value)));
        return resultRef.get();
    }


    private void invokeOnApplicationThread(final Consumer<T> consumer, final T value) throws ExecutionException {
        executeOnFXThread(() -> consumer.accept(value));
    }

    private void executeOnFXThread(final Runnable r) throws ExecutionException {
        // TODO check if already running on FX application thread
        if (cancel.get()) return;
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
        // TODO make it configureable for each step!!
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
                    exchanger.set(new ThrowableWrapper(e));
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

    public final double getProgress() {
        checkThread();
        return progress.get();
    }

    public final ReadOnlyDoubleProperty progressProperty() {
        checkThread();
        return progress;
    }

    public final void updateProgress(final double progressValue) {
        if (isFXThread()) {
            progress.set(progressValue);
        } else {
            Platform.runLater(() -> progress.set(progressValue));
        }

    }

    public final String getMessage() {
        return message.get();
    }

    public final ReadOnlyStringProperty messageProperty() {
        return message;
    }

    public final void updateMessage(final String messageValue) {

        if (isFXThread()) {
            message.set(messageValue);
        } else {
            Platform.runLater(() -> message.set(messageValue));
        }
    }

    void checkThread() {
        if (!isFXThread()) {
            throw new IllegalStateException("Service must only be used from the FX Application Thread");
        }
    }
}
