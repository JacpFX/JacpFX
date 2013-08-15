package org.jacp.main;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The application Preloader. It is just a simple splash screen on application
 * init. The preloader shows up only when execute the package.
 * 
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 * 
 */
public class ApplicationPreloader extends Preloader {
	private ProgressBar bar;
	private Stage stage;
	private boolean isEmbedded = false;

	public void start(Stage stage) throws Exception {
		isEmbedded = (stage.getWidth() > 0);
		this.stage = stage;
		stage.setScene(createPreloaderScene());
		stage.show();
	}

	@Override
	public void handleProgressNotification(ProgressNotification pn) {
		bar.setProgress(pn.getProgress());
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification evt) {
		if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
			if (isEmbedded && stage.isShowing()) {
				// fade out, hide stage at the end of animation
				final FadeTransition ft = new FadeTransition(
						Duration.millis(3000), stage.getScene().getRoot());
				ft.setFromValue(1.0);
				ft.setToValue(0.0);
				final Stage s = stage;
				final EventHandler<ActionEvent> eh = new EventHandler<ActionEvent>() {
					public void handle(ActionEvent t) {
						s.hide();
					}
				};
				ft.setOnFinished(eh);
				ft.play();
			} else {
				stage.hide();
			}
		}
	}

	private Scene createPreloaderScene() {
		bar = new ProgressBar();
		final BorderPane p = BorderPaneBuilder
				.create()
				.center(new ImageView(new Image(this.getClass()
						.getResourceAsStream("/images/jacp-splash.png"))))
				.bottom(bar).build();
		return new Scene(p, 412, 279);
	}
}