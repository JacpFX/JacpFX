package org.jacp.misc;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Andy Moncsek on 15.12.15.
 */
public class Bleh {
    @Test
    public void testLock() {

        ReadWriteLock lock = new ReentrantReadWriteLock();

        Lock readLock = lock.readLock();

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("A", new Object());
        map.put("B", new Object());
        map.put("C", new Object());

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000000; i++) {
            readLock.lock();

            map.get("A");
            readLock.unlock();
        }
        System.err.println("time : " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testCH() {

        Map<String, Object> map = new ConcurrentHashMap<String, Object>();
        map.put("A", new Object());
        map.put("B", new Object());
        map.put("C", new Object());

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000000; i++) {

            map.get("A");
        }
        System.err.println("time : " + (System.currentTimeMillis() - start));
    }
}
