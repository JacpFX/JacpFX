package org.jacpfx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.rcp.util.FXUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collector;

/**
 * Created with IntelliJ IDEA.
 * User: Andy
 * Date: 28.05.13
 * Time: 21:13
 * Global registry with references to all perspectives
 */
public class PerspectiveRegistry {
    private static final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new ArrayList<>();
    private static final StampedLock lock = new StampedLock();
    private static final AtomicReference<String> currentVisiblePerspectiveId = new AtomicReference<>();

    /**
     * Set a new perspective id and returns the current id.
     *
     * @param id, the new perspective id
     * @return the previous perspective id
     */
    public static String getAndSetCurrentVisiblePerspective(final String id) {
        return currentVisiblePerspectiveId.getAndSet(id);
    }

    /**
     * Returns a unmodifiable list of all available perspectives.
     * @return a list of current registered perspectives
     */
    public static List<IPerspective<EventHandler<Event>, Event, Object>> getAllPerspectives() {
        return Collections.unmodifiableList(perspectives);
    }


    /**
     * Registers a perspective.
     *
     * @param perspective, a perspective to register
     */
    public static void registerPerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final long stamp = lock.tryWriteLock();
        try {
            if (!perspectives.contains(perspective))
                perspectives.add(perspective);
        } finally {
            lock.unlockWrite(stamp);
        }

    }

    /**
     * Removes perspective from registry.
     *
     * @param perspective, a perspective to remove
     */
    public static void removePerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        final long stamp = lock.tryWriteLock();
        try {
            if (perspectives.contains(perspective))
                perspectives.remove(perspective);
        } finally {
            lock.unlockWrite(stamp);
        }

    }

    /**
     * Returns the next active perspective. This can happen when a perspective was set to inactive. In this case the next underlying perspective should be displayed.
     *
     * @return
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findNextActivePerspective(final IPerspective<EventHandler<Event>, Event, Object> current) {
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            final List<IPerspective<EventHandler<Event>, Event, Object>> p = perspectives;
            if (lock.validate(stamp)) {
                return getNextValidPerspective(p, current);
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            return getNextValidPerspective(perspectives, current);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Return an active perspective
     * @param p, The List with all Perspectives
     * @param current, the current perspective
     * @return
     */
    private static IPerspective<EventHandler<Event>, Event, Object> getNextValidPerspective(final List<IPerspective<EventHandler<Event>, Event, Object>> p, final IPerspective<EventHandler<Event>, Event, Object> current) {
        final TreeSet<IPerspective<EventHandler<Event>, Event, Object>> allActive = p.stream()
                .filter(active->active.getContext().isActive() || active.equals(current))
                .collect(Collector.of(TreeSet::new, TreeSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }));

        IPerspective<EventHandler<Event>, Event, Object> targetId = allActive.higher(current);
        if (targetId == null) targetId = allActive.lower(current);
        if (targetId == null) return null;
        return targetId;
    }


    /**
     * Returns a perspective by perspectiveId
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
                return FXUtil.getObserveableById(FXUtil.getTargetPerspectiveId(targetId),
                        p);
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            return FXUtil.getObserveableById(FXUtil.getTargetPerspectiveId(targetId),
                    perspectives);
        } finally {
            lock.unlockRead(stamp);
        }

    }

    /**
     * Searches the given component id in metadata of all perspectives and returns the responsible perspective
     * @return
     */
    public static  IPerspective<EventHandler<Event>, Event, Object> findParentPerspectiveById(final String componentId) {
        perspectives.parallelStream()
                .map(p->p.getClass())
                .filter(c->c.isAnnotationPresent(Perspective.class))
                .map(clazz->clazz.getAnnotation(Perspective.class)).filter(annotation->annotation!=null).findFirst();
        return null;
    }

    private static boolean containsComponentInAnnotation(final Perspective annotation, final String componentId) {
        String[] componentIds = annotation.components();
        Arrays.parallelSort(componentIds);
        return Arrays.binarySearch(componentIds,componentId)>=0?true:false;
    }

    /**
     * Returns the a component by class.
     *
     * @param clazz , a perspective class
     * @return a perspective
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findPerspectiveByClass(final Class<?> clazz) {
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            final List<IPerspective<EventHandler<Event>, Event, Object>> p = perspectives;
            if (lock.validate(stamp)) {
                for (final IPerspective<EventHandler<Event>, Event, Object> comp : p) {
                    if (comp.getClass().isAssignableFrom(clazz)) return comp;
                }
                return null;
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            for (final IPerspective<EventHandler<Event>, Event, Object> comp : perspectives) {
                if (comp.getClass().isAssignableFrom(clazz)) return comp;
            }
            return null;
        } finally {
            lock.unlockRead(stamp);
        }
    }

}
