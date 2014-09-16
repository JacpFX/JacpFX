JacpFX
======

Event bus, message passing and async execution are concepts, getting more and more popular for server side applications. JacpFX brings this approach to the client, combining JavaFX with an actor-like component model. It is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in you client application.

### 16.09.2014 JacpFX 2.0.1 is available in maven.central
We released the version 2.0.1 which is mainly a bugfix release. On main issue was fixed when running a JacpFX project on windows with Eclipse or Netbeans, in this case the application start failed. One additional add-on was released with JacpFX 2.0.1, a minimal launcher. So when Spring is not needed in your application you can switch to the minimal launcher and reduce the dependencies.

### 18.07.2014 JacpFX RC4 is available in maven.central
RC4 release was deployed on maven central. The target for the final release is Aug. 1, so we may release one more RC5 if necessary (hopefully not). We also released a beat for the new project page: http://jacpfx.org . Here you can find the documentation, tutorials and the new blog.

### 09.07.2014 JacpFX RC3 is available in maven.central
To try out RC3 you can simply use the new (simple) maven archetype:
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
