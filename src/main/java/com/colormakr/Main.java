package com.colormakr;

import com.colormakr.database.DockerStarter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage window;
    public static Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start the Docker Engine

        window = primaryStage;
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("MainStage.fxml"));
        mainScene = new Scene(loader.load());
        mainScene.getStylesheets().add(Main.class.getResource("stylesheet.css").toExternalForm());

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Colormakr");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/com/colormakr/img/icon.png")));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
