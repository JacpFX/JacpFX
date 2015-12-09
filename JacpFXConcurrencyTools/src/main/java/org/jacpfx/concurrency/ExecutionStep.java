/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ExecutionStep.java]
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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents an execution step in the AsyncHandler
 * Created by Andy Moncsek on 16.04.15.
 */
public class ExecutionStep<V, T> {


    private final CheckedFunction<V, T> function;
    private final ExecutionType type;
    private final CompletableFuture<T> feature;
    private final Function<Throwable, T> fnException;
    private double pos, amount;

    public ExecutionStep(final CheckedFunction<V, T> function, final ExecutionType type, CompletableFuture<T> feature,final Function<Throwable, T> fnException, double pos, double amount) {
        this.function = function;
        this.type = type;
        this.feature = feature;
        this.fnException = fnException;
        this.pos = pos;
        this.amount = amount;
    }

    public ExecutionStep(final CheckedFunction<V, T> function, final ExecutionType type, CompletableFuture<T> feature,double pos, double amount) {
        this(function,type,feature,null,pos,amount);
    }

    public ExecutionStep(final CheckedFunction<V, T> function, final ExecutionType type, CompletableFuture<T> feature) {
        this(function,type,feature,null,0,0);
    }

    public ExecutionStep(final CheckedFunction<V, T> function, final ExecutionType type, CompletableFuture<T> feature,final Function<Throwable, T> fnException) {
        this(function,type,feature,fnException,0,0);
    }


    public ExecutionStep(final CheckedFunction<V, T> function, final ExecutionType type) {
       this(function,type,null,null);
    }




    /**
     * Returns the function to execute
     * @return the @see{java.util.function.Function}
     */
    public CheckedFunction<V, T> getFunction() {
        return this.function;
    }

    /**
     * Returns the type (POOL or FX Thread)
     * @return  the @see{ExecutionType}
     */
    public ExecutionType getType() {
        return this.type;
    }


    /**
     * Returns the compleatable future for execution
     * @return the @see{CompletableFuture}
     */
    public CompletableFuture<T> getFeature() {
        return this.feature;
    }

    /**
     * Returns the exception handler
     * @return  the @see{Function}
     */
    public Function<Throwable, T> getFnException() {
        return this.fnException;
    }

    /**
     * The current position in execution step list
     * @return the current position in list
     */
    public double getPos() {
        return this.pos;
    }

    /**
     * Get the total amount of execution steps
     * @return the total amount
     */
    public double getAmount() {
        return this.amount;
    }
}
