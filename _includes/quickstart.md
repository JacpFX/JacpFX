
# JacpFX Quickstart #
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
The JacpFX quickstart archetype provides a simple JacpFX project with one <i>FXWorbench</i>, two <i>FXPerspective(s)</i> (FXML and JavaFX), two <i>FXComponent(s)</i> (FXML and JavaFX) and two <i>CallbackComponent(s)</i>.

### Requirements ###
Since JacpFX 2, Java 8 and JavaFX 8 is prerequisite.

### Create a project from simple quickstart archetype ###
<pre>mvn archetype:generate  -DarchetypeGroupId=org.jacpfx  -DarchetypeArtifactId=JacpFX-simple-quickstart  -DarchetypeVersion=2.0.2</pre>


To build the project go to project root and type: <pre>mvn packge</pre> 

After the compilation and packaging is finished you may go to the target folder and execute the jar: 
<pre>cd target && java -jar project-name-app.jar</pre>

<br/>

### Create a project from empty quickstart archetype ###
<pre>mvn archetype:generate  -DarchetypeGroupId=org.jacpfx  -DarchetypeArtifactId=JacpFX-empty-quickstart  -DarchetypeVersion=2.0.2</pre>
This archetype creates the default project structure, an application launcher and a Spring configuration class.


## JacpFX from scratch ##
The  goal of the following tutorial is to create a simple JacpFX application similar to the application created by the <i>simple-quickstart archetype</i>

### Create a simple java project ###
To create a JacpFX application from scratch you may use a simple maven java project. To create one, type:
<pre>mvn archetype:create -DgroupId=your.simple.jacpfx.gid -DartifactId=your-simple-jacpfx-aid -DarchetypeArtifactId=maven-archetype-quickstart</pre>

### Add folders and packages ###
Create a resources folder and following subfolders: 
<i>bundles, fxml, styles</i>
in <i>your-simple-jacpfx-aid/src/</i>
<br/>
When finished, go to <i>src/main/java</i>, create a <i>quickstart</i> folder and add <i>workbench, perspective, component, fragment, config, main</i> folders.

### Add the JacpFX dependencies ###
Add following JacpFX dependencies:

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
The launcher project is responsible to create JacpFX component instances. Currently only a Spring launcher is available, so all JacpFX components are simple Spring beans, which means you may use any Spring functionality in you JacpFX application.

```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.JavaFXSpring</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```

### The Spring configuration ###
To start the Spring container we need to create either a spring.xml or a spring configuration class like this:
<script src="https://gist.github.com/amoAHCP/191727abc7841fc1b2bc.js"></script>

Simply put the BasicConfig class in the config packe created before. A best practice is to put all component and perspective ids as static members to this configuration class and to user this members to reference a specific id.
<br/>
### The <i>ApplicationLauncher<i/> ###
The <i>ApplicationLauncher</i> contains the main method, the reference to the <i>FXWorkbench</i> implementation, the Spring configuration file and the package names to scann for JacpFX components. Create an <i>ApplicationLauncher</i> class in the <i>main</i> package.
<script src="https://gist.github.com/amoAHCP/85644f5c0aecb9f026e4.js"></script>

### The FXWorkbench ###
The <i>FXWorkbench</i> is the „root node“ of your JacpFX application. A <i>FXWorkbench</i> creates an application window, defines references to <i>FXPerspective(s)</i> and contains some basic configurations like the initial window size, toolbar definitions and menu definition. Create a <i>JacpFXWorkbench</i> in the <i>workbench</i> package.
<script src="https://gist.github.com/amoAHCP/3623a326e8ff049f9700.js"></script>

### The FXPerspective ###
Next we create a simple JavaFX based <i>FXPerspective</i> called  <i>PerspectiveOne</i> in the <i>perspective</i> package. A perspective defines the basic layout structure of your view, contains references to <i>FXComponent(s)</i> and declares render targets where <i>FXComponent(s)</i> can render their view.
<script src="https://gist.github.com/amoAHCP/018cf84d24baee12a4ea.js"></script>
<i>PerspectiveOne</i> creates a simple view with a <i>SplitPane</i> which contains two <i>GridPanes</i>, both of them registered as a <i>FXComponent</i> render target.(TARGET_CONTAINER_LEFT, TARGET_CONTAINER_MAIN). A <i>FXComponent</i> can now registers itself to be rendered (the view) in one of those areas.

### The FXComponent(s) ###
Now we create two <i>FXComponent(s)</i>, one with a JavaFX view and the other with a FXML view. 

#### The JavaFX FXComponent ####
<script src="https://gist.github.com/amoAHCP/bda85e05ae6cf7a0b9a9.js"></script>

> <i>ComponentLeft</i> registers itself to be rendered in <i>TARGET_CONTAINER_LEFT</i> defined in <i>PerspectiveOne</i>.

#### The FXML FXComponent ####
The next step is to create a <i>FXML</i> file in <i>/resources/fxml/myview.fxml</i> with the following content:

##### The FXML file #####

```xml
<GridPane  hgap="10" vgap="10" minHeight="-Infinity" minWidth="-Infinity"
          xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1" GridPane.hgrow="ALWAYS" GridPane.vgrow="ALWAYS">

    <children>

        <TextArea fx:id="name" text=""/>
				<Button onAction="#sayHello" text="%hello" />
    </children>

</GridPane>
```
<br/>
The corresponding FXController must not implement/return an JavaFX view, instead the root node of your FXML file will be passed as view.
<br/>
##### The FXML FXController #####
<script src="https://gist.github.com/amoAHCP/43751243b35c3b316389.js"></script>

<br/>
Now we have created a minimal JacpFX application which you can execute by starting the <i>ApplicationLauncher</i> class in the main package. This small tutorial contains only basic examples to compose your application UI, for advanced topics like <i>messaging</i> feel free to read the **[documentation](documentation_main.html)** 