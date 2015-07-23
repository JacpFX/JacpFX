/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [InvalidComponentMatch.java]
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

package org.jacpfx.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 20.11.13
 * Time: 21:02
 * This Exception will be thrown when a component in intermediate state like "shutdown" should be executed. This can happen on messages to component that are in shutdown process.
 */
public class InvalidComponentMatch extends RuntimeException {

    public InvalidComponentMatch() {

    }

    public InvalidComponentMatch(String message) {
        super(message);
    }

    public InvalidComponentMatch(String message, Throwable e) {
        super(message, e);
    }

    public InvalidComponentMatch(Throwable e) {
        super(e);
    }
}
