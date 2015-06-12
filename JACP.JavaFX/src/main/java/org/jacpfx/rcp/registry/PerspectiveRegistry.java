package org.jacpfx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.Perspective;
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
 * Global registry with references to all perspective
 */
public class PerspectiveRegistry {
    private static final List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives = new CopyOnWriteArrayList<>();
    private static final AtomicReference<String> currentVisiblePerspectiveId = new AtomicReference<>();
    private static final Collector<Perspective<Node, EventHandler<Event>, Event, Object>, ?, TreeSet<Perspective<Node, EventHandler<Event>, Event, Object>>> collector = Collector.of(TreeSet::new, TreeSet::add,
            (left, right) -> {
                left.addAll(right);
                return left;
            });


    /**
     * clears registry on application shutdown
     */
    public static void clearOnShitdown() {
        perspectives.clear();
    }

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
     * returns the current visible perspective id.
     *
     * @return the current visible perspective id
     */
    public static String getCurrentVisiblePerspective() {
        return currentVisiblePerspectiveId.get();
    }

    /**
     * Returns a unmodifiable list of all available perspective.
     *
     * @return a list of current registered perspective
     */
    private static List<Perspective<Node, EventHandler<Event>, Event, Object>> getAllPerspectives() {
        return Collections.unmodifiableList(perspectives);
    }


    /**
     * Registers a perspective.
     *
     * @param perspective, a perspective to register
     */
    public static void registerPerspective(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        if (!perspectives.contains(perspective))
            perspectives.add(perspective);
    }

    /**
     * Removes perspective from registry.
     *
     * @param perspective, a perspective to remove
     */
    public static void removePerspective(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        if (perspectives.contains(perspective))
            perspectives.remove(perspective);
    }

    /**
     * Returns the next active perspective. This can happen when a perspective was set to inactive. In this case the next underlying perspective should be displayed.
     *
     * @param current the current active perspective
     * @return the next active perspective
     */
    public static Perspective<Node, EventHandler<Event>, Event, Object> findNextActivePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> current) {
        return getNextValidPerspective(getAllPerspectives(), current);
    }

    /**
     * Return an active perspective
     *
     * @param p,       The List with all Perspectives
     * @param current, the current perspective
     * @return the next valid perspective
     */
    private static Perspective<Node, EventHandler<Event>, Event, Object> getNextValidPerspective(final List<Perspective<Node, EventHandler<Event>, Event, Object>> p, final Perspective<Node, EventHandler<Event>, Event, Object> current) {
        final TreeSet<Perspective<Node, EventHandler<Event>, Event, Object>> allActive = p.stream()
                .filter(active -> active.getContext().isActive() || active.equals(current))
                .collect(collector);
        return selectCorrectPerspective(current, allActive);
    }

    private static Perspective<Node, EventHandler<Event>, Event, Object> selectCorrectPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> current, final NavigableSet<Perspective<Node, EventHandler<Event>, Event, Object>> allActive) {
        Perspective<Node, EventHandler<Event>, Event, Object> targetId = allActive.higher(current);
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
    public static Perspective<Node, EventHandler<Event>, Event, Object> findPerspectiveById(
            final String targetId) {
        return FXUtil.getObserveableById(FXUtil.getTargetPerspectiveId(targetId),
                getAllPerspectives());
    }

    /**
     * Returns a perspective by perspectiveId
     *
     * @param componentId , the target perspective id
     * @param parentId    , the target workbench id
     * @return a perspective
     */
    public static Perspective<Node, EventHandler<Event>, Event, Object> findPerspectiveById(
            final String parentId, final String componentId) {
        return FXUtil.getObserveableByQualifiedId(parentId, componentId,
                getAllPerspectives());
    }

    /**
     * Searches the given component id in metadata of all perspective and returns the responsible perspective
     *
     * @param componentId the component id
     * @return The parent perspective of given component id
     */
    public static Perspective<Node, EventHandler<Event>, Event, Object> findParentPerspectiveByComponentId(final String componentId) {
        return findByComponentId(getAllPerspectives(), componentId);
    }

    private static Perspective<Node, EventHandler<Event>, Event, Object> findByComponentId(List<Perspective<Node, EventHandler<Event>, Event, Object>> perspectives, final String componentId) {
        final Optional<Perspective<Node, EventHandler<Event>, Event, Object>> first = perspectives.stream()
                .filter(p -> {
                    final Class perspectiveClass = p.getPerspective().getClass();
                    if (!perspectiveClass.isAnnotationPresent(org.jacpfx.api.annotations.perspective.Perspective.class))
                        return false;
                    final org.jacpfx.api.annotations.perspective.Perspective annotation = (org.jacpfx.api.annotations.perspective.Perspective) perspectiveClass.getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
                    return containsComponentInAnnotation(annotation, componentId);
                }).findFirst();

        return first.isPresent() ? first.get() : null;
    }



    /**
     * Checks if a specific componentId is present in defined perspective annotation. This method call assumes that a check for component instances for this perspective was already done
     *
     * @param parentId    The perspective ID
     * @param componentId The component ID
     * @return True if component exists in perspective
     */
    public static boolean perspectiveContainsComponentIdInAnnotation(final String parentId, final String componentId) {
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = findPerspectiveById(parentId);
        if (perspective == null) return false;
        final Class perspectiveClass = perspective.getPerspective().getClass();
        if (!perspectiveClass.isAnnotationPresent(org.jacpfx.api.annotations.perspective.Perspective.class))
            return false;
        final org.jacpfx.api.annotations.perspective.Perspective annotation = (org.jacpfx.api.annotations.perspective.Perspective) perspectiveClass.getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
        return containsComponentInAnnotation(annotation, componentId);
    }

    private static boolean containsComponentInAnnotation(final org.jacpfx.api.annotations.perspective.Perspective annotation, final String componentId) {
        final String[] componentIds = annotation.components();
        Arrays.sort(componentIds);
        return Arrays.binarySearch(componentIds, componentId) >= 0;
    }


    /**
     * Returns the a component by class.
     *
     * @param clazz , a perspective class
     * @return a perspective
     */
    public static Perspective<Node, EventHandler<Event>, Event, Object> findPerspectiveByClass(final Class<?> clazz) {
        final Optional<Perspective<Node, EventHandler<Event>, Event, Object>> first = getAllPerspectives().stream().filter(p -> p.getPerspective().getClass().isAssignableFrom(clazz)).findFirst();
        return first.isPresent() ? first.get() : null;
    }

}
