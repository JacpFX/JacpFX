
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

### create a simple java project ###
To create a JacpFX application from scratch you may use a simple maven java project. To create on type:
<pre>mvn archetype:create -DgroupId=your.simple.jacpfx.gid -DartifactId=your-simple-jacpfx-aid -DarchetypeArtifactId=men-archetype-quickstart</pre>

### add folders and packages ###
After the project was created go to <i>cd your-simple-jacpfx-aid/src/</i>, create a resources folder and following subfolders: <i>bundles, fxml, styles</i>
<br/>
After doing this go to the <i>src/main/java</i> folder and add <i>workbench, perspective, component, fragment, config, main</i>

### add the JacpFX dependencies ###
A JacpFX projects depends on following projects:

#### the JacpFX API ####
```xml
<dependency>
    <groupId>org.jacpfx</groupId>
    <artifactId>jacpfx.API</artifactId>
    <version>${jacp.version}</version>
    <scope>compile</scope>
</dependency>
```
