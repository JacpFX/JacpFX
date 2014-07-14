
#JacpFX Documentation --- WORK IN PROGRESS --- #
This documentation pages give you detailed information about all parts of JacpFX, how to bootstrap a new JacpFX application and the general usage. You may want to read the [quick-start tutorial](documentation_quickstart.html) to start with a JacpFX project.
## What it is##
JacpFX is an UI application framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components. It frees you from the pitfalls of traditional multi-threaded programming helping you to separate the task execution from UI changes in your client application. JacpFX focusing on following goals to deliver best developer and user experience:

* simplify the structuring of loosely coupled UI components
* easy communication between components through a message-bus
* supports asynchronous processes to avoid UI blocking
* lightweight (size and memory footprint)


## General structure ##
Like any other UI application framework JacpFX has a hierarchic component-structure to create client applications.

<br/>
<div align="center">
![JacpFX Component structure](/img/JACP_Overview_v2.png)
</div>
<br/>


The following components make up a JacpFX application:

* an **[ApplicationLauncher](#ApplicationLauncher)**, which contains any configuration to bootstrap the application as well as the application main method.
* a **[Workbench](#workbench)**, the root Node of the client application. It contains the <i>Perspectives</i> and application specific configurations.
* at least one **[Perspective](#perspective)** to define the basic layout of your view
* **[UI Components](#components)** to define the content in a <i>Perspective</i>
* **[UI Fragments](#fragments)** to define parts of your UI. This allows you to split the <i>Component</i>-view in more fine-grained managed controls.
* **[Stateful/Stateless service Components](#services)**, non UI service-<i>Components</i> for task execution and communication with external systems.

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
#### Java8 ####
#### JavaFX8 ####
#### maven ####
<br/>

## <a name=ApplicationLauncher></a>ApplicationLauncher 
An ApplicationLauncher contains the main method, the component-scanning configuration, the managed container configuration and a reference to the workbench class.
<br/>JacpFX defines a Launcher interface which can be implemented to work with different managed containers like Spring or Weld. Currently Spring is used as the main container implementation, but a minimal Launcher without any dependencies is planned in the near future. For the Spring implementation two abstract Launcher implementations are available:

- The <i>AFXSpringXmlLauncher</i>
- The <i>AFXSpringJavaConfigLauncher</i>

### AFXSpringXmlLauncher example ###

<script src="https://gist.github.com/amoAHCP/53959e5378c31ae5a72b.js"></script>

> The "getXMLConfig()" methods returns the name of your spring configuration xml, which is located in the resources folder.

<br/>
### AFXSpringJavaConfigLauncher example ###


<script src="https://gist.github.com/amoAHCP/51f4752d9a622f8366fc.js"></script>

> The "getConfigClasses()" returns an array with all valid spring configuration classes (annotated with @Configuration)
<br/>

### Common applicationLauncher methods ###
#### getWorkbenchClass ####
Returns the defined Workbench class.
<br/>

#### getBasePackages ####
Define all packages to scan for <i>Components</i> and <i>Perspectives</i>. JacpFX uses component scanning to resolve all <i>Components</i> and <i>Perspectives</i> by ID.
<br/>

#### postInit ####
This method gives you access to the JavaFX stage. You may define a stylesheet for your application.

## <a name=workbench></a>Workbench ##
The Workbench is the root node of your client project. It provides a simple interface to configure the basic behavior of your client. Besides the application launcher, it is the only component where you can get direct access to the JavaFX "stage". 
Furthermore a Workbench logically groups all <i>Perspectives</i> defined by @Workbench annotations.
 
### Example workbench ###
<br/>
<script src="https://gist.github.com/amoAHCP/2d5613384248caa6b4d2.js"></script>
<br/>

The Workbench interface defines two method:

- handleInitialLayout
- postHandle
<br/>

### The handleInitialLayout method ###
This method is the first one called on application startup. It allows you to do a basic configuration of your application. The method signature defines three parameter:

- Message<Event,Object> action : the initial message, see **[JacpFX messaging](#messaging)**

- WorkbenchLayout<Node> layout (the configuration handler to define the following application values): 
	- layout.setWorkbenchXYSize(x,y) : define the initial workbench size
	- layout.registerToolBar(ToolbarPosition.NORTH): activate toolbars (NORTH, SOUTH, EAST, WEST)
	- layout.setStyle(StageStyle.DECORATED): enable/disable window decoration 
	- layout.setMenuEnabled(false): enable/disable application menues
	
- Stage: the JavaFX "Stage" object

<br/>
### The postHandle method ###

The postHandle method will be executed after the configuration in the "handleInitialLayout" method. Depending on the configured toolbars and menus you can add global toolbar/menu entries to your application here.
The FXComponentLayout interface defines the following methods:

- layout.getRegisteredToolBar(ToolbarPosition.NORTH) : returns the (NORTH, SOUTH, EAST, WEST) toolbar
- layout.getRegisteredToolBars() : returns all registered toolbars
- layout.getMenu(): returns the application menu

For detailed information about toolbars, see **[Toolbars](#toolbars)**

<br/>
### Declare references to <i>Perspectives</i> ###
To declare references to <i>Perspectives</i>, simply add the <i>Perspective</i> ID's to the <i>"perspectives"</i> attribute of "@Workbench" annotations. The component-scanning tries to find the corresponding <i>Perspective</i> implementation in the classpath. The Implementations do not need to be located in the same project as the workbench as long they are in the classpath.
<br/>
<script src="https://gist.github.com/amoAHCP/f0217d935d55e81aa277.js"></script>

##<a name=perspective></a><i>FXPerspectives</i>##

A <i>FXPerspective</i> defines the basic UI structure for your view and provides a container for <i>Components</i>. 
While a <i>FXPerspective</i> is more like a template with placeholders (or a portal page), <i>Components</i> are the detail views of your application (or the portlets).
<br/>
A typical UI application has a root node and a large tree of Nodes/Controls which represent the application UI. The leaf nodes of such a component tree are your user defined controles like Buttons, TextFields and so on.  A Perspective allows you to register JavaFX Nodes of your <i>FXPerspective</i> view, where <i>Component</i> views are rendered. Child <i>Components</i> in your <i>FXPerspective</i> can now registers themselves to be rendered in one of those targets.
<br/>
<div align="center">
![perspective node tree](/img/JACP_NodeTree_View.png)
</div>
<br/>


### The <i>FXPerspective</i> lifecycle ###
A <i>FXPerspective</i> defines five lifecycle hooks:

- The <b>"handlePerspective"</b> method must be overwritten and will be executed on each message the <i>FXPerspective</i> is receiving.
- <b>@PostConstruct:</b> A method annotated with @PostConstruct will be executed after a <i>FXPerspective</i> was activated, usually this happens on start 
- <b>@PreDestroy:</b> A method annotated with @PreDestroy will be executed before a <i>Perspective</i> is destroyed
- <b>@OnShow:</b> A method annotated with @OnShow will be executed when an active <i>Perspective</i> gets the focus. Only one <i>FXPerspective</i> is visible in a workbench at the same time. When a <i>FXPerspective</i> gets a message it gets the focus and is placed to the foreground.
	- in this phase you may turn on toolbar buttons or start timer tasks
- <b>@OnHide:</b> A method annotated with @OnHide will be executed when an active <i>FXPerspective</i> loses the focus and is moved to the background.


<br/>
<div align="center">
![perspective lifecycle](/img/JACP_Perspective_Lifecycle.png)
</div>
<br/>

### <i>FXPerspective</i> types ###
<i>FXPerspective</i> can be implemented either <b>programmatically</b> in plain JavaFX or <b>declarative</b> with an FXML view.
<br/> 
#### Programmatic <i>FXPerspectives</i> ####

Programmatic <i>FXPerspectives</i> declare their view in plain JavaFX. You can create any complex UI tree but you have to register the root Node of your UI tree which will then be added to the workbench.

<script src="https://gist.github.com/amoAHCP/5a07f9563f4c2b726763.js"></script>
<br/>
#### Declarative <i>FXPerspectives</i> ####
Declarative <i>FXPerspectives</i> provides their view by defining a FXML file representing the view. The root Node is always the root of your FXML and is automatically registered.

<script src="https://gist.github.com/amoAHCP/408d1e8b86388c5e5e5b.js"></script>
<br/>
####The FXML view:####

<script src="https://gist.github.com/amoAHCP/a95003f8d2b84ad32802.js"></script>
<br/>
### Register <i>Components</i> ###
<i>Component</i> references are defined inside the @Perspective annotation. Once the application is started you can move <i>Components</i> from one <i>Perspective</i> to another. 
<i>Components</i> are subjected to one simple rule: They are <b>ALWAYS unique per <i>Perspective</i></b>. You can't add the same <i>Component</i> twice in one <i>FXPerspective</i>, but you can use one <i>Component</i> in many <i>FXPerspectives</i>. The same <i>Component</i> will be created in it's own instance per <i>FXPerspective</i>.
#### Definition of <i>Component</i> references####
<script src="https://gist.github.com/amoAHCP/ee4ffe9c557841cf1066.js"></script>

<br/>

### Register render targets ###
Render targets are areas in your <i>FXPerspective</i> where <i>Component</i> views are rendered. You may register any Node of your <i>FXPerspective</i> view to be a render target. A child <i>Component</i> of your <i>FXPerspective</i> can now register itself to be rendered in this node.
#### Definition of render-targets####
<script src="https://gist.github.com/amoAHCP/a7c92f0951d75f7b94f8.js"></script>

<br/>

### The <i>@Perspective</i> annotation ###
The @Perspective annotation provides necessary metadata for all classes implementing the <i>FXPerspective</i> interface. The following attributes describe a JacpFX <i>FXPerspective</i>:

- name (mandatory): The <i>Perspective</i> name
- id (mandatory): The <i>Perspective</i> id
- components (mandatory): all referenced <i>Component</i> id's
- active (optional): The state of the <i>Perspective</i>. The default value is "true". If the value is set to "false" the perspective will be activated when it receives the first message.
- viewLocation (optional): The path to the FXML file. If this attribute is set the <i>FXPerspective</i> will be handled as a declarative <i>Perspective</i>.
- resourceBundleLocation (optional): The path to your resource bundle.
- localeID (optional): The default locale, if not set the system default will be used.

## <a name=components></a>Components ##
While <i>FXPerspectives</i> help you to structure your application, <i>Components</i> are more like "micro" applications or portlets. You can simply create master-detail views and reuse <i>Components</i> in different <i>FXPerspectives</i>. Basically JacpFX <i>Components</i> are distinguished in UI and NonUI <i>Components</i>;
UI <i>Components</i> may contain your complex UI (e.g Form) and Controls like "TextField" or "Button". NonUI <i>Components</i> are meant to be services for long running tasks or a connector to an external system. Common to all <i>Components</i>is that they have a “handle” method. That method will be run <b> outside the FX application thread</b> so the execution does not block the rest of your UI.

### UI-<i>Components</i> ###
UI <i>Components</i> must implement the <i>"FXComponent"</i> interface. They act as controller class and return a view either in plain JavaFX or FXML. 
While JavaFX <i>Components</i> must return a (JavaFX) Node, FXML-<i>Components</i> pass the root node of their FXML view directly to the parent <i>FXPerspective</i>.

#### The FXComponent lifecycle ####


<br/>
<div align="center">
![UI component lifecycle](/img/JACP_UI-Component_Lifecycle.png)
</div>
<br/>

#### The FXComponent interface ####

The FXComponent interface defines the following two methods:

- The <b>"handle(...)"</b> method is executed first each time the <i>FXComponent</i> receives a message. This method will be executed <b>outside the FX Application Thread </b> an a worker-thread. The return value of this method is a JavaFX Node which will be passed to the FX Application thread from the "postHandle" method. Unless you not modify existing UI elements you are free to create any new UI elements. You can use the handle method to create large and complex UI trees, but you should avoid modifications of existing Nodes (it will throw an UnsupportedOperationException exception). You are also free to return a null value and to create the View-element in the postHandle method.
- The <b>"postHandle(...)"</b> will be executed on the FX Application Thread after the "handle" method is finished. In this method you can modify any existing View Node. In case of FXML <i>Components</i> you should not return any Node (it will throw an UnsupportedOperationException). The associated FXML document is the Node that is passed to the target from the corresponding <i>FXPerspective</i>.

#### Method-level annotations ####
- <b>@PostConstruct:</b> A method annotated with @PostConstruct will be executed when a <i>FXComponent</i> is activated. Usually this happens on start and before the "handle" method. @PostConstruct is executed in the FX Application Thread. The method signature may have no parameter, an FXComponentLayout parameter and/or a reference to the ResourceBundle. With the FXComponentLayout reference you can define Menu and ToolBar entries in your <i>FXComponent</i>.
- <b>@PreDestroy:</b> A method annotated with @PreDestroy will be executed when a <i>FXComponent</i> is destroyed. The method will be executed inside the FX Application Thread. The method signature may have no parameters, the FXComponentLayout parameter and/or the reference to the ResourceBundle. 

### FXComponent types ###
FXComponent may be written either <b>programmatically</b> in plain JavaFX or <b>declarative</b>, with an FXML view.

#### The @View class level annotation ###
The @View annotation contains all metadata related to a JacpFX-<i>Component</i> implementing the FXComponent interface.

- <b>"name"</b>, defines the <i>FXComponent</i> name
- <b>"id"</b>, defines an unique <i>FXComponent</i> Id
- <b>"active"</b>, defines the initial <i>FXComponent</i> state. Inactive <i>FXComponent</i> are activated on message.
- <b>"initialTargetLayoutId"</b>, contains the render target id defined in the parent <i>FXPerspective</i>.
- <b>"resourceBundleLocation" (optional)</b>, defines the resource bundle file
- <b>"localeID"</b>,  the default locale Id (http://www.oracle.com/technetwork/java/javase/locales-137662.html)

<br/>
#### JavaFX FXComponent example ####
The "postHandle" method of a <i>FXComponent</i> must always return a JavaFX Node representing the view of the JacpFX-<i>Component</i>.
<script src="https://gist.github.com/amoAHCP/b4f780fb5ed0bc1a7ccf.js"></script>
<br/>
#### The @DeclarativeView class level annotation ####
The @DeclarativeView annotation contains all metadata related to the FXML-<i>Component</i> implementing the FXComponent interface.

- <b>"name"</b>, defines the <i>FXComponent</i> name
- <b>"id"</b>, defines an unique <i>FXComponent</i> Id
- <b>"viewLocation"</b>, defines the location the FXML file representing the view
- <b>"active"</b>, defines the initial <i>FXComponent</i> state. Inactive <i>FXComponent</i> are activated on message.
- <b>"initialTargetLayoutId"</b>, contains the render target id defined in the parent <i>FXPerspective</i>.
- <b>"resourceBundleLocation" (optional)</b>, defines the resource bundle file
- <b>"localeID"</b>,  the default locale Id (http://www.oracle.com/technetwork/java/javase/locales-137662.html)



<br/>
#### FXML FXComponent example ####
The "postHandle" method of a FXML <i>FXComponent</i> must return NULL, as the root node of the FXML-file will be passed to the <i>FXPerspective</i>. 

<script src="https://gist.github.com/amoAHCP/b8151043d620d06d50ab.js"></script>
<br/>

##### The ComponentOne.fxml file: #####
<script src="https://gist.github.com/amoAHCP/3b0a87adb19baea6effa.js"></script>
<br/>
### Callback <i>Components</i> ###
Callback <i>Components</i> are service like <i>Components</i> which react on messages and returns an Object to the caller <i>Component</i> or any other target (Request/Response). By default the caller <i>Component</i> will be notified unless no return value is specified.
<br/>

#### The CallbackComponent interface ####

The CallbackComponent interface defines the following two methods to implement:

- The <b>"handle(...)"</b> method will be executed each time the <i>Component</i> receives a message. This method will be executed <b>outside the FX Application Thread </b> inside a worker thread. 
By default the return value of this method will generate a message to the caller <i>Component</i> to return the result. If no return value is specified, no result message will be sent. If the return value should be redirected to another <i>Component</i> you can use "<i>Context.setReturnTarget("parent.targetId")</i>" to specify the target <i>Component</i>.

#### Method-level annotations ####
- <b>@PostConstruct:</b> A method annotated with @PostConstruct will be executed when a <i>Component</i> is activated, and runs in a worker Thread. The method signature may have no parameter and/or the reference to the ResourceBundle resourceBundle. 
- <b>@PreDestroy:</b> A method annotated with @PreDestroy will be executed before a <i>Component</i> is be destroyed. The method will be executed in a worker Thread. The method signature may have no parameter and/or the reference to the ResourceBundle. 


#### The @Component class level annotation ####
The @Component annotation contains all metadata related to the Callback-<i>Component</i> implementing the CallbackComponent interface.

- <b>"name"</b>, defines the <i>Component</i> name
- <b>"id"</b>, defines an unique <i>Component</i> Id
- <b>"active"</b>, defines the initial <i>Component</i> state. Inactive <i>Components</i> are activated on message.
- <b>"resourceBundleLocation" (optional)</b>, defines the resource bundle file
- <b>"localeID"</b>,  the default locale Id (http://www.oracle.com/technetwork/java/javase/locales-137662.html) <br/>


### CallbackComponent types ###
CallbackComponents can be <b>stateful</b> or <b>stateless</b>

#### Stateful CallbackComponent ####
A stateful CallbackComponent must implement the <i>CallbackComponent</i> interface and define the @Component annotation.
In terms of JEE it is a "singleton per <i>Perspective</i>" <i>Component</i>. While JEE singletons must be synchronized (Container or Bean managed concurrency), JacpFX <i>Components</i> are never accessed directly (only trough messages) and must not be synchronized. The container puts all messages to a FIFO queue and is aware of correct message delivering (similar to a Message Driven Bean running on one thread). Like all JacpFX <i>Components</i> it has a handle method that is executed in a separate Thread (Worker Thread). Use this type of <i>Component</i> to handle long running tasks or service calls and when you need a conversational state. The result of your task will be sent to the message caller by default. This type of <i>Component</i> has one method you have to implement: 

#### Stateful CallbackComponent lifecycle ####


<br/>
<div align="center">
![stateful component lifecycle](/img/JACP_Stateful-Component.png)
</div>
<br/>

#### Stateful CallbackComponent example ####

<script src="https://gist.github.com/amoAHCP/f63986d02e6db76b24e3.js"></script>
<br/>

#### Stateless CallbackComponent ####
A stateless CallbackComponent must implement the <i>CallbackComponent</i> interface and define the annotations @Component and @Stateless.
Stateless <i>Components</i> are using instance-pooling for scaling, a CallbackComponent pool will be created for every <i>Component</i> per <i>Perspective</i>.

#### Stateless CallbackComponent lifecycle ####

<br/>
<div align="center">
![stateless component lifecycle](/img/JACP_Stateless-Component.png)
</div>

<br/>
#### Stateful CallbackComponent example ####

<script src="https://gist.github.com/amoAHCP/1d7e5fdd6e8c8ed8b2a4.js"></script>
<br/>

## <a name=fragments></a>Fragments ##
JacpFX <i>Fragments</i> are small managed <i>Components</i>, that exist in the context of a <i>FXPerspective</i> or a <i>FXComponent</i>. The purpose of a <i>Fragment</i> is to create a reusable custom control or a group of controls that has access to the parent context. This allows the Fragment to send messages, access resources and to interact with the parent <i>FXComponent</i> or <i>FXPerspective</i>.
A <i>Fragment</i> can either extent a JavaFX "Node" or declare a FXML view. The <i>Fragment</i> itself can not be a message target, but his parent <i>FXComponent</i> can access his Controller class and the view. 
<br/>
### The @Fragment class level annotation ###
The @Fragment annotation contains all metadata related to the JacpFX <i>Fragment</i>.

- <b>"id"</b>, defines an unique <i>Fragment</i> Id
- <b>"viewLocation"</b>, defines the location the FXML-file representing the view
- <b>"resourceBundleLocation" (optional)</b>, defines the resource bundle file
- <b>"localeID"</b>,  the default locale Id (http://www.oracle.com/technetwork/java/javase/locales-137662.html)
- <b>"scope"</b>,  defines the scope of the <i>Fragment</i> (singleton/prototype). 
<br/>


### Fragment types ###
<i>Fragments</i> can either extend a JavaFX Node, or be a POJO defining a FXML view.
<br/>
#### FXML Fragment example####
<script src="https://gist.github.com/amoAHCP/7637efbad4bab3c672b6.js"></script>
<br/>
##### The FragmentOne.fxml file: #####
<script src="https://gist.github.com/amoAHCP/85f8fc6f14e558a8ebc3.js"></script>
<br/>
#### JavaFX Fragment example####
<script src="https://gist.github.com/amoAHCP/1633ecbbadb709cf7000.js"></script>
<br/>
### Create a Fragment instance ###
<i>Fragments</i> <b>never</b> instantiated directly, they can only be created inside a <i>FXPerspective</i> or an <i>FXComponent</i>. To create a <i>Fragment</i>, the <i>Context</i> interface provides the method: <i>getManagedFragmentHandler(FragmentOne.class);</i> and returns a <i>ManagedFragmentHandler</i>. The Handler provides access to the controller (FragmentOne) and to the view (VBox).
Depending on the <i>Fragment</i> scope, the method call returns always the same instance or in case of "prototype" scope, different one. 
<br/>

<script src="https://gist.github.com/amoAHCP/8336576900b69d467b83.js"></script>
<br/>

#### The ManagedFragmentHandler ####

The <i>ManagedFragmentHandler</i> holds the reference to the <i>Fragment</i> instance and their view. To create a <i>Fragment</i>, the JacpFX <i>Context</i> interface provides the method: <i>getManagedFragmentHandler(FragmentOne.class);</i> and returns a <i>ManagedFragmentHandler</i>. The Handler provides access to the controller (FragmentOne) and to the view (VBox).

- <b>"getController()"</b> returns the an instance of your <i>Fragment</i>.
- <b>"getFragmentNode()"</b> returns an JavaFX Node representing the view.

## <a name=messaging></a>JacpFX messaging##
Messaging is an essential part of JacpFX that allows to communicate with all <i>FXPerspectives</i>/JacpFX <i>Components</i>, and to change their state. You can send an object to any <i>components</i> and start their specific lifecycle.
<br/>
<div align="center">
![message lifecycle](/img/JACP_ComponentMessage_View.png)
</div>
<br/>

### The message interface ###
The <i>Message</i> interface provides access to the message payload itself and contains methods for easy message checks. The interface contains following methods:

- <b>"getMessageBody()"</b> returns the message payload
- <b>"getSourceId()"</b> returns the caller id
- <b>"getSourceEvent()"</b> returns the event source
- <b>"getTargetId()"</b> returns the target id
- <b>"isMessageBodyTypeOf(Class<T> clazz)"</b> checks if the message payload is type of the declared Class
- <b>"getTypedMessageBody(Class<T> clazz)"</b> returns the typed message payload
- <b>"messageBodyEquals(Object object)"</b> returns true if the payload equals to the declared Object

<br/>

### Send a message ###
The JacpFX <i>Context</i> provides methods to send messages to other <i>Components</i> and to the caller itself.

- <b>"send(M message)"</b>, sends any Object to the caller component itself. The target and the source id is equal in this case. You may use this method to trigger asynchronous execution in the "handle(...)" method of your <i>Component</i>.
- <b>"send(String targetId, M message)"</b>, sends any Object to the specified target
<br/> 

### Message addressing schema###
JacpFX has a hierarchical Component schema where a <i>Workbench</i> is the root node and <i>Components<i/> are the leaf nodes. The <i>Workbench</i> can never be a message target itself but you may send messages from a <i>FXWorkbench</i> to any <i>Perspective</i>/<i>Component</i>. JacpFX has a simple "." (dot) separator to specify the exact target of your message.

- <b>send a message to a perspective: </b> context.send("perspective1", new Person("John")) 
- <b>send a message to a component: </b> context.send("perspective1.component1", new Person("John")) 
<br/>

### Message example ###
<script src="https://gist.github.com/amoAHCP/0ebad3a7f73bcc27fbd0.js"></script>
<br/>
## The JacpFX Context ##
The JacpFX <i>Context</i> provides methods to access to the metadata of any <i>Perspective</i>/<i>Component</i> and to several JacpFX functionality. To get a <i>org.jacpfx.rcp.context.Context</i> reference you must annotate a class member with this type. Following methods and metadata are provided by the <i>Context</i> interface:

- <b> getId(), getParentId(), getName(), getResourceBundle(), isActive()</b>, the <i>Perspective</i>/<i>Component</i> metadata
- <b> setActive(false)</b> deactivate a <i>Component</i> if a <i>Perspective</i> is deactivated all child <i>Components</i> are deactivated too
- <b> setReturnTarget("componentId")</b>, only valid for <i>CallbackComponent</i>; set the return target for the "handle(...)" method
- <b>setExecutionTarget("parentId")</b>, only valid for JacpFX <i>Components</i>; defines the parent <i>Perspective</i> by id. You may move <i>Components</i> from one <i>Perspective</i> to another.
- <b>setTargetLayout("top")</b>, only valid for <i>FXComponents</i>; set a valid render target defined by the parent <i>Perspective</i>. You may move a <i>Component</i> view from one area in your view to another.
- <b>getManagedFragmentHandler(Class<T> clazz)</b>, only valid for <i>FXComponents</i>; Creates a <i>ManagedFragment</b> by type and returns a <i>ManagedFragmentHandler</i> reference
- <b>showModalDialog(Node node)</b>, only valid for <i>FXComponents</i> and <i>FXPerspectives</i>; show a JavaFX Node in the modal dialog pane of the Workbench
- <b>hideModalDialog()</b>, only valid for <i>FXComponents</i> and <i>FXPerspectives</i>; hide the currently visible modal dialog
- <b>invokeFXAndWait(Runnable r)</b>, only valid for <i>FXComponents</i> and <i>FXPerspectives</i>; invoke a Runnable on JavaFX Application Thread and wait for the execution

##modal dialogs##

##toolbar and menubar##

##localisation##

@Component, @DeclarativeView, @View and @Perspective annotation allow the declaration of a resource bundle and a default localeID. If no localeID is declared the system default is assumed. Set the relative resourceBundleLocation in URL (in resource) like "bundles.languageBundle" and create in resources/bundles a file languageBundle_en.properties for further informations on resource bundles see: http://docs.oracle.com/javase/7/docs/api/java/util/ResourceBundle.html. 

<script src="https://gist.github.com/amoAHCP/553e9047fb68e083112f.js"></script>

<br/>To get access to the ResourceBundle use a @PostConstruct annotated method with a <i>ResourceBundle</i> parameter or annotate a class member of type <i>ResourceBundle</i> with @Resource.

##resources##
The default project layout provides following structure for resources:

<b>src/main/resources:</b>

- bundles: resource bundle files
- fxml: all fxml files
- images: application images
- styles: css files
<br/>

### assign a stylesheet ###
Assuming you put your stylesheet to <i>src/main/resources/styles/mystyle.css</i>, you may assign the stylesheet in the application launcher. The <i>postInit</i> method gives you access to the JavaFX stage objects where you may ad the stylesheet like this:

<script src="https://gist.github.com/amoAHCP/9d9d061965e247c56610.js"></script>

##dependency injection##
Dependency injection is provided by the <i>Launcher</i> implementation which is currently is Spring. All <i>Perspectives</i> and <i>Components</i> are Spring managed beans and supports all injection capabilities of a Spring bean.

##annotations overview##

###class-level annotations###

###method-level annotations###

<b>@PostConstruct</b>
Lifecycle annotation, a method annotated with <i>@PostConstruct</i> is executed on component startup (also when component is reactivated). It is applicable for all component types and perspectives. Annotated methods MUST NOT throw a checked exception. Following method signature is applicable:

- method with no parameters
- with FXComponentLayout layout
- with FXComponentLayout layout, URL url (in case of FXML components)
- with FXComponentLayout layout, URL url (in case of FXML components) , ResourceBundle resourceBundle
<br/>

<b>@PreDestroy</b>
Lifecycle annotation, a method annotated with <i>@PreDestroy</i> is executed on component shutdown. It is applicable for all component types and perspectives. Annotated methods MUST NOT throw a checked exception. Following method signature is applicable:

- method with no parameters
- with FXComponentLayout layout
- with FXComponentLayout layout, URL url (in case of FXML components)
- with FXComponentLayout layout, URL url (in case of FXML components) , ResourceBundle resourceBundle

##error handler##
An error handler catches all Exceptions occur in an JacpFX application and forwards it to an error dialog. JacpFX comes with a default implementation, but you may want to to overwrite it with your own implementation.
To do so you need to extend the <i>AErrorDialogHandler</i> and to implement an error dialog.
### example dialogHandler###
<script src="https://gist.github.com/amoAHCP/f907e320553e7b331652.js"></script>
<br/>
The ErrorDialog must extend an JavaFX <i>Node</i> and should handle/display the StackTrace or an appropriate error message. <br/>
To register the dialogHandler overwrite the <i>getErrorHandler()</i> method in the application launcher.


 
