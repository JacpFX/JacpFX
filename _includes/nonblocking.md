
#JacpFX Non Blocking UI#
<br/>
The JacpFX component-lifecycle separates the tasks execution and state changes from each other. When a component receives a message, it first executes a "handle" method, which is running inside a worker thread. 
In this stage you can execute any long running task or create new JavaFX nodes without blocking the application. When the task execution is finished, the subsequent "postHandle" method will be executed inside the FX Application thread.
<div align="center">
![component-lifecycle](/img/JacpFX_Non-Blocking_UI.png)
</div>
<br/>
##JacpFX Callback-Components##
<br/>
JacpFX Callback-Components have no UI, and therefore no need to switch to the FX Application thread. Callback-<i>Components</i> can be used to outsource long running tasks, or to coordinate a message flow.
<div align="center">
![component-lifecycle](/img/JacpFX_Non-Blocking_UI_Abb2.png)
</div>

<br/><br/>
A JacpFX Callback-<i>Components</i> can be either stateful or stateless.

## Stateful Callback-<i>Component</i>: ##
<pre>
@Component(id = ComponentIds.STATEFUL_CALLBACK, name = "statefulCallback", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
public class StatefulCallback implements CallbackComponent {
	private Logger log = Logger.getLogger(StatefulCallback.class.getName());
    @Override
    public Object handle(final Message<Event, Object> message) {
        log.info(message.getMessageBody().toString());
		return "StatefulCallback - hello";
	}

}
</pre>
<br/>
## Stateless Callback-Component: ##
<pre>
@Component(id = ComponentIds.STATELESS_CALLBACK, name = "statelessCallback", active = true, resourceBundleLocation = "bundles.languageBundle", localeID = "en_US")
<b>@Stateless</b>
public class StatelessCallback implements CallbackComponent {
	private Logger log = Logger.getLogger(StatelessCallback.class.getName());
	@Override
    public Object handle(final Message<Event, Object> message) {
		log.info(message.getMessageBody().toString());
		return "StatelessCallback - hello";
	}

}

</pre>