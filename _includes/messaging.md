
#JacpFX messaging#
Messaging is an essential part of JacpFX. JacpFX components (workbench, perspectives, components) have no direct reference to each other and communicate via messages.
Every component has a message box and handles it's messages sequential, similar to an actor. 
To send a message the JacpFX context contains following two methods:
### sending a message to yourself ###
<pre>
@Resource
private Context context;   
...    
context.send("message");
</pre>
> This can be interesting when you want to execute methods in a worker thread. (see non blocking UI)

<br/>
### sending a message to a specific component  ###
<pre>
context.send(ComponentIds.COMPONENT_ONE,"message");
</pre> 
<br/>

## Message lifecycle ##
To handle a message, the JacpFX component interfaces defines two methods which will be called sequential:

<pre>
public Node handle(final Message<Event, Object> message) {
        // runs in worker thread
        return null;
    }
</pre> 
<br/>
and
<br/>
<pre>
public Node postHandle(final Node arg0,
                           final Message<Event, Object> message) {
        // runs in FX application thread
        return null;
    }
</pre>

While the "handle" method will be executed in a worker thread, the "postHandle" method is always executed on the FX application thread. For details about the component lifecycle see [the documentation about async execution](nonblocking.html)



