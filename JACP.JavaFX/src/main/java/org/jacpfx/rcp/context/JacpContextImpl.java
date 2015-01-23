package org.jacpfx.rcp.context;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.CustomSecurityManager;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.component.CallbackComponent;
import org.jacpfx.rcp.component.FXComponent;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.components.managedFragment.ManagedFragment;
import org.jacpfx.rcp.components.managedFragment.ManagedFragmentHandler;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;
import org.jacpfx.rcp.message.ActionListenerImpl;
import org.jacpfx.rcp.message.MessageImpl;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.AccessUtil;
import org.jacpfx.rcp.util.FXUtil;
import org.jacpfx.rcp.util.PerspectiveUtil;
import org.jacpfx.rcp.util.WorkerUtil;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.jacpfx.rcp.worker.AComponentWorker;
import org.jacpfx.rcp.worker.AEmbeddedComponentWorker;

import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 24.06.13
 * Time: 21:36
 * JACP context object provides functionality to component context and basic features.
 */
public class JacpContextImpl implements Context,InternalContext {

    private final static CustomSecurityManager customSecurityManager =
            new CustomSecurityManager();
    private volatile BlockingQueue<Message<Event, Object>> globalMessageQueue;
    /**
     * will be set on init
     */
    private String id;
    private volatile String parentId;
    private volatile String fullyQualifiedId;
    /**
     * will be set on init
     */
    private String name;
    private volatile String returnTarget;
    private volatile String targetLayout;
    private volatile String executionTarget = "";
    private volatile FXComponentLayout layout;
    private volatile ResourceBundle resourceBundle;
    private volatile AtomicBoolean active = new AtomicBoolean(false);

    public JacpContextImpl(final String id, final String name, final BlockingQueue<Message<Event, Object>> globalMessageQueue) {
        this.id = id;
        this.name = name;
        this.globalMessageQueue = globalMessageQueue;

    }

    public JacpContextImpl(final BlockingQueue<Message<Event, Object>> globalMessageQueue) {
        this.globalMessageQueue = globalMessageQueue;

    }

