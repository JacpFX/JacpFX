
#Structuring and composition#
JacpFX can help you to define your application UI in various ways. It allows you to mix FXML and JavaFX easily. 

* The root of a JacpFX application is always the workbench which defines an application window.

* A workbench contains 1-n perspectives, each defining the layout of your current view. A perspective defines his view eigther programatically with JavaFX or by using a FXML file. In each view you can register several UI nodes to be a target (placeholder) for a component view. 

* A perspective contains 0-n UI components. Each component registers for a specific target (defined in the parent perspective) where the component view will be rendered. A component can represent a complex form or any other complex UI part. Like a perspective you can define the view in FXML or JavaFX.

* A component can contain 0-n ManagedFragments. A ManagedFragment is a reusable custom control which has access to the context of the parent component an can use DI (in case you use a DI container like Spring). A ManagedForm can be, for example, a part of a complex form (the address part) which can be reused in other components. Like perspectives and components a ManagedFragment can have a FXML or JavaFX view.

> The example below will demonstrate how to define FXML and JavaFX perspectives, how to declare the targets for components and how to implement FXML and JavaFX components. Each component will use a ManagedFragment for a specific part of the view to show the usage of ManagedFragments.

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


####the ExamplePerspective.fxml :

```xml
<BorderPane id="mainPane"
            xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
    <center>
        <SplitPane fx:id="mainLayout" dividerPositions="0.55" focusTraversable="true"
                   orientation="VERTICAL"  HBox.hgrow="ALWAYS">
            <items>
                <HBox fx:id="contentTop" />
                <BorderPane fx:id="contentBottom" >
                    <center>
                        <Button fx:id="myButton" text="my button"/>
                    </center>
                </BorderPane>
            </items>
        </SplitPane>
    </center>
</BorderPane>
```

### JavaFX perspective example ###
The code example below will produce exactly the same UI output like the FXML Perspective. You are free to mix FXML- and JavaFX-perspectives in a workbench. 

```java
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
        BorderPane contentBottom = new BorderPane();
        contentBottom.setCenter(new Button("my button"));
        mainLayout.getItems().addAll(contentTop,contentBottom);

        // Register root component
        perspectiveLayout.registerRootComponent(mainPane);
 		...
    }
}
```
> Note: In case of JavaFX views you have to register the root component of your view:

```java
// Register root component
perspectiveLayout.registerRootComponent(mainPane);
```

#### The resulting UI will look in both cases (FXML and JavaFX) like this: ####
![basic perspective](/img/basicPerspective.jpg)

## Define target areas for components ##
Both perspectives defines a very basic SplitPane layout with a top content- and bottom-content area. 