/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [CustomSecurityManager.java]
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

package org.jacpfx.api.util;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 18.07.13
 * Time: 15:34
 * A custom security manager that exposes the getClassContext() information
 */
public class CustomSecurityManager extends SecurityManager {
    private final static int CALL_STACK_DEPTH = 2;

    public String getCallerClassName() {
        return getClassContext()[CALL_STACK_DEPTH].getName();
    }

}
