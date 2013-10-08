package org.jacp.javafx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.component.IPerspective;
import org.jacp.javafx.rcp.util.FXUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Created with IntelliJ IDEA.
 * User: Andy
 * Date: 28.05.13
 * Time: 21:13
 * Global registry with references to all perspectives
 */
public class PerspectiveRegistry {
    private static volatile List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new ArrayList<>();
    private static volatile StampedLock lock = new StampedLock();
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
        final long stamp = lock.tryWriteLock();
        try{
            if (!perspectives.contains(perspective))
                perspectives.add(perspective);
        }finally{
            lock.unlockWrite(stamp);
        }

    }

    /**
     * Removes component from registry.
     *
     * @param perspective, a perspective to remove
     */
    public static void removePerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final long stamp = lock.tryWriteLock();
        try{
            if (perspectives.contains(perspective))
                perspectives.remove(perspective);
        }finally{
            lock.unlockWrite(stamp);
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
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            final List<IPerspective<EventHandler<Event>, Event, Object>> p = perspectives;
            if (lock.validate(stamp))
                return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                        p);
        }
        stamp = lock.readLock(); // fall back to read lock
        try{
            return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                    perspectives);
        }finally{
            lock.unlockRead(stamp);
        }

    }
    /**
     * Returns the a component by class.
     * @param clazz , a perspective class
     * @return   a perspective
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findPerspectiveByClass(final Class<?> clazz) {
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            final List<IPerspective<EventHandler<Event>, Event, Object>> p = perspectives;
            if (lock.validate(stamp)){
                for(final IPerspective<EventHandler<Event>, Event, Object> comp : p) {
                    if(comp.getClass().isAssignableFrom(clazz))return comp;
                }
                return null;
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try{
            for(final IPerspective<EventHandler<Event>, Event, Object> comp : perspectives) {
                if(comp.getClass().isAssignableFrom(clazz))return comp;
            }
            return null;
        }finally{
            lock.unlockRead(stamp);
        }
    }

}