    public JacpContextImpl(final String parentId,final BlockingQueue<Message<Event, Object>> globalMessageQueue) {
        this.globalMessageQueue = globalMessageQueue;
        this.parentId = parentId;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final EventHandler<Event> getEventHandler(
            final Object message) {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (AccessUtil.hasAccess(callerClassName, FXWorkbench.class))
            throw new IllegalStateException(" a FXWorkbench is no valid message target");
        return new ActionListenerImpl(new MessageImpl(this.fullyQualifiedId, message),
                this.globalMessageQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final EventHandler<Event> getEventHandler(
            final String targetId, final Object message) {
        return new ActionListenerImpl(new MessageImpl(this.fullyQualifiedId, targetId, message, null),
                this.globalMessageQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void send(final String targetId, final Object message) {
        try {
            this.globalMessageQueue.put(new MessageImpl(this.fullyQualifiedId, targetId, message, null));
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void send(final Object message) {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (AccessUtil.hasAccess(callerClassName, FXWorkbench.class))
            throw new IllegalStateException(" a FXWorkbench is no valid message target");
        try {
            this.globalMessageQueue.put(new MessageImpl(this.fullyQualifiedId,this.id, message, null));

        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getFullyQualifiedId() {
        return this.fullyQualifiedId;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setId(final String id) {
        this.id = id;
        this.fullyQualifiedId = this.parentId!=null?this.parentId.concat(FXUtil.PATTERN_GLOBAL).concat(this.id):this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentId() {
        return this.parentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setParentId(final String parentId) {
        this.parentId = parentId;
        this.fullyQualifiedId = this.id!=null?this.parentId.concat(FXUtil.PATTERN_GLOBAL).concat(this.id):this.parentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ResourceBundle getResourceBundle() {
        return this.resourceBundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return this.active.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActive(boolean active) {
        this.active.set(active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateActiveState(boolean active) {
        this.setActive(active);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ManagedFragmentHandler<T> getManagedFragmentHandler(final Class<T> clazz) {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, FXPerspective.class, FXComponent.class))
            throw new IllegalStateException(" managed fragments are accessible from FXPerspective and FXComponent");
        return ManagedFragment.getInstance().getManagedFragment(clazz, this.parentId,this.id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showModalDialog(final Node node) {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, FXPerspective.class, FXComponent.class))
            throw new IllegalStateException("modal dialogs are accessible from FXPerspective and FXComponent");
        JACPModalDialog.getInstance().showModalDialog(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideModalDialog() {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, FXPerspective.class, FXComponent.class))
            throw new IllegalStateException("modal dialogs are accessible from FXPerspective and FXComponent");
        JACPModalDialog.getInstance().hideModalDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FXComponentLayout getComponentLayout() {
        return this.layout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFXComponentLayout(final FXComponentLayout layout) {
        this.layout = layout;
    }

    /**
     * Returns component id which is targeted by callback component return value; the
     * return value will be handled like an average message and will be
     * delivered to targeted component.
     *
     * @return the target id
     */
    @Override
    public final String getReturnTargetAndClear() {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, CallbackComponent.class, AComponentWorker.class, AEmbeddedComponentWorker.class))
            throw new IllegalStateException(" getReturnTargetAndClear can only be called from a CallbackComponent");
        final String returnVal = String.valueOf(this.returnTarget);
        this.returnTarget = null;
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setReturnTarget(final String returnTargetId) throws IllegalStateException {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, CallbackComponent.class, AComponentWorker.class, AEmbeddedComponentWorker.class))
            throw new IllegalStateException(" the return target can be set only in CallbackComponents");
        this.returnTarget = returnTargetId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void updateReturnTarget(final String returnTargetId) throws IllegalStateException {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, CallbackComponent.class, AComponentWorker.class, AEmbeddedComponentWorker.class))
            throw new IllegalStateException(" the return target can be set only in CallbackComponents");
        this.returnTarget = returnTargetId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExecutionTarget() {
        return this.executionTarget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionTarget(final String id) throws IllegalStateException {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, FXComponent.class, AStatelessCallbackComponent.class, AEmbeddedComponentWorker.class))
            throw new IllegalStateException(" the execution target can be set only in FXComponents");

        if (id == null) {
            this.executionTarget = "";
            return;
        }
        this.executionTarget = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateExecutionTarget(final String id) throws IllegalStateException {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, FXComponent.class, AStatelessCallbackComponent.class, AEmbeddedComponentWorker.class))
            throw new IllegalStateException(" the execution target can be set only in FXComponents");

        if (id == null) {
            this.executionTarget = "";
            return;
        }
        this.executionTarget = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void invokeFXAndWait(final Runnable r) {
        final Thread t = Thread.currentThread();
        try {
            WorkerUtil.invokeOnFXThreadAndWait(r);
        } catch (InterruptedException | ExecutionException e) {
           t.getUncaughtExceptionHandler().uncaughtException(t,e);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTargetLayout() {
        return this.targetLayout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetLayout(String targetLayout) throws IllegalStateException {
        final String callerClassName = customSecurityManager.getCallerClassName();
        if (!AccessUtil.hasAccess(callerClassName, FXComponent.class, PerspectiveUtil.class))
            throw new IllegalStateException(" the target layout can be set only in FXComponents");
        if (targetLayout == null) throw new IllegalArgumentException("targetLayout should not be null");
        this.targetLayout = targetLayout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JacpContextImpl that = (JacpContextImpl) o;

        if (active.get() != that.active.get()) return false;
        if (executionTarget != null ? !executionTarget.equals(that.executionTarget) : that.executionTarget != null)
            return false;
        if (globalMessageQueue != null ? !globalMessageQueue.equals(that.globalMessageQueue) : that.globalMessageQueue != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (layout != null ? !layout.equals(that.layout) : that.layout != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        if (resourceBundle != null ? !resourceBundle.equals(that.resourceBundle) : that.resourceBundle != null)
            return false;
        if (returnTarget != null ? !returnTarget.equals(that.returnTarget) : that.returnTarget != null) return false;
        return !(targetLayout != null ? !targetLayout.equals(that.targetLayout) : that.targetLayout != null);

    }

    @Override
    public int hashCode() {
        int result = globalMessageQueue != null ? globalMessageQueue.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (returnTarget != null ? returnTarget.hashCode() : 0);
        result = 31 * result + (targetLayout != null ? targetLayout.hashCode() : 0);
        result = 31 * result + (executionTarget != null ? executionTarget.hashCode() : 0);
        result = 31 * result + (layout != null ? layout.hashCode() : 0);
        result = 31 * result + (resourceBundle != null ? resourceBundle.hashCode() : 0);
        result = 31 * result + (active.get() ? 1 : 0);
        return result;
    }
}
