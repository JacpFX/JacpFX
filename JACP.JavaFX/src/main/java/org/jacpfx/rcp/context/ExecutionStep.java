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

import java.util.function.Function;

/**
 * Created by Andy Moncsek on 16.04.15.
 */
public class ExecutionStep<V, T> {


    private final Function<V, T> function;
    private final ExecutionType type;

    public ExecutionStep(final Function<V, T> function,  final ExecutionType type) {
        this.function = function;
        this.type = type;
    }

    public Function<V, T> getFunction() {
        return this.function;
    }

    public ExecutionType getType() {
        return this.type;
    }
}
