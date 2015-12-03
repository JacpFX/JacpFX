package org.jacp.test.toolbar.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jacp.test.toolbar.workbench.WorkbenchHandleToolBarButtonsBetweenPerspectives;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.jacpfx.spring.launcher.AFXSpringXmlLauncher;

import java.util.logging.Logger;

/**
 * Created by Andy Moncsek on 09.11.15.
 */
public class ApplicationHandleToolBarButtonsBetweenPerspectives extends AFXSpringXmlLauncher {
    private static final Logger log = Logger.getLogger(ApplicationHandleToolBarButtonsBetweenPerspectives.class
            .getName());
    public static final String[] STYLES = new String[2];
    private static final String[] STYLE_FILES = {"/styles/style_light.css", "/styles/style_dark.css"};
    /// binary style sheets created while deployment
    private static final String[] BINARY_FILES = {"/styles/style_light.bss", "/styles/style_dark.bss"};


    @Override
    public String getXmlConfig() {
        return "main.xml";
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchHandleToolBarButtonsBetweenPerspectives.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp"};
    }



    @Override
    public void postInit(final Stage stage) {
        stage.setMinHeight(580);
        stage.setMinWidth(800);
        final Scene scene = stage.getScene();
        stage.getIcons().add(new Image("images/icons/JACP_512_512.png"));
        // add style sheet
    }




}
