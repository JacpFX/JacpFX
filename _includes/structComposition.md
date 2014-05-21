
#Structuring and composition#
<br/>

> JacpFX can help you to define your application UI in various ways. It allows you to mix FXML and JavaFX easily. 

* The root of a JacpFX application is always the workbench which defines an application window.

* A workbench contains 1-n perspectives, each defining the layout of their view. A perspective defines his view either programmatically with JavaFX or by using a FXML file. In each perspective view you can register several UI nodes as a target (placeholder) for a component view. 

* A perspective contains 0-n UI components. Each component registers for a specific target (defined in the parent perspective) where the component view will be rendered. A component can represent e.g. a complex form or any other complex UI part. Like any perspective you can define the view either in FXML or JavaFX.

* A component can contain 0-n ManagedFragments. A ManagedFragment is a reusable custom control which can be e.g a part of a complex form (the address part) and can also be reused in other components. Like all perspectives and components, a ManagedFragment can have a FXML or JavaFX view.

> The following example demonstrates how to define a FXML and a JavaFX perspective, how to declare the target areas for component views and how to implement FXML and JavaFX components.

<br/>
<div align="center">
![basic perspective](/img/overview.jpg)
</div>
<br/>

## Define the perspective view ##
The first step is to create two perspectives having the same UI, one implemented with a FXML view and the other with a JavaFX view.
### FXML perspective example ###
<pre>
@Perspective(id = BaseConfig.ID, name = "p1",components = {…},
        <b>viewLocation = "/fxml/ExamplePerspective.fxml")</b>
public class ExampleFXMLPerspective implements FXPerspective {
   …
}
</pre>


####The ExamplePerspective.fxml file:

```xml
<BorderPane id="mainPane"
            xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
    <center>
        <SplitPane fx:id="mainLayout" dividerPositions="0.55" focusTraversable="true"
                   orientation="VERTICAL"  HBox.hgrow="ALWAYS">
            <items>
                <HBox fx:id="contentTop" />
                <HBox fx:id="contentBottom">
                    <children>
                        <Button fx:id="myButton" mnemonicParsing="false" text="my button"/>
                    </children>
                </HBox>
            </items>
        </SplitPane>
    </center>
</BorderPane>
```
<br/>
### JavaFX perspective example ###
The code example below will produce exactly the same UI output like the FXML Perspective. You are free to mix FXML- and JavaFX-perspectives in one workbench. 

<pre>
@Perspective(id = BaseConfig.ID, name = "p1",components = {…})
public class ExampleJavaFXPerspective implements FXPerspective {

    @PostConstruct
    public void onStartPerspective(final PerspectiveLayout perspectiveLayout,final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        BorderPane mainPane = new BorderPane();

        SplitPane mainLayout = new SplitPane();
        mainLayout.setOrientation(Orientation.VERTICAL);
        mainLayout.setDividerPosition(0, 0.55f);
        mainPane.setCenter(mainLayout);

        HBox contentTop = new HBox();
        HBox.setHgrow(contentTop, Priority.ALWAYS);
        
        HBox contentBottom = new HBox();
        HBox.setHgrow(contentBottom, Priority.ALWAYS);
        
        contentBottom.getChildren().add(new Button("my button"));
        mainLayout.getItems().addAll(contentTop,contentBottom);

        // Register root component
       <b> perspectiveLayout.registerRootComponent(mainPane);</b>
 		...
    }
}
</pre>
> Note: In case of JavaFX views you must register the root component of your view:

```java
// Register root component
perspectiveLayout.registerRootComponent(mainPane);
```
<br/>
#### The resulting UI will look in both cases (FXML and JavaFX) like this: ####
<br/>
<div align="center">
![basic perspective](/img/basicPerspective.jpg)
</div>

<br/>
## Define target areas for component rendering ##
Both perspectives define a very basic SplitPane layout with a top content- and bottom-content area. The examples above define a HBox for top area and a BorderPane for the bottom area. Both nodes can be registered to be a target for components. 
### Register targets in the FXML perspective ###
<pre>
@Perspective(id = BaseConfig.ID, name = "p1",components = {…},
        <b>viewLocation = "/fxml/ExamplePerspective.fxml")</b>
public class ExampleFXMLPerspective implements FXPerspective {
  	<b>@FXML
    private HBox contentTop;
    @FXML
    private HBox contentBottom;</b>
    
