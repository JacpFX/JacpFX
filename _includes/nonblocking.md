
# JacpFX Non Blocking UI #
<br/>
The JacpFX component lifecycle separates the tasks execution and state changes from each other. When a <i>FXComponent</i> receives a message, it first executes a <i>handle</i> method, which is running inside a worker thread. 
In this stage you can execute any long running task or create new JavaFX nodes without blocking the application. When the task execution is finished, the subsequent <i>postHandle</i> method will be executed inside the FX Application thread.
<div align="center">
![component-lifecycle](/img/JacpFX_Non-Blocking_UI.png)
</div>
<br/>
## JacpFX FXComponent ##
<pre>
@View(id = ComponentIds.COMPONENT__TWO,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        initialTargetLayoutId = PerspectiveIds.TARGET__CONTAINER_MAIN)
public class ComponentTwo implements FXComponent {
    private GridPane rootPane;
    @Override
    public Node handle(final Message<Event, Object> message) {
       <b> // runs in worker thread </b>
        ...
        return new GridPane();
    }
    
    @Override
    public Node postHandle(final Node node,
                           final Message<Event, Object> message) {
       <b> // runs in FX application thread </b>
        this.rootPane= node;
        node.getChildren().add(new Label("xyz"));
        return node
    }
 
}
</pre>

## JacpFX Callback-Components ##
<br/>
A JacpFX <i>CallbackComponent</i> has no UI, and therefore no need to switch to the FX Application thread. <i>CallbackComponents</i> can be used to outsource long running tasks, or to coordinate a message flow.
<div align="center">
![component-lifecycle](/img/JacpFX_Non-Blocking_UI_Abb2.png)
</div>

<br/><br/>
A JacpFX <i>CallbackComponent</i> can be either stateful or stateless.

### Stateful Callback-<i>Component</i>: ###
<pre>
@Component(id = ComponentIds.STATEFUL_CALLBACK, name = "statefulCallback", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
public class StatefulCallback implements CallbackComponent {
    private Logger log = Logger.getLogger(StatefulCallback.class.getName());
    @Override
    public Object handle(final Message message) {
        log.info(message.getMessageBody().toString());
        return "StatefulCallback - hello";
    }
 
}
</pre>
<br/>

### Stateless Callback-Component: ###
<pre>
@Component(id = ComponentIds.STATELESS_CALLBACK, name = "statelessCallback", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
<b>@Stateless</b>
public class StatelessCallback implements CallbackComponent {
    private Logger log = Logger.getLogger(StatelessCallback.class.getName());
    @Override
    public Object handle(final Message message) {
        log.info(message.getMessageBody().toString());
        return "StatelessCallback - hello";
    }
 
}
</pre>