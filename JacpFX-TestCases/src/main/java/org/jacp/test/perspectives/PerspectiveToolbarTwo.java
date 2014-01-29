package org.jacp.test.perspectives;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jacp.test.components.ComponentIds;
import org.jacp.test.main.ApplicationLauncher;
import org.jacpfx.api.annotations.Resource;
import org.jacpfx.api.annotations.lifecycle.PostConstruct;
import org.jacpfx.api.annotations.lifecycle.PreDestroy;
import org.jacpfx.api.annotations.perspective.Perspective;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.componentLayout.FXComponentLayout;
import org.jacpfx.rcp.componentLayout.PerspectiveLayout;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.perspective.FXPerspective;
import org.jacpfx.rcp.util.FXUtil;

import java.util.ResourceBundle;

/**
 * @author: Patrick Symmangk (pete.jacp@gmail.com)
 */
@Perspective(id = PerspectiveIds.PerspectiveToolbarTwo, name = "toolBarTwoPerspective",
        components = {ComponentIds.ComponentHandleToolBarBetweenPerspectives2},
        viewLocation = "/fxml/toolBarperspectiveTwo.fxml",
        resourceBundleLocation = "bundles.languageBundle",
        localeID = "en_US", active = true)
public class PerspectiveToolbarTwo implements FXPerspective {
    @FXML
    private HBox content1;
    @FXML
    private HBox content2;
    @FXML
    private HBox content3;
    @Resource
    Context context;

    @Override
    public void handlePerspective(final Message<Event, Object> action,
                                  final PerspectiveLayout perspectiveLayout) {
        if (action.messageBodyEquals(FXUtil.MessageUtil.INIT)) {

            GridPane.setVgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);
            GridPane.setHgrow(perspectiveLayout.getRootComponent(),
                    Priority.ALWAYS);

            // register left panel
            perspectiveLayout.registerTargetLayoutComponent("content0",
                    this.content1);
            perspectiveLayout.registerTargetLayoutComponent("content1",
                    this.content2);
            perspectiveLayout.registerTargetLayoutComponent("content2",
                    this.content3);
            ApplicationLauncher.latch.countDown();
        }

    }


    @PostConstruct
    /**
     * @OnStart annotated method will be executed when component is activated.
     * @param layout
     * @param resourceBundle
     */
    public void onStartPerspective(final FXComponentLayout layout,
                                   final ResourceBundle resourceBundle) {
        System.out.println("START " + layout);
        final JACPToolBar toolbar = layout.getRegisteredToolBar(ToolbarPosition.NORTH);

        final Button p1 = new Button("Perspective A" + PerspectiveIds.PerspectiveToolbarTwo);
        p1.setOnMouseClicked((event) -> context.send(PerspectiveIds.PerspectiveToolbarOne, "show"));
        final Button p2 = new Button("Perspective B" + PerspectiveIds.PerspectiveToolbarTwo);
        p2.setOnMouseClicked((event) -> context.send(PerspectiveIds.PerspectiveToolbarTwo, "show"));
        toolbar.addToCenter(p1);
        toolbar.addOnEnd(p2);
    }


    @PreDestroy
    /**
     * @OnTearDown annotated method will be executed when component is deactivated.
     * @param arg0
     */
    public void onTearDownPerspective(final FXComponentLayout arg0) {
        // remove toolbars and menu entries when close perspective

    }

}
