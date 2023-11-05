package fr.osallek;

import fr.osallek.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class Main extends Application {

    private MainView mainView;

    @Override
    public void start(Stage stage) {
        this.mainView = new MainView(stage);
        Scene scene = new Scene(this.mainView.getRoot());
        scene.getStylesheets().add(Main.class.getClassLoader().getResource("style/style.css").toExternalForm());

        stage.setTitle("MultiGames");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.getIcons().add(new Image(Main.class.getClassLoader().getResourceAsStream("favicon.ico")));
        stage.show();
    }

    @Override
    public void stop() {
        if (this.mainView != null) {
            this.mainView.stop();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
