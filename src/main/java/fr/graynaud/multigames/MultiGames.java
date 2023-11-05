package fr.graynaud.multigames;

import fr.graynaud.multigames.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MultiGames extends Application {

    private MainView mainView;

    @Override
    public void start(Stage stage) {
        this.mainView = new MainView(stage);
        Scene scene = new Scene(this.mainView.getRoot());
        scene.getStylesheets().add(MultiGames.class.getResource("/style/style.css").toExternalForm());

        stage.setTitle("MultiGames");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.getIcons().add(new Image(MultiGames.class.getResourceAsStream("/favicon.ico")));
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
