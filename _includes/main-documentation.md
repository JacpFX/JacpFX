
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
![JacpFX Component structure](/img/JACP_Overview_v2_1.png)
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

A perspective defines the basic UI structure for your view and provides a container for components. While a perspective is more like a template with placeholders, components are the detail views of your application.
<br/>
A typical UI application has a root node and a large tree of components which represents your UI structure. The leaf nodes of such a component-tree are your user-defined nodes containing the Buttons, TextFields and so on.  In a typical business application you can create a (Split-)Pane in your perspective, which represents the the root node of your current view, place a Pane on the left and on the right and register those Panes as “Targets” for your components. Child components of your perspective can now registers themselves to be rendered in one of those targets.
<br/>
<div align="center">
![perspective node tree](/img/JACP_NodeTree_View.png)
</div>
<br/>


### The perspective lifecycle ###

<br/>
<div align="center">
![perspective lifecycle](/img/JACP_Perspective_Lifecycle.png)
</div>
<br/>

### Perspective types ###
Perspectives can be written either <b>programmatic</b> in plain JavaFX or <b>declarative</b>, with an FXML view.
<br/> 
#### Programmatic Perspectives ####

Programmatic perspectives declare their view in plain JavaFX. You can create any complex UI tree, but you have to register the root node of you UI tree which will than be added to the workbench.

<pre>
@Perspective(id = BaseConfiguration.PERSPECTIVE_ONE, name = "PerspectiveOne",
        components = {...},
        resourceBundleLocation = "bundles.languageBundle")
public class PerspectiveOne implements FXPerspective {


    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        
    }


    @OnShow
    public void onShow(final FXComponentLayout layout) {

    }
    @OnHide
    public void onHide(final FXComponentLayout layout) {

    }

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
        HBox.setHgrow(contentTop, Priority.ALWAYS);

        HBox contentBottom = new HBox();
        HBox.setHgrow(contentBottom, Priority.ALWAYS);

        mainLayout.getItems().addAll(contentTop, contentBottom);
		<b>
       // Register root component
        perspectiveLayout.registerRootComponent(mainPane);
        </b>
		...
    }

    @PreDestroy
    public void onTearDownPerspective(final FXComponentLayout arg0) {
       ...
    }
    
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
    private VBox mainPane;


    @Resource
    public Context context;

    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {

    }


 	@OnShow
    public void onShow(final FXComponentLayout layout) {

    }
    @OnHide
    public void onHide(final FXComponentLayout layout) {

    }

    @PostConstruct
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
       LayoutUtil.GridPaneUtil.setFullGrow(ALWAYS, mainPane);
    }

    @PreDestroy
    public void onTearDownPerspective(final FXComponentLayout arg0) {

    }

}

</pre>
### Register targets ###

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

 
