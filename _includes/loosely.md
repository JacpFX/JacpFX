
#Loosely coupled#
A JacpFX application has a hierarchical structure composed of a <i>Workbench</i>, <i>Perspective</i>(s) and <i>Component</i>(s). All references to them are handled by ID's inside the component annotation of the parent component.
<br/>
<div align="center">
![loosely coupled](/img/JacpFX_Loosely_Coupled.png)
</div>
<br/>
Example <i>Workbench</i> annotation, referencing two perspectives:
<br/>
<pre>
@Workbench(id = "id1", name = "workbench",
      <b>  perspectives = {
                BaseConfiguration.PERSPECTIVE_TWO,
                BaseConfiguration.PERSPECTIVE_ONE
        })</b>
</pre> 
<br/>
Example <i>Perspective</i> annotation, referencing two <i>Components</i>:
<br/>
<pre>
@Perspective(id = BaseConfiguration.PERSPECTIVE_ONE, name = "contactPerspective",
        <b>components = {
        	BaseConfiguration.COMPONENT_ONE, 
        	BaseConfiguration.COMPONENT_TWO
        	},</b>
        resourceBundleLocation = "bundles.languageBundle")
</pre>
<br/>
<i>Perspectives</i> and <i>Components</i> are resolved by <i>Component</i> scanning on application startup. To exchange a <i>Component</i> reference in a <i>Perspective</i>, simply remove the "old" <i>Component</i> ID and add the new one.
<br/>
## Loosely coupled view rendering ##
<i>Perspectives</i> are defining the layout structure of your view. You can define "render-targets" in your <i>Perspective</i> by ID, to mark areas where <i>Component</i> views should be rendered. Each <i>Component</i> of a <i>Perspective</i> can register to a specific "render-target", so the resulting view of a <i>Component</i> will be included at that specific area. The "render-target" of a <i>Component</i> can also be changed at runtime, so a <i>Component</i> view can be moved from one area in <i>Perspective</i> to an other.
### Register targets in a perspective ### 
<pre>
@Perspective(id = BaseConfig.ID, name = "p1",components = {…},
        <b>viewLocation = "/fxml/ExamplePerspective.fxml")</b>
public class ExampleFXMLPerspective implements FXPerspective {
  	@FXML
    private HBox contentTop;
    @FXML
    private BorderPane contentBottom;
    
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
<br/>
### <i>Component</i> example: ###
<pre>
@DeclarativeView(id = ComponentIds.COMPONENT_ONE,
        name = "SimpleView",
        active = true,
        resourceBundleLocation = "bundles.languageBundle",
        <b>initialTargetLayoutId = PerspectiveIds.TARGET_CONTAINER_TOP,</b>
        viewLocation = "/fxml/ComponentOne.fxml")
public class ComponentOne implements FXComponent {

...
}

</pre>




