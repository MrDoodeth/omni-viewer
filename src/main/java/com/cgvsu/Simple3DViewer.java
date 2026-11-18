package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Simple3DViewer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent viewport = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml/gui.fxml")));

        Scene scene = new Scene(viewport, 1000, 700);
        stage.setMinWidth(640);
        stage.setMinHeight(480);

        stage.setTitle("Omni Viewer");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png"))));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(Simple3DViewer.class, args);
    }
}
