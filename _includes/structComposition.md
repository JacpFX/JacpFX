
#Structuring and composition#
JacpFX can help you to define your application UI in various ways. 

* The root of a JacpFX application is always the workbench which defines an application window.
* A workbench contains 1-n perspectives, each defining the layout of your current view. A perspective defines his view eigther programatically with JavaFX or by using a FXML file:

## FXML perspective example ##
<pre>
@Perspective(id = BaseConfig.ID, name = "p1",components = {…},
        <b>viewLocation = "/fxml/ExamplePerspective.fxml")</b>
public class ExamplePerspective implements FXPerspective {
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
                        <Button fx:id="myButton" mnemonicParsing="false" text="my botton"/>
                    </center>
                </BorderPane>
            </items>
        </SplitPane>
    </center>
</BorderPane>
```

## JacpFX perspective example ##