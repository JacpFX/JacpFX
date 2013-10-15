package org.jacp.test.main;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 04.10.13
 * Time: 09:12
 * To change this template use File | Settings | File Templates.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 */
public class HelloAsyncRunLaterSlow extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.err.println("Application.start");

        final Button startButton = new Button("Start");
        final HBox box = new HBox();
        box.setMinHeight(600);
        box.setMinWidth(800);
        box.setCache(true);
        box.getChildren().addAll(startButton);
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
                createThread(box).start();
            }
        });

        Scene scene = new Scene(box);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Thread createThread(final HBox box) {
        return new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                final int counter = i;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Platform.runLater(() -> {

                    box.setCacheHint(CacheHint.SPEED);
                    box.getChildren().clear();
                    box.getChildren().add(new Button("counter: " + counter + " in Thread:" + Thread.currentThread()));
                    box.setCacheHint(CacheHint.QUALITY);
                });

            }

        });
    }

    @Override
    public void stop() throws Exception {
        System.err.println("Application.stop");
    }

    public static void main(String[] args) {
        Application.launch(args);
        System.err.println("return from main method");
    }

}