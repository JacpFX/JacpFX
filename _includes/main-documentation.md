
#JacpFX Documentation --- WORK IN PROGRESS --- #
This documentation pages gives you detailed informations about all parts of JacpFX; how to bootstrap a new JacpFX application and the general usage. You may want to read the [quick-start tutorial](documentation_quickstart.html) to start with a JacpFX project directly.
## What it is##
JacpFX is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in you client application. JacpFX focusing on following goals to deliver best developer- and user-experience:

* Simple structuring of loosely coupled UI components
* Simple communication between components through a message-bus
* Support of asynchronous processes to avoid blocking UIs
* Lightweight (size and memory footprint)


## General structure ##
JacpFX has, like any other UI application framework, a hierarchic component structure to create client applications.

<br/>
<div align="center">
![JacpFX Component structure](/img/JACP_Overview_v2.png)
</div>
<br/>


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
### AFXSpringJavaConfigLauncher example ###

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
Returns the defined Workbench class.
<br/>

#### getBasePackages ####
Define all packages to scan for components and perspectives. JacpFX uses component scanning to resolve all components and perspectives by ID.
<br/>

#### postInit ####
This method gives you access to the JavaFX stage. You can e.g. define stylesheet for you application.

## <a name=workbench></a>Workbench ##
The workbench is the root node of your client project, providing simple interfaces to configure the basic behavior of your client. Besides the application launcher, it is the only component where you can get direct access to the JavaFX "stage". 
Furthermore a workbench logically groups all perspectives defined in the @workbench annotation.
 
### Example workbench ###
<br/>
<pre>
@Workbench(id = "id1", name = "workbench",
        perspectives = {
                BaseConfiguration.PERSPECTIVE_TWO,
                BaseConfiguration.PERSPECTIVE_ONE
        })
public class JacpFXWorkbench implements FXWorkbench {
    @Override
    public void handleInitialLayout(final Message<Event, Object> action,
                                    final WorkbenchLayout<Node> layout, final Stage stage) {
        layout.setWorkbenchXYSize(1024, 768);
        layout.registerToolBar(ToolbarPosition.NORTH);
        layout.setStyle(StageStyle.DECORATED);
        layout.setMenuEnabled(false);
    }
    @Override
    public void postHandle(final FXComponentLayout layout) {
    ...
    }
}
</pre>
<br/>

The workbench interface defines two method:

- handleInitialLayout
- postHandle
<br/>

### The handleInitialLayout method ###
This method is the first one which will be called on application start. It allows to do a basic configuration of you application. The method signature defines three parameter:

