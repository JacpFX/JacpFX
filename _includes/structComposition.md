
#Structuring and composition#
JacpFX can help you to define your application UI in various ways. 

* The root of a JacpFX application is always the workbench which defines an application window.
* A workbench contains 1-n perspectives, each defining the layout of your current view. A perspective defines his view eigther programatically with JavaFX or by using a FXML file:

## FXML perspective example ##
<pre>
@Perspective(id = BaseConfig.ID, name = „p1“,components = {…},
        <b>viewLocation = „/fxml/ExamplePerspective.fxml“)</b>
public class ExamplePerspective implements FXPerspective {
   …
}
</pre>