/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2014
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

package org.jacp.misc;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Andy Moncsek on 18.12.14.
 */
public class HashCodeTest {
    private static final int LIMIT = 900_000;
    private void fillAndSearch() {
        Person person = null;
        Map<Class, String> map = new HashMap<>();
        for (int i=0;i<LIMIT;i++) {
            UUID randomUUID = UUID.randomUUID();
            person = new Person("fn", "ln", randomUUID);
            map.put(person.getClass(), "comment" + i);

        }
        long start = System.currentTimeMillis();
        for (int i=0;i<LIMIT;i++) {
        map.get(person.getClass());
        }
        long stop = System.currentTimeMillis();
        System.out.println(stop-start+" millis");
    }
    @Test
    public void testHashCodes() {
        fillAndSearch();
    }

    @Test
    public void testSimpleHashCode() {
         String id1="id01";
         String id1_1="id02";
         String id2="id1";
         String id3="id1.id01";
        System.out.println(id1.hashCode());
        System.out.println(id1_1.hashCode());
        System.out.println(id2.hashCode());
        System.out.println(id3.hashCode());
    }
}
