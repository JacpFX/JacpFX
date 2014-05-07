
#Loosely coupled#
A JacpFX application has a hierarchical structure composed of a workbench, perspective(s) and component(s). All references to perspectives and components handled by ID's inside the component-annotation of the parent component.
<br/>
<br/>
Example workbench annotation, referencing two perspectives:
<br/>
<pre>
@Workbench(id = "id1", name = "workbench",
      <b>  perspectives = {
                BaseConfiguration.PERSPECTIVE_TWO,
                BaseConfiguration.PERSPECTIVE_ONE
        })</b>
</pre> 
<br/>
Example perspective annotation, referencing two components:
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
Perspectives and components are resolved by component scanning on application startup. To exchange a component-reference in a perspective, simply remove the "old" component ID and add the new ID.
<br/>
## Loosely coupled view rendering ##
Perspectives are defining the layout structure of you view. You can define "render" targets by ID in your perspective where a component view should be rendered. Each component of a perspective can register to a specific render target, so the resulting view of a component will be included at that specific area. The render target of a component can also be changed at runtime so a component view can be moved from one area in perspective to an other.
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
### Component example: ###
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




