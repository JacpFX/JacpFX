package org.jacp.javafx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IPerspective;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 * User: Andy
 * Date: 28.05.13
 * Time: 21:13
 * Global registry with references to all perspectives
 */
public class PerspectiveRegistry {
    private static volatile List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new CopyOnWriteArrayList<>();
    private static volatile ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final AtomicReference<String> currentVisiblePerspectiveId = new AtomicReference<>();

    /**
     * Set a new perspective id and returns the current id.
     * @param id, the new perspective id
     * @return   the previous perspective id
     */
    public static String getAndSetCurrentVisiblePerspective(final String id){
        return currentVisiblePerspectiveId.getAndSet(id);
    }


    /**
     * Registers a component.
     *
     * @param perspective, a perspective to register
     */
    public static void registerPerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        lock.writeLock().lock();
        try{
            if (!perspectives.contains(perspective))
                perspectives.add(perspective);
        }finally{
            lock.writeLock().unlock();
        }

    }

    /**
     * Removes component from registry.
     *
     * @param perspective, a perspective to remove
     */
    public static void removePerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        lock.writeLock().lock();
        try{
            if (perspectives.contains(perspective))
                perspectives.remove(perspective);
        }finally{
            lock.writeLock().unlock();
        }

    }

    /**
     * Returns a component by component id
     *
     * @param targetId , the target perspective id
     * @return a perspective
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findPerspectiveById(
            final String targetId) {
        lock.readLock().lock();
        try{
            return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                    perspectives);
        }finally{
            lock.readLock().unlock();
        }

    }
    /**
     * Returns the a component by class.
     * @param clazz , a perspective class
     * @return   a perspective
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findPerspectiveByClass(final Class<?> clazz) {
        lock.readLock().lock();
        try{
            for(final IPerspective<EventHandler<Event>, Event, Object> comp : perspectives) {
                if(comp.getClass().isAssignableFrom(clazz))return comp;
            }
            return null;
        }finally{
            lock.readLock().unlock();
        }
    }

}
