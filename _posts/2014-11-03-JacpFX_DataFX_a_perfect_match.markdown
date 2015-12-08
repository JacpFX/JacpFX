---
layout: post
title:  JacpFX-DataFX a perfect match
date:   2014-11-03 21:27:27
---
# JacpFX and DataFX-flows, a perfect match for JavaFX

JacpFX is a RCP framework based on JavaFX, supporting developers to structure an application with loosely coupled, reusable components and DataFX-flow is an API that allows you to define flows between controllers and views in JavaFX.

Both projects developed independently, and while I developed a JacpFX application I realized the lack of flow support. Integrating a simple DataFX-flow into JacpFX was quite easy, a JacpFX component is a controller with either a FXML view or a plain JavaFX view; on the other hand the FlowHandler of a DataFX-flow returns a StackPane which is easy to integrate in your current view. The drawback of this simple solution is the isolation of your DataFX-flow context from the JacpFX context which means, they have no direct access to each other.

At JavaOne I had the chance to spend a hour with Hendrik Ebbers (one of the DataFX committers) and to create a solution for this. The resulting JacpFX/DataFX-flow plugin allows you to inject the JacpFX context of a specific component to DataFX-flow controllers. It allows DataFX-flow controllers to communicate via the JacpFX message-bus with the rest of the JacpFX application. Beside this, the DataFX-flow controller gets access to resource-bundles and other methods/resources of the JacpFX component. <br/>


<div align="center">
<img src="http://jacpfx.org/img/JacpFX_DataFX.png" class="img-responsive" style="position: center;" align="middle" alt="JacpFX/DataFX-flow">

</div>
<br/>


To demonstrate the usage of both frameworks I created a simple example application based on following steps:

## Create a JacpFX application from archetype
JacpFX provides a simple archetype with two example perspectives (FXML + JavaFX view) and two components, reused in both perspectives. To create a basic JacpFX application use the simple archetype:

```bash
mvn archetype:generate  -DarchetypeGroupId=org.jacpfx  -DarchetypeArtifactId=JacpFX-simple-quickstart  -DarchetypeVersion=2.0.2
```
To integrate the DataFX-flow plugin add following dependency to your pom:

```xml
        <dependency>
            <groupId>org.jacpfx</groupId>
            <artifactId>jacpfx.JavaFXDataFXPlugin</artifactId>
            <version>1.0</version>
            <scope>compile</scope>
        </dependency>
```
## Create a simple Flow between two DataFX-flow controllers
Now we create two DataFX-flow controllers, a WizardStartController and a Wizard1Controller. The Wizard1Controller will contain the JacpFX context and include a textfield, text input will be send via messages to a JacpFX component. The controller will look like this:

```java
@FXMLController(value="/fxml/wizard1.fxml", title = "Wizard: Step 1")
public class Wizard1Controller {

    @FXML
    @ActionTrigger("back")
    private Button backButton;

    @FXML
    @ActionTrigger("next")
    private Button nextButton;

	/**
	** The JacpFX context
	**/
    @Resource
    private Context context;


    @FXML
    private TextField name;


    @PostConstruct
    public void init() {
        nextButton.setDisable(true);
        name.setOnKeyReleased(event->{
            context.send(BasicConfig.COMPONENT_BOTTOM, name.getText());
		});
    }

}

```


## Integrate the DataFX-flow into a JacpFX component
To be able to inject a JacpFX context into a DataFX-flow controller I created a DataFX <i>Flow</i>-wrapper which additionally takes the ID of a JacpFX component who’s context should be injected. The usage of the DataFX <i>Flow</i> is exactly the same, so we create a <i>FlowHandler</i> and get the root node of the DataFX-flow when starting the <i>FlowHandler</i>. The root node can now be included to your JacpFX component view.

```java
@View(id = BasicConfig.COMPONENT_TOP,
        name = "SimpleView",
        resourceBundleLocation = "bundles.languageBundle",
        initialTargetLayoutId = BasicConfig.TARGET_CONTAINER_TOP)
public class ComponentLeft implements FXComponent {
    private Node pane;
    @Resource
    private Context context;

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
    public void onPostConstructComponent(final FXComponentLayout arg0,
                                         final ResourceBundle resourceBundle) {
        this.pane = createUI();
    }


    private Node createUI() {
        final VBox main = new VBox();
        Flow flow = new DataFXFlowWrapper(WizardStartController.class,BasicConfig.COMPONENT_TOP).
                withLink(WizardStartController.class, "next", Wizard1Controller.class).
                withGlobalBackAction("back");
        FlowHandler flowHandler = flow.createHandler();
        StackPane pane = null;
        try {
            pane = flowHandler.start(new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.ZOOM_IN));
			main.getChildren().add(pane);
        } catch (FlowException e) {
            e.printStackTrace();
        }

        return main;
    }


}
```
JacpFX and DataFX-flow needs Java 8 / JavaFX 8 to run, a packed jar you can download here: http://jacpfx.org/data/SimpleDataFX_JacpFX.jar , simply execute:

```bash
java -jar SimpleDataFX_JacpFX.jar

```

The source of this example application can be found here:
https://github.com/amoAHCP/JacpFX-DataFX/tree/master/SimpleDataFX_JacpFX

The basics about DataFX-flow you can find on Hendriks blog here:http://www.guigarage.com/2014/05/datafx-8-0-tutorials/

The JacpFX homepage with all the documentation is http://jacpfx.org
