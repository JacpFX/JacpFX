
#JacpFX Non Blocking UI#
<br/>
The JacpFX component-lifecycle separates the tasks execution and state changes from each other. When a component receives a message, it first executes a "handle" method, which is running inside a worker thread. 
In this stage you can execute any long running task or create new JavaFX nodes without blocking the application. When the task execution is finished, the subsequent "postHandle" method will be executed inside the FX Application thread.

![component-lifecycle](/img/AsyncModel.png)

