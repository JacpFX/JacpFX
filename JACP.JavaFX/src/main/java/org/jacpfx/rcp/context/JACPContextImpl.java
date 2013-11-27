package org.jacpfx.rcp.context;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.message.ActionListener;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.CustomSecurityManager;
import org.jacpfx.rcp.message.FXActionListener;
import org.jacpfx.rcp.message.FXMessage;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.components.managedDialog.JACPManagedDialog;
import org.jacpfx.rcp.components.managedDialog.ManagedDialogHandler;
import org.jacpfx.rcp.components.modalDialog.JACPModalDialog;

import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 24.06.13
 * Time: 21:36
 * JACP context object provides functionality to components context and basic features.
 */
public class JACPContextImpl implements JACPContext {

    private final static CustomSecurityManager customSecurityManager =
            new CustomSecurityManager();
    private volatile BlockingQueue<Message<Event, Object>> globalMessageQueue;
    /**
     * will be set on init
     */
    private String id;
    private volatile String parentId;
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

    public JACPContextImpl(final String id, final String name, final BlockingQueue<Message<Event, Object>> globalMessageQueue) {
        this.id = id;
        this.name = name;
        this.globalMessageQueue = globalMessageQueue;

    }

    public JACPContextImpl(final BlockingQueue<Message<Event, Object>> globalMessageQueue) {
        this.globalMessageQueue = globalMessageQueue;

    }

    /**
     * {@inheritDoc}
     */
    @Override        // TODO check access, workbench is not allowed to use this method
    public final ActionListener<Event, Object> getActionListener(
            final Object message) {
        return new FXActionListener(new FXMessage(this.id, message),
                this.globalMessageQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ActionListener<Event, Object> getActionListener(
            final String targetId, final Object message) {
        return new FXActionListener(new FXMessage(this.id, targetId, message, null),
                this.globalMessageQueue);
    }


    @Override
    public final void send(final String targetId, final Object message) {
        try {
            this.globalMessageQueue.put(new FXMessage(this.id, targetId, message, null));
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    // TODO check access, workbench is not allowed to use this method
    public final void send(final Object message) {
        try {
            this.globalMessageQueue.put(new FXMessage(this.id, message));
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

    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParentId() {
        return this.parentId;
    }

    public final void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

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
    public <T> ManagedDialogHandler<T> getManagedDialogHandler(final Class<T> clazz) {
        // TODO check if call is from UI component, otherwise throw exception
        final String callerClassName = customSecurityManager.getCallerClassName();
        return JACPManagedDialog.getInstance().getManagedDialog(clazz, callerClassName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showModalDialog(final Node node) {
        JACPModalDialog.getInstance().showModalDialog(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideModalDialog() {
        JACPModalDialog.getInstance().hideModalDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FXComponentLayout getComponentLayout() {
        return this.layout;
    }

    public void setFXComponentLayout(final FXComponentLayout layout) {
        this.layout = layout;
    }

    /**
     * Returns component id which is targeted by bg component return value; the
     * return value will be handled like an average message and will be
     * delivered to targeted component.
     *
     * @return the target id
     */
    // TODO check that only callables can call this
    public final String getReturnTargetAndClear() {
        String returnVal = String.valueOf(this.returnTarget);
        this.returnTarget = null;
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setReturnTarget(final String returnTargetId) {
        this.returnTarget = returnTargetId;
    }

    public String getExecutionTarget() {
        return this.executionTarget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionTarget(String id) {
        if (id == null) {
            this.executionTarget = "";
            return;
        }
        this.executionTarget = id;
    }

    public final String getTargetLayout() {
        return this.targetLayout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetLayout(String targetLayout) {
        if (targetLayout == null) throw new IllegalArgumentException("targetLayout should not be null");
        this.targetLayout = targetLayout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JACPContextImpl that = (JACPContextImpl) o;

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
