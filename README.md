JacpFX
======

Event bus, message passing and async execution are concepts, getting more and more popular for server side applications. JacpFX brings this approach to the client, combining JavaFX with an actor-like component model. It is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in you client application.

### 09.07.2014 JacpFX RC3 is available in maven.central
To try out RC3 you can simply try the new maven archetype:
<pre>
mvn archetype:generate  -DarchetypeGroupId=org.jacpfx  -DarchetypeArtifactId=JacpFX-simple-quickstart  -DarchetypeVersion=2.0-RC3
</pre>

### 10.03.2014 JacpFX RC2 is available in maven.central
We hope that RC3 will be the last RC release before the final JacpFX 2 verion. We still have a lot to do with documentation and the new project page. If you want to test RC2 you can try the [JacpFX-vertx](https://github.com/amoAHCP/vertx-samples) demo. Simply start two or more clients, create a server in one of the client-apps and connect the other clients to the created server instance.  

### 09.09.2013 We are currently migrating JacpFX from google code to GitHub!
You can find the old sources [here:] (https://code.google.com/p/jacp/)

### 09.09.2013 Github is the new repository for JacpFX development
 - JacpFX 2 will run on top of Java8/JavaFX8
 - will have a new an cleaner API
 - migrating from JacpFX 1.x is easy and strait forward 
