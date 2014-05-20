
#JacpFX Non Blocking UI#
<br/>
The JacpFX component-lifecycle separates the tasks execution and state changes from each other. When a component receives a message, it first executes a "handle" method, which is running inside a worker thread. 
In this stage you can execute any long running task or create new JavaFX nodes without blocking the application. When the task execution is finished, the subsequent "postHandle" method will be executed inside the FX Application thread.
<div style="margin:0 10px 10px 0" markdown="1">
![component-lifecycle](/img/AsyncModel.png)
</div>
<br/>
JacpFX Service-Components have no UI, and therefore no need to switch to the FX Application thread. Service-Components can be used to outsource long running tasks, or to coordinate a message flow.


![component-lifecycle](/img/AsyncModel_Service.png)
<br/>