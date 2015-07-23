/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AccessUtil.java]
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

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 02.12.13
 * Time: 22:32
 * Util method to check if access is allowed
 */
public class AccessUtil {


    public static boolean hasAccess(final String callerClass, Class ...clazz) {
        final Class<?> caller = getClassByString(callerClass);
        if(caller==null) return false;
         for(final Class c : clazz) {
             if(c.isAssignableFrom(caller)) return true;
         }
        return false;

    }



    private static Class<?> getClassByString(final String className) {
        if(className==null) return null;
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
