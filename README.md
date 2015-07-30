JacpFX
======

Event bus, message passing and async execution are concepts, getting more and more popular for server side applications. JacpFX brings this approach to the client, combining JavaFX with an actor-like component model. It is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in you client application.

### 28.07.2015 JacpFX 2.1-RC1 is available in maven.central
After a longer period without any releases I am pleased to announce JacpFX 2.1-RC1. The JacpFX2.1 release is fixing some issues, adding some minor enhancements and we did a lot of cleanups and optimizations under the hood. If no major issues in RC1 can be found we plan to have a RC2 release in about 4 Weeks before going live. Please take a look at the fixed issues and enhancements, test it and please give feedback on any issue you can find.

#### Fixed issues:
- OnShow method not called on application start ( https://github.com/JacpFX/JacpFX/issues/33 )
	- the lifecycle was fixed so for perspectives now @PostConstruct -> @OnShow (if it’s the current visible) -> @OnHide (if it was hided) -> @Predestroy is valid
- Let ClassFinder work in Gradle (https://github.com/JacpFX/JacpFX/issues/32)
	- JacpFX should work with Gradle out-of-the-box
- Inaccurate exception (https://github.com/JacpFX/JacpFX/issues/31)
	- Exceptions while loading the FXML should be now more meaningful
- CSS Error parsing (https://github.com/JacpFX/JacpFX/issues/28)
- ToolBar button management fails on two perspectives when second is initially inactive (https://github.com/JacpFX/JacpFX/issues/11)
- and more….

#### Some enhancements:
- You can now provide your own WorkBenchLayout
	- If you dislike the default Layout implementation with placeholders for ToolBars an so on you can implement your own (more mobile friendly ?)
	- Just implement the WorkbenchDecorator interface and register you implementation in the ApplicationLauncher by overriding the WorkbenchDecorator getWorkbenchDecorator() method. If you have issues with the new default decorator, the old one is still available as DefaultLegacyWorkbenchDecorator.class
- The JacpContext provides now the fullQualified name
- Fluent concurrency API provided by JacpContext (https://github.com/JacpFX/JacpFX/issues/26)
- Provide SPI for persisting events (https://github.com/JacpFX/JacpFX/issues/23)
	- implement the MessageLogger interface, create a file called org.jacpfx.api.message.MessageLogger in META-INF/service and add your implementation

<dependency>
	<groupId>org.jacpfx</groupId>
	<artifactId>jacpfx.API</artifactId>
	<version>2.1-RC1</version>
</dependency>







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
