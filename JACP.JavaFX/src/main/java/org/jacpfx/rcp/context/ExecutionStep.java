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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents an execution step in the AsyncHandler
 * Created by Andy Moncsek on 16.04.15.
 */
public class ExecutionStep<V, T> {


    private final Function<V, T> function;
    private final ExecutionType type;
    private final CompletableFuture<T> feature;

    public ExecutionStep(final Function<V, T> function, final ExecutionType type, CompletableFuture<T> feature) {
        this.function = function;
        this.type = type;
        this.feature = feature;
    }

    public ExecutionStep(final Function<V, T> function, final ExecutionType type) {
       this(function,type,null);
    }

    /**
     * Returns the function to execute
     * @return  @link{java.util.function.Function}
     */
    public Function<V, T> getFunction() {
        return this.function;
    }

    /**
     * Returns the type (POOL or FX Thread)
     * @return  @link{ExecutionType}
     */
    public ExecutionType getType() {
        return this.type;
    }


    /**
     * Returns the compleatable future for execution
     * @return
     */
    public CompletableFuture<T> getFeature() {
        return this.feature;
    }
}
