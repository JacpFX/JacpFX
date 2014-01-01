package org.jacpfx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.rcp.util.FXUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;

/**
 * Created with IntelliJ IDEA.
 * User: Andy
 * Date: 28.05.13
 * Time: 21:13
 * Global registry with references to all perspectives
 */
public class PerspectiveRegistry {
    private static final List<IPerspective<EventHandler<Event>, Event, Object>> perspectives = new CopyOnWriteArrayList<>();
    private static final AtomicReference<String> currentVisiblePerspectiveId = new AtomicReference<>();
    private static final Collector<IPerspective<EventHandler<Event>, Event, Object>, ?, TreeSet<IPerspective<EventHandler<Event>, Event, Object>>> collector = Collector.of(TreeSet::new, TreeSet::add,
            (left, right) -> {
                left.addAll(right);
                return left;
            });

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
     *
     * @return a list of current registered perspectives
     */
    private static List<IPerspective<EventHandler<Event>, Event, Object>> getAllPerspectives() {
        return Collections.unmodifiableList(perspectives);
    }


    /**
     * Registers a perspective.
     *
     * @param perspective, a perspective to register
     */
    public static void registerPerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        if (!perspectives.contains(perspective))
            perspectives.add(perspective);
    }

    /**
     * Removes perspective from registry.
     *
     * @param perspective, a perspective to remove
     */
    public static void removePerspective(
            final IPerspective<EventHandler<Event>, Event, Object> perspective) {
        if (perspectives.contains(perspective))
            perspectives.remove(perspective);
    }

    /**
     * Returns the next active perspective. This can happen when a perspective was set to inactive. In this case the next underlying perspective should be displayed.
     *
     * @return
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findNextActivePerspective(final IPerspective<EventHandler<Event>, Event, Object> current) {
        return getNextValidPerspective(getAllPerspectives(), current);
    }

    /**
     * Return an active perspective
     *
     * @param p,       The List with all Perspectives
     * @param current, the current perspective
     * @return
     */
    private static IPerspective<EventHandler<Event>, Event, Object> getNextValidPerspective(final List<IPerspective<EventHandler<Event>, Event, Object>> p, final IPerspective<EventHandler<Event>, Event, Object> current) {
        final TreeSet<IPerspective<EventHandler<Event>, Event, Object>> allActive = p.stream()
                .filter(active -> active.getContext().isActive() || active.equals(current))
                .collect(collector);
        return selectCorrectPerspective(current, allActive);
    }

    private static IPerspective<EventHandler<Event>, Event, Object> selectCorrectPerspective(final IPerspective<EventHandler<Event>, Event, Object> current, final TreeSet<IPerspective<EventHandler<Event>, Event, Object>> allActive) {
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
        return FXUtil.getObserveableById(FXUtil.getTargetPerspectiveId(targetId),
                getAllPerspectives());
    }

    /**
     * Searches the given component id in metadata of all perspectives and returns the responsible perspective
     *
     * @param componentId
     * @return The parent perspective of given component id
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findParentPerspectiveByComponentId(final String componentId) {
        return findByComponentId(getAllPerspectives(), componentId);
    }

    private static IPerspective<EventHandler<Event>, Event, Object> findByComponentId(List<IPerspective<EventHandler<Event>, Event, Object>> perspectives, final String componentId) {
        final Optional<IPerspective<EventHandler<Event>, Event, Object>> first = perspectives.parallelStream()
                .filter(p -> {
                    final Class perspectiveClass = p.getPerspective().getClass();
                    if (!perspectiveClass.isAnnotationPresent(Perspective.class)) return false;
                    final Perspective annotation = (Perspective) perspectiveClass.getAnnotation(Perspective.class);
                    return containsComponentInAnnotation(annotation, componentId);
                }).findFirst();

        return first.isPresent() ? first.get() : null;
    }

    private static boolean containsComponentInAnnotation(final Perspective annotation, final String componentId) {
        final String[] componentIds = annotation.components();
        Arrays.parallelSort(componentIds);
        return Arrays.binarySearch(componentIds, componentId) >= 0;
    }

    /**
     * Returns the a component by class.
     *
     * @param clazz , a perspective class
     * @return a perspective
     */
    public static IPerspective<EventHandler<Event>, Event, Object> findPerspectiveByClass(final Class<?> clazz) {
        final Optional<IPerspective<EventHandler<Event>, Event, Object>> first = getAllPerspectives().parallelStream().filter(p -> p.getPerspective().getClass().isAssignableFrom(clazz)).findFirst();
        return first.isPresent() ? first.get() : null;
    }

}