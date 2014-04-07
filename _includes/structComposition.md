
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
and the ExamplePerspective.fxml :

```
<BorderPane id="BorderPane">
    <center>
        <HBox fx:id="paneRight">
            <children>
                <SplitPane dividerPositions="0.5"
                           orientation="VERTICAL"  HBox.hgrow="ALWAYS">
                    <items>
                        <HBox fx:id="contentTop" />
                        <BorderPane fx:id="contentBottom">
                        </BorderPane>
                    </items>
                </SplitPane>
            </children>
        </HBox>
    </center>
</BorderPane>
```