    @PostConstruct
    public void onStartPerspective(final PerspectiveLayout perspectiveLayout, final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        // register left menu
     <b>perspectiveLayout.registerTargetLayoutComponent(PerspectiveIds.TARGET_CONTAINER_TOP, contentTop);</b>
        // register main content
     <b>perspectiveLayout.registerTargetLayoutComponent(PerspectiveIds.TARGET_CONTAINER_BOTTOM, contentBottom);</b>

    }
}
</pre>
### Register targets in the JacpFX perspective ###

<pre>
@Perspective(id = BaseConfig.ID, name = "p1",components = {…})
public class ExampleJavaFXPerspective implements FXPerspective {

    @PostConstruct
    public void onStartPerspective(final PerspectiveLayout perspectiveLayout,final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        BorderPane mainPane = new BorderPane();
        SplitPane mainLayout = new SplitPane();
		...
        <b>HBox contentTop = new HBox();</b>
        <b>HBox contentBottom = new HBox();</b>
		...
        // Register root component
        perspectiveLayout.registerRootComponent(mainPane);
 		...
        // register left menu
     <b>perspectiveLayout.registerTargetLayoutComponent(PerspectiveIds.TARGET_CONTAINER_TOP, contentTop);</b>
        // register main content
     <b>perspectiveLayout.registerTargetLayoutComponent(PerspectiveIds.TARGET_CONTAINER_BOTTOM, contentBottom);</b>
    }
}
</pre>
<br/>
## Define the component views ##
Component views are detailed parts of your perspective view. Each perspective view can have many component views defined in FXML or programmatically.
<br/>
### FXML component example: ###
<pre>
@DeclarativeView(id = ComponentIds.COMPONENT_ONE,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        <b>initialTargetLayoutId = PerspectiveIds.TARGET_CONTAINER_TOP,</b>
        <b>viewLocation = "/fxml/ComponentOne.fxml")</b>
public class ComponentOne implements FXComponent {

    @FXML
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
        return null;
    }

    @PostConstruct
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
    }
}

</pre>
> Note: the "initialTargetLayoutId" attribute registers the component view for a specific targetLayout defined in the parent perspective. The current FXML component will be registered for "TARGET_CONTAINER_TOP" in the parent perspective.   
<br/>

### The ComponentOne.fxml file: ###
```xml
<VBox fx:id="mainPane" xmlns="http://javafx.com/javafx/8"
      xmlns:fx="http://javafx.com/fxml/1" HBox.hgrow="ALWAYS">
    <children>
        <HBox prefHeight="100.0" prefWidth="200.0" fx:id="top">
            <children>
                <Label text="First name:">
                   ...
                </Label>
                <TextField prefHeight="50.0" HBox.hgrow="ALWAYS">
                    ...
                </TextField>
            </children>
        </HBox>
        <HBox prefHeight="100.0" prefWidth="200.0" fx:id="bottom">
            ...
        </HBox>
    </children>
</VBox>
```
### JavaFX component example ###
The following component example will produce the same UI output as the FXML example above.
<br/>
<pre>
@View(id = ComponentIds.COMPONENT_TWO,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        initialTargetLayoutId = PerspectiveIds.TARGET_CONTAINER_MAIN)
public class ComponentTwo implements FXComponent {
	private VBox pane;
    @Override
    public Node handle(final Message<Event, Object> message) {
        // runs in worker thread
        return null;
    }
    @Override
    public Node postHandle(final Node arg0,
                           final Message<Event, Object> message) {
        // runs in FX application thread
        return this.pane;
    }
	@PostConstruct
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
       pane = (VBox) createUI();
	}
	private Node createUI() {
        final VBox pane = new VBox();
        HBox.setHgrow(pane, Priority.ALWAYS);
        final HBox top = new HBox();
        top.setPrefHeight(100);
        top.setPrefWidth(200);
        Label firstName = new Label("First name:");
        firstName.setFont(new Font(29.0));
        HBox.setMargin(firstName, new Insets(15,5,0,5));
        TextField firstNameText = new TextField();
        firstNameText.setPrefHeight(50);
        firstNameText.setPadding(new Insets(10, 0, 0, 0));
        HBox.setHgrow(firstNameText,Priority.ALWAYS);
        HBox.setMargin(firstNameText, new Insets(10,5,0,0));
        top.getChildren().addAll(firstName,firstNameText);
        final HBox bottom = new HBox();
		...
        pane.getChildren().addAll(top,bottom);
        return pane;
    }
}
</pre>
> The JavaFX component will be registered for "TARGET_CONTAINER_MAIN" in the parent perspective. The resulting application will show both (the FXML and the JavaFX component) in one perspective.

