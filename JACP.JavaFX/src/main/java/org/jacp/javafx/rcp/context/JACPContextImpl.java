package org.jacp.javafx.rcp.context;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.action.IActionListener;
import org.jacp.api.util.CustomSecurityManager;
import org.jacp.javafx.rcp.action.FXAction;
import org.jacp.javafx.rcp.action.FXActionListener;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.components.managedDialog.JACPManagedDialog;
import org.jacp.javafx.rcp.components.managedDialog.ManagedDialogHandler;
import org.jacp.javafx.rcp.components.modalDialog.JACPModalDialog;

import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 24.06.13
 * Time: 21:36
 * JACP context object provides functionality to components context and basic features.
 */
public class JACPContextImpl implements JACPContext {

    protected volatile BlockingQueue<IAction<Event, Object>> globalMessageQueue;
    private String id;
    private volatile String parentId;

    private String name;
    private volatile String returnTarget;
    private volatile String targetLayout;
    private volatile String executionTarget="";
    private final static CustomSecurityManager customSecurityManager =
            new CustomSecurityManager();
    private FXComponentLayout layout;
    private ResourceBundle resourceBundle;

    private volatile boolean active;

    public JACPContextImpl(final String id, final String name, final BlockingQueue<IAction<Event, Object>> globalMessageQueue) {
        this.id = id;
        this.name = name;
        this.globalMessageQueue = globalMessageQueue;

    }

    public JACPContextImpl(final BlockingQueue<IAction<Event, Object>> globalMessageQueue) {
        this.globalMessageQueue = globalMessageQueue;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IActionListener<EventHandler<Event>, Event, Object> getActionListener(
            final Object message) {
        return new FXActionListener(new FXAction(this.id, message),
                this.globalMessageQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IActionListener<EventHandler<Event>, Event, Object> getActionListener(
            final String targetId, final Object message) {
        return new FXActionListener(new FXAction(this.id, targetId, message, null),
                this.globalMessageQueue);
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
    public String getParentId() {
        return this.parentId;
    }

    public final void setParentId(final String parentId) {
          this.parentId = parentId;
    }

    public final void setId(final String id) {
        this.id = id;
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
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return this.active;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
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
    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionTarget(String id) {
        if(id==null){
            this.executionTarget ="";
            return;
        }
        this.executionTarget = id;
    }

    public String getExecutionTarget() {
        return this.executionTarget;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetLayout(String targetLayout) {
        if(targetLayout==null) throw new IllegalArgumentException("targetLayout should not be null");
        this.targetLayout = targetLayout;
    }

    public final String getTargetLayout(){
        return this.targetLayout;
    }
}
