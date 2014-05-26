
#JacpFX Documentation --- WORK IN PROGRESS --- #
This documentation pages gives you detailed informations about all parts of JacpFX; how to bootstrap a new JacpFX application and the general usage. You may want to read the [quick-start tutorial](http://) to start with a JacpFX project directly.
## What it is##
JacpFX is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in you client application. JacpFX focusing on following goals to deliver best developer- and user-experience:

* Simple structuring of loosely coupled UI components
* Simple communication between components through a message-bus
* Support of asynchronous processes to avoid blocking UIs
* Lightweight (size and memory footprint)

## General structure ##
JacpFX has, like any other UI application framework, a hierarchic component structure to create client applications.

![JacpFX Component structure](http://jacp.googlecode.com/svn/wiki/JACP_Overview_v1.png "JacpFX Component structure") 

A JacpFX application consists of following components:

* An **[ApplicationLauncher](#ApplicationLauncher)**, which contains any configurations to bootstrap the application as well as the application main method.
* A **[Workbench](#workbench)**, this is the root Node of the client application. He basically contains any perspective constraints and some application specific configurations.
* At least one **[Perspective](#perspective)** to define the basic layout of your view
* **[UI Components](#components)**, to define the contents in a perspective
* **[UI Fragments](#fragments)**, to define parts of your UI in a component, this allows you to seperate one component in more fine-grained parts
* **[Stateful/Stateless service Components](#services)**, non UI service components for task execution and communication with external systems.

##Project structure##
JacpFX projects have a typical maven (Java) project structure.
###Dependencies###
#### JacpFX.API ####
#### JacpFX.JavaFX ####
#### JacpFX.JavaFXControls ####
#### JacpFX.JavaFXSpring ####
##Configuration##
<br/>

## <a name=ApplicationLauncher></a>ApplicationLauncher 
The ApplicationLauncher is the entry point to bootstrap a JacpFX application. It contains the main method, the configuration for component scanning, the managed container configuration and the reference to the workbench class.
JacpFX defines an Launcher interface which is currently using Spring as the managed container. A CDI implementation is planned, but not available yet. For the Spring implementation there are two abstract launcher implementations available:

- The AFXSpringXmlLauncher
- The AFXSpringJavaConfigLauncher

### AFXSpringXmlLauncher example ###

<pre>
public class ApplicationLauncher extends AFXSpringXmlLauncher {


    @Override
    public String getXmlConfig() {
        return "main.xml";
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return JacpFXWorkbench.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"my.project.quickstart"};
    }


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void postInit(Stage stage) {
	...
    }

}

</pre>

> The "getXMLConfig()" methods returns the name of your spring configuration xml, which is located in resources folder.

<br/>
### AFXSpringXmlLauncher example ###

<pre>
public class ApplicationLauncher extends AFXSpringJavaConfigLauncher {

    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class<?>[]{BaseConfiguration.class};
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return JacpFXWorkbench.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"my.project.quickstart"};
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void postInit(Stage stage) {
	...
    }
}
</pre>

> The "getConfigClasses()" returns an array with all valid spring configuration classes (annotated with @Configuration)
<br/>

### Common applicationLauncher methods ###
#### getWorkbenchClass ####
Returns the defined Workbench class
<br/>

#### getBasePackages ####
Define all packages to scan for components and perspectives. JacpFX uses component scanning to resolve all components and perspectives by ID.
<br/>

#### postInit ####
This method gives you access to the JavaFX stage. You can e.g. define stylesheet for you application.

## <a name=workbench></a>Workbench ##
The workbench is the root node of your client project, providing simple interfaces to configure the basic behavior of your client. Besides the application launcher, it is the only component where you can get direct access to the JavaFX "stage". 
Furthermore a workbench logically groups all perspectives defined in the @workbench annotation. 
### Declare references to perspectives ###
### Set window style ###
### Set workbench size ###
### Enable menus ###
### Enable toolbars ###
<br/>
## <a name=perspective></a>Perspective 
### The perspective lifecycle ###
### Perspective types ###
#### Programmatic Perspectives ####
#### Declarative Perspectives ####
<br/>
## <a name=components></a>Components
### UI Components ###
#### component lifecycle ###
<br/>
### Callback Components ###
<br/>
## <a name=fragments></a>Fragments
<br/>
## <a name=services></a>Service components
<br/>
##JacpFX messaging##
### The message interface ###
<br/>
### The JacpFX Context ###

##modal dialogs##

##toolbar and menubar##

##localisation and internationalisation##

##resources##

##annotations overview##

###class-level annotations###

###method-level annotations###

 