- Message<Event,Object> action : the initial message, see **[JacpFX messaging](#messaging)**

- WorkbenchLayout<Node> layout (the configuration handler to define following application values): 
	- layout.setWorkbenchXYSize(x,y) : define the initial workbench size
	- layout.registerToolBar(ToolbarPosition.NORTH): activate toolbars (NORTH, SOUTH, EAST, WEST)
	- layout.setStyle(StageStyle.DECORATED): enable/disable window decoration 
	- layout.setMenuEnabled(false): enable/disable application menues
	
- Stage: the JavaFX "Stage" object

<br/>
### The postHandle method ###

The postHandle method will be executed after the configuration in the "handleInitialLayout" method was done. Depending on the configured toolbars and menus you can add global toolbar/menue entries to you application here.
The FXComponentLayout interface defines following methods:

- layout.getRegisteredToolBar(ToolbarPosition.NORTH) : returns the (NORTH, SOUTH, EAST, WEST) toolbar
- layout.getRegisteredToolBars() : returns all registered toolbars
- layout.getMenu(): returns the application menu

To get detailled informations about toolbars, see **[Toolbars](#toolbars)**

<br/>
### Declare references to perspectives ###
To declare references to perspectives, simply add the perspective ID's in the "perspective" attribute located in the "@Workbench" annotation. The component scanning tries to find the corresponding perspective implementation in the classpath, so the implementations do not need to be located in the same project as the workbench.
<br/>
<pre>
@Workbench(id = "id1", name = "workbench",
      <b>  perspectives = {
                BaseConfiguration.PERSPECTIVE_TWO,
                BaseConfiguration.PERSPECTIVE_ONE
        }) </b>
</pre>

##<a name=perspective></a>Perspective##

A perspective defines the basic UI structure for your view and provides a container for components. 
While a perspective is more like a template with placeholders (or a portal page), components are the detail views of your application (or the portlets).
<br/>
A typical UI application has a root node and a large tree of components which represents your application UI. The leaf nodes of such a component-tree are your user-defined controles like Buttons, TextFields and so on.  In a typical business application you can create a (Split-)Pane in your perspective, which represents the the root node of your current view, place a Pane on the left and on the right and register those Panes as “targets” for your components. Child components of your perspective can now registers themselves to be rendered in one of those targets.
<br/>
<div align="center">
![perspective node tree](/img/JACP_NodeTree_View.png)
</div>
<br/>


### The perspective lifecycle ###
A perspective defines five lifecycle hooks:

- The <b>"handlePerspective"</b> method must be overwritten and will be executed on each message the perspective is receiving.
- <b>@PostConstruct:</b> A method annotated with @PostConstruct will be executed when a perspective will be activated, usually this happens on start 
- <b>@PreDestroy:</b> A method annotated with @PreDestroy will be executed when a perspective will be destroyed
- <b>@OnShow:</b> A method annotated with @OnShow will be executed when an active perspective gets the focus. Only one perspective is visible in a workbench at the same time, when a perspective gets a message it will be focused and placed in the foreground.
	- in this phase you can e.g. turn on toolbar buttons or start timer tasks
- <b>@OnHide:</b> A method annotated with @OnHide will be executed when an active perspective looses the focus and moved to the background.


<br/>
<div align="center">
![perspective lifecycle](/img/JACP_Perspective_Lifecycle.png)
</div>
<br/>

### Perspective types ###
Perspectives can be written either <b>programmatically</b> in plain JavaFX or <b>declarative</b>, with an FXML view.
<br/> 
#### Programmatic Perspectives ####

Programmatic perspectives declare their view in plain JavaFX. You can create any complex UI tree, but you have to register the root node of you UI tree which will than be added to the workbench.

<pre>
@Perspective(id = BaseConfiguration.PERSPECTIVE_ONE, name = "PerspectiveOne",
        components = {...},
        resourceBundleLocation = "bundles.languageBundle")
public class PerspectiveOne implements FXPerspective {


    @Override
    public void handlePerspective(final Message<Event, Object> message,
                                  final PerspectiveLayout perspectiveLayout) { ... }

    @OnShow
    public void onShow(final FXComponentLayout layout) { ... }
    
    @OnHide
    public void onHide(final FXComponentLayout layout) { ... }

    @PostConstruct
    public void onStartPerspective(final PerspectiveLayout perspectiveLayout, final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
                                   
       <b> // define the perspective view with JavaFX </b>  
		BorderPane mainPane = new BorderPane();
        LayoutUtil.GridPaneUtil.setFullGrow(ALWAYS, mainPane);

        SplitPane mainLayout = new SplitPane();
        mainLayout.setOrientation(Orientation.VERTICAL);
        mainLayout.setDividerPosition(0, 0.55f);
        mainPane.setCenter(mainLayout);

        HBox contentTop = new HBox();
        HBox contentBottom = new HBox();

        mainLayout.getItems().addAll(contentTop, contentBottom);
		<b>
       // Register root component
        perspectiveLayout.registerRootComponent(mainPane);
        </b>
		...
    }

    @PreDestroy
    public void onTearDownPerspective(final FXComponentLayout arg0) { ... }
    
</pre>
#### Declarative Perspectives ####
Declarative perspectives provides their view by defining a FXML file representing the view. The root node is always the root of your FXML and will be automatically registered.

<pre>
@Perspective(id = PerspectiveIds.PERSPECTIVE_TWO, name = "PerspectiveTwo",
        components = {},
        <b>viewLocation = "/fxml/perspectiveOne.fxml",</b>
        resourceBundleLocation = "bundles.languageBundle")
public class PerspectiveTwo implements FXPerspective {

    @FXML
    private HBox contentTop;
    @FXML
    private HBox contentBottom;
    @FXML
    private BorderPane mainPane;


    @Override
    public void handlePerspective(final Message<Event, Object> message,
                                  final PerspectiveLayout perspectiveLayout) { ... }

    @OnShow
    public void onShow(final FXComponentLayout layout) { ... }
    
    @OnHide
    public void onHide(final FXComponentLayout layout) { ... }

    @PostConstruct
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) { ... }

    @PreDestroy
    public void onTearDownPerspective(final FXComponentLayout arg0) { ... }

}

</pre>

####The FXML view:####

```xml
<BorderPane id="mainPane"
            xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
    <center>
        <SplitPane fx:id="mainLayout" dividerPositions="0.55" focusTraversable="true"
                   orientation="VERTICAL" HBox.hgrow="ALWAYS">
            <items>
                <HBox fx:id="contentTop"/>
                <HBox fx:id="contentBottom"/>
            </items>
        </SplitPane>
    </center>
</BorderPane>
```

### Register components ###
Component-references are defined inside the @Perspective annotation. This defines the initial state of a perspective after startup; once the application is started, you can move components from one perspective to an other. 
Component references are subjected to one simple rule: components are <b>ALWAYS unique per perspective</b>, you can't add the same component twice in one perspective but you can use one component in many perspectives. Each component is a singleton per perspective, this means the a second perspective will get a different component instance as the first perspective.
#### Definition of components-references####
<pre>
@Perspective(id = BaseConfiguration.PERSPECTIVE_ONE, name = "PerspectiveOne",
        <b>components = {ComponentIds.ONE,ComponentIds.TWO,ComponentIds.THREE},</b>
        resourceBundleLocation = "bundles.languageBundle")
public class PerspectiveOne implements FXPerspective {
		...
    }   
</pre>

<br/>

### Register render-targets ###
Render-targets are areas in your perspective where component-views can be rendered. You can register any node of your perspective-view (except the root-node) to be a render-target. A child component of your perspective can now register itself to be rendered in this node.
#### Definition of render-targets####
<pre>
@Perspective(id = BaseConfiguration.PERSPECTIVE_ONE, name = "PerspectiveOne",
        components = {...},
        resourceBundleLocation = "bundles.languageBundle")
public class PerspectiveOne implements FXPerspective {
   ...

    @PostConstruct
    public void onStartPerspective(final PerspectiveLayout perspectiveLayout, final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {                                  
		...

        HBox contentTop = new HBox();
        HBox contentBottom = new HBox();

        mainLayout.getItems().addAll(contentTop, contentBottom);
		<b>
       // register top menu
        perspectiveLayout.registerTargetLayoutComponent(BaseConfiguration.TARGET_CONTAINER_TOP, contentTop);
        // register bottom content
        perspectiveLayout.registerTargetLayoutComponent(BaseConfiguration.TARGET_CONTAINER_MAIN, contentBottom);
        </b>
		...
    }   
</pre>

<br/>

### The @Perspective annotation ###
The @Perspective annotation provides necessary meta-informations for all classes implementing the FXPerspective interface. Following attributes describes a JacpFX perspective:

- name (mandatory): The perspective name
- id (mandatory): The perspective id
- components (mandatory): all referenced component id's
- active (optional): The state of the perspective. The default value is "true", if the value is set to "false" the perspective will be activated when it receives the first message.
- viewLocation (optional): The path to the FXML file, if this attribute is set, the perspective will be handled as a declarative perspective.
- resourceBundleLocation (optional): The path to your resource bundle.
- localeID (optional): The default locale, if not set the system default will be used.

## <a name=components></a>Components ##
While perspectives helping you to structure you application, components are more like "micro" applications or portlets. You can simply create master-detail views and reuse both parts (components) in different contextes. Basically JacpFX components are distinguished in UI- and NonUI-Components;
UI-Components contain your complex UI (e.g Form) and Controls like "TextField" or "Button". NonUI-Components are ment to be services for long running tasks. All components in common is, that they have a “handle” method that is <b>running outside the FX application thread</b>, so the execution of this method will not block the rest of your UI.
### UI-Components ###
The purpose of UI-Components is to create UI parts or views in plain JavaFX or FXML (similar to views or editors in other RCP frameworks). UI-Components must implement the "FXComponent" interface, they represent a controller class which returns a view either in plain JavaFX or FXML. 
While the explicit return value of a JavaFX component is a (JavaFX) Node, which will be included in the parent perspective; FXML Components passes the root-node of their FXML file directly to the parent perspective.

#### The FXComponent lifecycle ####

#### The FXComponent interface ####
The FXComponent interface defines following two methods to implement:

#### Method-level annotations ####


#### component lifecycle ###
<br/>
### Callback Components ###
<br/>
## <a name=fragments></a>Fragments
<br/>
## <a name=services></a>Service components
<br/>
## <a name=messaging></a>JacpFX messaging##
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

 
