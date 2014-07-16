
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

## JacpFX from scratch ##