### The resulting application ###
<br/>
<div align="center">
![basic perspective](/img/perspectiveWithComponents.jpg)
</div>
## Managed fragments ##
The next (optional) step is to create reusable controls, the so called "ManagedFragments". A ManagedFragment has access to the parent context (of the component or perspective), can use dependency injection like any other component and can be used to create parts of your view.
Like any other component or perspective a ManagedFragment can define it's view either in JavaFX or FXML.
## The FXML ManagedFragment example ##

<pre>
@Fragment(id = ComponentIds.FRAGMENT_ONE,
        <b>viewLocation = "/fxml/FragmentOne.fxml",</b>
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US",
        scope = Scope.PROTOTYPE)
public class FragmentOne {
}
</pre>

### The FragmentOne.fxml file ###

```xml
<VBox fx:id="mainPane" xmlns="http://javafx.com/javafx/8"
      xmlns:fx="http://javafx.com/fxml/1" HBox.hgrow="ALWAYS" style="-fx-background-color:#f5f5f5">
    <children>
        <HBox prefHeight="100.0" prefWidth="200.0" fx:id="top">
            <children>
                <Label text="Phone:">
                    <font>
                        <Font size="29.0"/>
                    </font>
                    <HBox.margin>
                        <Insets left="5.0" right="5.0" top="15.0"/>
                    </HBox.margin>
                </Label>
                <TextField prefHeight="50.0" HBox.hgrow="ALWAYS">
                    <padding>
                        <Insets top="10.0"/>
                    </padding>
                    <HBox.margin>
                        <Insets right="5.0" top="10.0"/>
                    </HBox.margin>
                </TextField>
            </children>
        </HBox>
        <HBox prefHeight="100.0" prefWidth="200.0" fx:id="bottom">
           ...
        </HBox>
    </children>
</VBox>
```

### The JavaFX ManagedFragment example ###
The same UI can be also achieved using plain JavaFX. In this case a ManagedFragment extends a node / control.
<pre>
@Fragment(id = ComponentIds.FRAGMENT_TWO,
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US",
        scope = Scope.PROTOTYPE)
public class FragmentTwo extends VBox{
	public FragmentTwo() {
          setStyle("-fx-background-color:#f5f5f5");
          HBox.setHgrow(this, Priority.ALWAYS);
          final HBox top = new HBox();
          top.setPrefHeight(100);
          top.setPrefWidth(200);
          Label firstName = new Label("Phone:");
          firstName.setFont(new Font(29.0));
          HBox.setMargin(firstName, new Insets(15,5,0,5));
          TextField firstNameText = new TextField();
          firstNameText.setPrefHeight(50);
          firstNameText.setPadding(new Insets(10, 0, 0, 0));
          HBox.setHgrow(firstNameText,Priority.ALWAYS);
          HBox.setMargin(firstNameText, new Insets(10,5,0,0));
          top.getChildren().addAll(firstName,firstNameText);
          final HBox bottom = new HBox();
          ...
          this.getChildren().addAll(top,bottom);
          }
}
</pre> 

### Creating a ManagedFragment instance ###
ManagedFragments can be used in perspectives or components. To create a new instance the JacpFX Context provides a method to create a typed fragment handler. The following example will create a ManagedFragment in ComponentOne and include the fragment view to his component view.
<pre>
@View(id = ComponentIds.COMPONENT_TWO,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        initialTargetLayoutId = PerspectiveIds.TARGET_CONTAINER_MAIN)
public class ComponentTwo implements FXComponent {
...
	@PostConstruct
    public void onStartComponent(final FXComponentLayout arg0,
                                 final ResourceBundle resourceBundle) {
        pane = (VBox) createUI();
        HBox lastRow = new HBox();
       <b> ManagedFragmentHandler<FragmentOne> fragment = context.getManagedFragmentHandler(FragmentOne.class);
        lastRow.getChildren().addAll(fragment.getFragmentNode());</b>
        pane.getChildren().add(lastRow);
    }
}
</pre>

> Note: The same ManagedFragment class can be reused in any other component or perspective. Detailed documentation about context access and the scope of ManagedFragments can be found in the documentation section.

### The final UI  ###
<br/>
<div align="center">
![basic perspective](/img/fullResult.jpg)
</div>
> The example code you can download here : [ui composition project](https://github.com/JacpFX/JacpFX-misc/tree/master/JacpFX-ui-composition)