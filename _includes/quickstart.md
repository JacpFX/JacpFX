
#JacpFX Quickstart#
JacpFX projects have a typical maven project structure.
<pre>
root
  |
  src
  | |
  | main
  |   |
  |	  java
  |	  |
  |	resources
  |	   |
  |    bundles (Resource bundles)
  |	   |
  |	   fxml (FXML files)
  |	   |
  |	   styles (CSS files)
  |
  pom.xml  
</pre>  
<br/>

To start a new JacpFX project you may use a simple Java archetype or one of the JacpFX archetypes.

## JacpFX maven quickstart ##
The JacpFX quickstart archetype provides a simple JacpFX project containing a Workspace, two <i>FXPerspective(s)</i> (FXML and JavaFX), two <i>FXComponent(s)</i> (FXML and JavaFX) and two <i>CallbackComponent(s)</i>.

### Requirements ###
Since JacpFX 2, Java 8 and JavaFX 8 is prerequisite.

### Create a project from quickstart archetype ###
<pre>mvn archetype:generate  -DarchetypeGroupId=org.jacpfx  -DarchetypeArtifactId=JacpFX-simple-quickstart  -DarchetypeVersion=2.0-RC4</pre>


To build the project go to project root and type: <pre>mvn packge</pre> 

After the compilation and packaging is finished you may go to the target folder and execute the jar: 
<pre>cd target && java -jar project-name-app.jar</pre>

<br/>

## JacpFX from scratch ##
The  goal of the following tutorial is to create a simple JacpFX application similar to the application created by the <i>simple-quickstart archetype</i>

### Create a simple java project ###
To create a JacpFX application from scratch you may use a simple maven java project. To create on type:
<pre>mvn archetype:create -DgroupId=your.simple.jacpfx.gid -DartifactId=your-simple-jacpfx-aid -DarchetypeArtifactId=men-archetype-quickstart</pre>

### Add folders and packages ###
After the project was created go to <i>cd your-simple-jacpfx-aid/src/</i>, create a resources folder and following subfolders: <i>bundles, fxml, styles</i>
<br/>
After doing this go to the <i>src/main/java</i> folder, create a <i>quickstart</i> folder and add <i>workbench, perspective, component, fragment, config, main</i> folders.

### Add the JacpFX dependencies ###
A JacpFX projects depends on following projects:

#### The JacpFX API ####
```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.API</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```
#### The JacpFX implementation ####
```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.JavaFX</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```
#### The JacpFX controls ####
```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.JavaFXControls</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```
#### The JacpFX launcher ####
The launcher project is responsible to launch all JacpFX components. Currently only a Spring launcher is available, so all JacpFX components are simple Spring beans, which means you may use any Spring functionality in you JacpFX application.

```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.JavaFXSpring</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```
### The application launcher ###
The application launcher contains the main method and some configurations to launch a JacpFX application. Before we create one, we create a Spring configuration class which is needed for a Spring application launcher.

#### The Spring configuration ####
<script src="https://gist.github.com/amoAHCP/191727abc7841fc1b2bc.js"></script>

Simply put the BasicConfig class in the config packe created before. A best practice is to put all component and perspective ids as static members to this configuration class and to user this members to reference a specific id.
<br/>
#### The Application launcher ###
The <i>ApplicationLauncher</i> contains the reference to the <i>FXWorkbench</i>, the Spring configuration file and the packages to scann for JacpFX components. Create an <i>ApplicationLauncher</i> class in the <i>main</i> package.
<script src="https://gist.github.com/amoAHCP/85644f5c0aecb9f026e4.js"></script>

### The FXWorkbench ###
The <i>FXWorkbench</i> is the „root node“ of your JacpFX application. The workbench creates the application window, defines references to perspective and contains some basic configurations like the initial window size, toolbar definitions and menu definition. Create a <i>JacpFXWorkbench</i> in the <i>workbench</i> package.
<script src="https://gist.github.com/amoAHCP/3623a326e8ff049f9700.js"></script>

### The FXPerspective ###
Next we create a simple JavaFX based <i>FXPerspective</i> called  <i>PerspectiveOne</i> in the <i>perspective</i> package. A perspective defines the basic layout of your view, contains references to components and declares render targets where components can render their view.
<script src="https://gist.github.com/amoAHCP/018cf84d24baee12a4ea.js"></script>
<i>PerspectiveOne</i> creates a simple view with a <i>SplitPane</i> which contains two <i>GridPanes</i>, both of them registered as a <i>FXComponent</i> render target.(TARGET_CONTAINER_LEFT, TARGET_CONTAINER_MAIN). A <i>FXComponent</i> can now registers itself to be rendered in one of those areas.

### The FXComponent(s) ###

