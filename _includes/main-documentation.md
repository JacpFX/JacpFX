
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

* An **[ApplicationLauncher](#ApplicationLauncher)**, which contains any configurations to bootstrap the application, as well as the application main method.
* A **[Workbench](#workbench)**, this is the root Node of the client application. He basically contains the perspectives and application specific configurations.
* At least one **[Perspective](#perspective)** to define the basic layout of your view
* **[UI Components](#components)**, to define the contents in a perspective
* **[UI Fragments](#fragments)**, to define parts of your UI, this allows you to split the component view in more fine-grained managed controls.
* **[Stateful/Stateless service Components](#services)**, non UI service-components for task execution and communication with external systems.

##Project structure##
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

##Dependencies##
#### JacpFX.API ####
#### JacpFX.JavaFX ####
#### JacpFX.JavaFXControls ####
#### JacpFX.JavaFXSpring ####
<br/>

## <a name=ApplicationLauncher></a>ApplicationLauncher 
An ApplicationLauncher contains the main method, the component-scanning configuration, the managed container configuration and the reference to the workbench class.
<br/>JacpFX defines a Launcher interface which can be implemented to work with different managed containers like Spring or Weld; Currently Spring is used as the main container implementation, but a minimal Launcher without any dependencies is planned in the near future. For the Spring implementation there are two abstract Launcher implementations are available:

- The AFXSpringXmlLauncher
- The AFXSpringJavaConfigLauncher

### AFXSpringXmlLauncher example ###

<pre>
public class ApplicationLauncher extends AFXSpringXmlLauncher {

<b>
    @Override
    public String getXmlConfig() {
        return "main.xml";
    }
</b>
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
<b>
    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class<?>[]{BaseConfiguration.class};
    }
</b>
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
The Workbench is the root node of your client project, providing simple interfaces to configure the basic behavior of your client. Besides the application launcher, it is the only component where you can get direct access to the JavaFX "stage". 
Furthermore a Workbench logically groups all perspectives defined in the @Workbench annotation.
 
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

The Workbench interface defines two method:

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

The postHandle method will be executed after the configuration in the "handleInitialLayout" method was done. Depending on the configured toolbars and menus you can add global toolbar/menue entries to your application here.
The FXComponentLayout interface defines following methods:

- layout.getRegisteredToolBar(ToolbarPosition.NORTH) : returns the (NORTH, SOUTH, EAST, WEST) toolbar
- layout.getRegisteredToolBars() : returns all registered toolbars
- layout.getMenu(): returns the application menu

To get detailled informations about toolbars, see **[Toolbars](#toolbars)**

<br/>
### Declare references to perspectives ###
To declare references to perspectives, simply add the perspective ID's in the "perspective" attribute located in the "@Workbench" annotation. The component scanning tries to find the corresponding perspective implementation in the classpath, so the implementations do not need to be located in the same project as the workbench, as long they are in the classpath.
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
A typical UI application has a root node and a large tree of components which represents your application UI. The leaf nodes of such a component-tree are your user-defined controles like Buttons, TextFields and so on.  A Perspective allows you to register JavaFX Nodes of your perspective view, where component views can be rendered. Child Components in your perspective can now registers themselves to be rendered in one of those targets.
<br/>
<div align="center">
![perspective node tree](/img/JACP_NodeTree_View.png)
</div>
<br/>


### The perspective lifecycle ###
A perspective defines five lifecycle hooks:

- The <b>"handlePerspective"</b> method must be overwritten and will be executed on each message the perspective is receiving.
- <b>@PostConstruct:</b> A method annotated with @PostConstruct will be executed when a perspective was activated, usually this happens on start 
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
Component-references are defined inside the @Perspective annotation. Once the application is started, you can move components from one perspective to an other. 
Component references are subjected to one simple rule: components are <b>ALWAYS unique per Perspective</b>, you can't add the same Component twice in one perspective, but you can use one Component in many perspectives. Each Component is a singleton per Perspective and a second Perspective will get a different Component instance as the first Perspective.
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
Render-targets are areas in your perspective where component-views can be rendered. You can register any node of your perspective-view to be a render-target. A child component of your perspective can now register itself to be rendered in this node.
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
While perspectives helping you to structure you application, components are more like "micro" applications or portlets. You can simply create master-detail views and reuse both Components in different Perspectives. Basically JacpFX components are distinguished in UI- and NonUI-Components;
UI-Components contain your complex UI (e.g Form) and Controls like "TextField" or "Button". NonUI-Components are ment to be services for long running tasks or a connector to an external system. All Components in common is, that they have a “handle” method that is <b>running outside the FX application thread</b>, so the execution of this method will not block the rest of your UI.

### UI-Components ###
UI-Components must implement the "FXComponent" interface, and act as controller class which returns a view either in plain JavaFX or FXML. 
While JavaFX-Components must return a (JavaFX) Node, FXML-Components passes the root-node of their FXML view directly to the parent perspective.

#### The FXComponent lifecycle ####


<br/>
<div align="center">
![UI component lifecycle](/img/JACP_UI-Component_Lifecycle.png)
</div>
<br/>

#### The FXComponent interface ####

The FXComponent interface defines following two methods to implement:

- The <b>"handle(...)"</b> method must be overwritten and will be executed first, each time the Component receives a message. This method will be executed <b>outside the FX Application Thread </b> inside an worker-thread. The return value of this method is a JavaFX Node which will be passed to the FX Application thread in the "postHandle" method. Unless you are not modify existing UI elements, you are free to create any new UI-components. You can use the handle method to create large and complex UI trees, but you should avoid modifications of existing Nodes (it will throw an UnsupportedOperationException exception). You are also free to return a null value and to create the View-element in the postHandle method.
- The <b>"postHandle(...)"</b> will be executed on the FX Application Thread after the "handle" method was finished. In this method you can modify any existing View-Nodes; In case of FXML components you should not return any Node (it will throw an UnsupportedOperationException), here the associated FXML document is the Node that is passed to the target in corresponding perspective.

#### Method-level annotations ####
- <b>@PostConstruct:</b> A method annotated with @PostConstruct will be executed when a Component was activated, usually this happens on start and before the "handle" method was executed in the FX Application Thread. The method signature can have no parameters, the FXComponentLayout layout parameter and/or the reference to the ResourceBundle resourceBundle. With the FXComponentLayout layout reference you can define Menu- and ToolBar-entries in your component.
- <b>@PreDestroy:</b> A method annotated with @PreDestroy will be executed when a component will be destroyed. The method will be executed on FX Application Thread. The method signature can have no parameters, the FXComponentLayout layout parameter and/or the reference to the ResourceBundle resourceBundle. With the FXComponentLayout layout reference you can define Menu- and ToolBar-entries in your component.

### FXComponent types ###
FXComponent can be written either <b>programmatically</b> in plain JavaFX or <b>declarative</b>, with an FXML view.

#### The @View class level annotation ###
The @View annotation contains all meta data related to the JavaFX-Component implementing the FXComponent interface.

- <b>"name"</b>, defines the Component name
- <b>"id"</b>, defines an unique Component Id
- <b>"active"</b>, defines the initial Component state. Inactive Components are activated on message.
- <b>"initialTargetLayoutId"</b>, contains the render-target id defined in the parent perspective.
- <b>"resourceBundleLocation" (optional)</b>, defines the resource bundle file
- <b>"localeID"</b>,  the default locale Id (http://www.oracle.com/technetwork/java/javase/locales-137662.html)

<br/>
#### JavaFX-Component example ####
The "postHandle" method of a JavaFX-Component must always return a JavaFX Node, representing the view of the component.
<br/>
<pre>
@View(id = ComponentIds.COMPONENT__TWO,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        initialTargetLayoutId = PerspectiveIds.TARGET__CONTAINER_MAIN)
public class ComponentTwo implements FXComponent {
	private VBox mainPane;
    @Override
    public Node handle(final Message<Event, Object> message) {
        // runs in worker thread
        return null;
    }
    @Override
    public Node postHandle(final Node arg0,
                           final Message<Event, Object> message) {
        // runs in FX application thread
        return this.mainPane;
    }
	@PostConstruct
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
       pane = createUI();
	}
	private VBox createUI() {
        final VBox mainPane = new VBox();
        HBox.setHgrow(mainPane, Priority.ALWAYS);
        final HBox top = new HBox();
        final HBox bottom = new HBox();
        mainPane.getChildren().addAll(top,bottom);
        return mainPane;
    }
}
</pre>

#### The @DeclarativeView class level annotation ###
The @DeclarativeView annotation contains all meta data related to the FXML-Component implementing the FXComponent interface.

- <b>"name"</b>, defines the Component name
- <b>"id"</b>, defines an unique Component Id
- <b>"viewLocation"</b>, defines the location the FXML file representing the view
- <b>"active"</b>, defines the initial Component state. Inactive Components are activated on message.
- <b>"initialTargetLayoutId"</b>, contains the render-target id defined in the parent perspective.
- <b>"resourceBundleLocation" (optional)</b>, defines the resource bundle file
- <b>"localeID"</b>,  the default locale Id (http://www.oracle.com/technetwork/java/javase/locales-137662.html)
<br/>
#### FXML-Component example ####
The "postHandle" method of a FXML-Component must return NULL, as the root node of the FXML-file will be passed to the perspective. 
<br/>
<pre>
@DeclarativeView(id = ComponentIds.COMPONENT_ONE,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        initialTargetLayoutId = PerspectiveIds.TARGET_CONTAINER_TOP,
        <b>viewLocation = "/fxml/ComponentOne.fxml")</b>
public class ComponentOne implements FXComponent {

    @FXML
    private VBox mainPane;    
    @FXML
    private HBox top;
    @FXML
    private HBox bottom;

    @Override
    public Node handle(final Message<Event, Object> message) {
        // runs in worker thread
        return null;
    }

    @Override
    public Node postHandle(final Node arg0,
                           final Message<Event, Object> message) {
        // runs in FX application thread
        return null;
    }

    @PostConstruct
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
    }
}
</pre>
<br/>

##### The ComponentOne.fxml file: #####
```xml
<VBox fx:id="mainPane" xmlns="http://javafx.com/javafx/8"
      xmlns:fx="http://javafx.com/fxml/1" HBox.hgrow="ALWAYS">
    <children>
        <HBox fx:id="top">
            ...
        </HBox>
        <HBox fx:id="bottom">
            ...
        </HBox>
    </children>
</VBox>
```
### Callback Components ###
Callback Components are service-like Components which reacts on messages and returns an Object to the caller Component or any other target (Request/Response). By default the caller Component will be notified, if no return value is defined no message will be send.
<br/>

#### CallbackComponent types ####
CallbackComponents can be either <b>stateful</b> or <b>stateless</b>; with an FXML view.

#### Stateful CallbackComponent ####
A stateful CallbackComponent must implement the CallbackComponent interface and contain the @Component annotation.
In terms of JEE it is a "singleton per perspective" component; While JEE singletons must be synchronized (Container- or Bean- managed concurrency), JacpFX components never accessed directly (only trough messages) and must not be synchronized. The container queues all messages and is aware of correct message delivering (similar to a MDB running on one thread). Like all JacpFX components it has a handle method that is executed in a separate Thread (Worker Thread). Use this type of component to handle long running tasks or service calls and when you need a conversational state. The result of your task will be send to the message caller by default. This type of component has one method you have to implement: 

#### Stateful CallbackComponent lifecycle ####


<br/>
<div align="center">
![stateful component lifecycle](/img/JACP_Stateful-Component.png)
</div>
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

###error handler###

 
