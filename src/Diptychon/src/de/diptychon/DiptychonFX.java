/*
 * This file is part of Diptychon.
 *
 * Diptychon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Diptychon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Diptychon. If not, see <http://www.gnu.org/licenses/>.
 */
package de.diptychon;

// for scene graph visualization (debug), include ScenicView to buildpath and comment in the following line
// import com.javafx.experiments.scenicview.ScenicView;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import de.diptychon.ui.MainFrame;

/**
 * The Diptychon starting point
 */
public class DiptychonFX extends Application {
    
    public static final String VERSION = "20160815";

    public static int[] progressWatch = null;

    private Pane splashLayout;

    // private Stage mainStage;

    private Stage primaryStage;

    private static final int SPLASH_WIDTH = 744;

    private static final int SPLASH_HEIGHT = 408;

    private Label version;

    /**
     * Shows the Main Stage
     */
    private void showMainStage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        primaryStage = new Stage();
                        DiptychonLogger.createDiptychonLogger();
                        DiptychonPreferences.readElements();
                        final DiptychonFX_Factory factory = new DiptychonFX_Factory();

                        final MainFrame mainFrame = factory.initDiptychon();

                        final Rectangle2D bounds = Screen.getPrimary()
                                .getVisualBounds();
                        final Scene scene = new Scene(mainFrame.getView());
                        mainFrame.bindTitleProperty(primaryStage
                                .titleProperty());
                        // for scene graph visualization (debug), include
                        // ScenicView to buildpath and comment in the
                        // following line
                        // ScenicView.show(scene);
                        primaryStage.setScene(scene);
                        primaryStage.setX(bounds.getMinX());
                        primaryStage.setY(bounds.getMinY());
                        primaryStage.setWidth(bounds.getWidth());
                        primaryStage.setHeight(bounds.getHeight());

                        primaryStage.getIcons().add(
                                new Image(this.getClass().getResourceAsStream(
                                        "/icons/diptychon_icon.png")));

                        primaryStage
                                .setOnCloseRequest(new EventHandler<WindowEvent>() {
                                    @Override
                                    public void handle(final WindowEvent event) {
                                        mainFrame
                                                .handleExitAction(new ActionEvent());
                                        event.consume();
                                    }
                                });

                        DiptychonLogger.info("Show stage.");
                        primaryStage.show();
                        primaryStage.toBack();
                        mainFrame.openRecentlyUsed();
                    }
                });
            }

        }).start();
    }

    /**
     * Shows the splash screen
     */
    private void showSplash(final Stage initStage,
            Task<ObservableList<String>> task) {
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(
                    ObservableValue<? extends Worker.State> observableValue,
                    Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    initStage.toFront();
                    FadeTransition fadeSplash = new FadeTransition(Duration
                            .seconds(1.5), splashLayout);
                    fadeSplash.setFromValue(1.0);
                    fadeSplash.setToValue(0.0);
                    fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            initStage.hide();
                        }
                    });
                    fadeSplash.play();
                }
            }
        });
        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH
                / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2
                - SPLASH_HEIGHT / 2);
        initStage.setResizable(true);
        initStage.setHeight(SPLASH_HEIGHT - 20);
        initStage.show();
    }

    /**
     * Shows the splash screen and the main stage
     * 
     * @param primaryStage
     *            the primary stage for this application, onto which the
     *            application scene can be set.
     */
    @Override
    public void start(final Stage primaryStage) {
        final Task<ObservableList<String>> friendTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                return null;
            }
        };

        showSplash(primaryStage, friendTask);
        showMainStage();
        new Thread(friendTask).start();

        /*
       */
    }

    /**
     * Initializes the splash screen
     */
    @Override
    public void init() {
        ImageView splash = new ImageView(new Image(this.getClass()
                .getResourceAsStream("/splash/diptychon4.png")));
        splashLayout = new VBox();
        version = new Label("Version " + DiptychonFX.VERSION);
        version.setAlignment(Pos.TOP_RIGHT);
        version.setTranslateX(SPLASH_WIDTH - 20 - version.getText().length()
                * 8);
        version.setTranslateY(-SPLASH_HEIGHT + 20);
        splashLayout.getChildren().addAll(splash, version);
        splashLayout
                .setStyle("-fx-padding: 0; -fx-background-color: white; -fx-border-width:1; -fx-border-color: linear-gradient(to bottom, darkgoldenrod, derive(gold, 50%));");
        splashLayout.setEffect(new DropShadow());
    }

    /**
     * The main method
     * 
     * @param args
     *            command line parameter (not used)
     */
    public static void main(final String[] args) {
        launch();
    }
}
