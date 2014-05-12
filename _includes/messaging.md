
#JacpFX messaging#
Messaging is an essential part of JacpFX. All JacpFX components (workbench, perspectives, components) are loosely coupled and have no direct reference to each other. 
All the communication between components is handled trough messaging, which is changing the state of a component. Every component has a message box and handles it's messages sequential similar to an actor. 
To send a message the JacpFX context contains following two methods:
### sending a message to yourself ###
<pre>
context.send("message");
</pre>
> This can be interesting when you want to execute methods in a worker thread. (see non blocking UI)

<br/>
### sending a message to component one ###
<pre>
context.send(ComponentIds.COMPONENT_ONE,"message");
</pre> 




