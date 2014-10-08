---
layout: post
title:  „Creating executables with Java 8 & maven
date:   2014-10-8 21:27:27
---
# Creating executable Uber jar’s and native applications with Java 8 & maven

Creating uber jar’s in Java nothing particular new, even creating executable jar’s was also possible with maven long before Java 8. <br/>
With the first release of JavaFX 2, Oracle introduced the javafxpackager tool, which has now been renamed to javapackager (Java 8 u20). This enables developers to create native executables for any common platform, even mac app store packages; the drawbacks of the javapackager are that you must create the executable on the target platform and that it will than contain the whole jre to run the application. This means that your 100kb application gets more than 40mb, depending on your target platform.
<br/><br/>
The first step on your way to an executable uber jar is to build your project and to collect all dependencies in a folder or a fat jar. This folder/fat jar will be the input for the javapackager tool, which creates the executable part.
<br/>
## Solution 1: the maven-dependency-plugin
The maven-dependency-plugin contains the goal “unpack-dependencies”. We can use this goal to enhance the default “target/classes” folder with all the dependencies you need to run your application. We assume that the default maven build creates all classes of your project in the “target/classes” folder and the “unpack-dependencies” plugin copies all the project dependencies to this folder. The result is a valid input for the javapackager tool, which creates the executable. 

<br/>
<div align="center">
![JacpFX Component structure](/img/maven_unpack.png)
</div>
<br/>


The “unpack-dependencies” goal is easy to use; you just need to set the output directory to the “target/classes” folder to ensure everything is together in one folder. A typical configuration looks like this:<br/>

<script src="https://gist.github.com/amoAHCP/403a1949d1e42c936404.js"></script>
<br/>
To create an executable jar from the target/classes directory we use the “exec-maven-plugin” to execute the javapackager commandline tool.<br/>

<script src="https://gist.github.com/amoAHCP/f7f7855991b718e65939.js"></script>
<br/>
Now we can create an executable jar, which contains all the project dependencies and that can simply be executed with “java –jar myApp.jar”.  <br/>
<br/>
In the next step we want to create a native executable or an installer. Specific configuration details for the javapackager can be found here: http://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html , for this tutorial we assume that we want to create a native installer. To do so, we add a second “execution” to your “exec-maven-plugin” like this:<br/>

<script src="https://gist.github.com/amoAHCP/96a8d99df75f05715eb5.js"></script>
<br/>
Once the configuration is done, you can run “mvn clean package” and you will find your executable jar as well as the native installer in your target folder.
<br/>
This solution works fine in most cases, but sometimes you can get in trouble with this plugin. When you have configuration files in your project that also exist in one of your dependencies, the unpack-dependency goal will overwrite your configuration file. For example your project contains a file like “META-INF/service/com.myconf.File” and any dependency does contain the same file. In this case solution 2 may be a better approach.
<br/><br/>

## Solution 2: the maven-shade plugin
The maven-shade plugin provides the capability to package artifacts into an uber-jar, including its dependencies. It also provides various transformers to merge configuration files or to define the MANIFEST of your jar file. This capability allows us to create an executable uber jar without involving the javapackager, so the packager is only needed to create the native executable.

<br/>
<div align="center">
![JacpFX Component structure](/img/maven_shade.png)
</div>
<br/>

To build an executable uber jar following plugin configuration is needed:
<br/>
<script src="https://gist.github.com/amoAHCP/f06e1d458d63ee295a34.js"></script>
<br/>
This configuration defines a main-class entry in the MANIFEST and merges all “META-INF/services/conf.files” together. The resulting executable jar file is now a valid input for the javapacker to create a native installer. The configuration to create a native installer with “exec-maven-plugin” and javapacker tool is exactly the same like in solution 1.
<br/>
I provided two example projects on github https://github.com/amoAHCP/mvnDemos where you can test both configurations. These are two simple projects with a main class starting a jetty webserver on port 8080, so the only dependency is jetty. Both solutions can be used for any type of java projects, even with Swing or JavaFX.







	
